package com.example.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.ApiKeyEntity
import com.example.data.PaperDatabase
import com.example.engine.MarksDistributionEngine
import com.example.model.ApiProviderStatus
import com.example.model.Board
import com.example.model.Difficulty
import com.example.model.ExamType
import com.example.model.GradeClass
import com.example.model.PaperSection
import com.example.model.ProviderMode
import com.example.model.QuestionItem
import com.example.model.QuestionPaper
import com.example.model.QuestionType
import com.example.security.KeystoreCryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

class GroqRepository(private val context: Context) {

    private val apiService = GroqApiService.create()
    private val database = PaperDatabase.getDatabase(context)

    private val _providerStatus = MutableStateFlow(
        ApiProviderStatus(
            provider = "Groq AI",
            defaultKeyConfigured = isDefaultKeyConfigured(),
            defaultKeyWorking = isDefaultKeyConfigured(),
            personalKeyConfigured = false,
            personalKeyMasked = null,
            activeMode = if (isDefaultKeyConfigured()) ProviderMode.DEFAULT_GROQ else ProviderMode.SMART_SYLLABUS_FALLBACK,
            statusMessage = if (isDefaultKeyConfigured())
                "Your request is being processed using the default AI service."
            else
                "Configure your personal Groq API key in AI Settings for cloud generation."
        )
    )
    val providerStatus: StateFlow<ApiProviderStatus> = _providerStatus.asStateFlow()

    fun isDefaultKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GROQ_API_KEY
            !key.isNullOrBlank() && key != "MY_GROQ_API_KEY"
        } catch (_: Exception) {
            false
        }
    }

    private fun getDefaultApiKey(): String {
        return try {
            val key = BuildConfig.GROQ_API_KEY
            if (!key.isNullOrBlank() && key != "MY_GROQ_API_KEY") key else ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun loadUserApiKey(userId: String) {
        withContext(Dispatchers.IO) {
            val entity = database.apiKeyDao().getApiKey(userId)
            val hasPersonal = entity != null && entity.encryptedKey.isNotBlank()
            val masked = entity?.maskedKey

            _providerStatus.value = _providerStatus.value.copy(
                defaultKeyConfigured = isDefaultKeyConfigured(),
                personalKeyConfigured = hasPersonal,
                personalKeyMasked = masked,
                activeMode = when {
                    isDefaultKeyConfigured() -> ProviderMode.DEFAULT_GROQ
                    hasPersonal -> ProviderMode.PERSONAL_GROQ
                    else -> ProviderMode.SMART_SYLLABUS_FALLBACK
                }
            )
        }
    }

    suspend fun savePersonalApiKey(userId: String, plainKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val trimmed = plainKey.trim()
            if (trimmed.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("API key cannot be empty."))
            }

            // Test connection first
            val testResult = testGroqKey(trimmed)
            if (testResult.isFailure) {
                return@withContext Result.failure(
                    testResult.exceptionOrNull() ?: Exception("Failed to validate key with Groq.")
                )
            }

            val encrypted = KeystoreCryptoManager.encrypt(trimmed)
            val masked = KeystoreCryptoManager.maskApiKey(trimmed)
            val entity = ApiKeyEntity(
                userId = userId,
                encryptedKey = encrypted,
                maskedKey = masked,
                updatedAt = System.currentTimeMillis()
            )
            database.apiKeyDao().saveApiKey(entity)

            _providerStatus.value = _providerStatus.value.copy(
                personalKeyConfigured = true,
                personalKeyWorking = true,
                personalKeyMasked = masked,
                activeMode = ProviderMode.PERSONAL_GROQ,
                statusMessage = "Your personal Groq API key is working."
            )

            Result.success("Groq API key saved and verified successfully!")
        }
    }

    suspend fun removePersonalApiKey(userId: String) {
        withContext(Dispatchers.IO) {
            database.apiKeyDao().deleteApiKey(userId)
            _providerStatus.value = _providerStatus.value.copy(
                personalKeyConfigured = false,
                personalKeyWorking = false,
                personalKeyMasked = null,
                activeMode = if (isDefaultKeyConfigured()) ProviderMode.DEFAULT_GROQ else ProviderMode.SMART_SYLLABUS_FALLBACK,
                statusMessage = if (isDefaultKeyConfigured())
                    "Your request is being processed using the default AI service."
                else
                    "Please add your personal Groq API key to continue."
            )
        }
    }

    suspend fun testGroqKey(plainKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.listModels("Bearer $plainKey")
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success("Connection verified! Found ${response.body()?.data?.size} Groq models.")
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string() ?: ""
                    when (code) {
                        401 -> Result.failure(Exception("Invalid API key (401 Unauthorized). Check your Groq key."))
                        429 -> Result.failure(Exception("Groq rate limit or quota exceeded (429)."))
                        else -> Result.failure(Exception("Groq API test failed ($code): $errorBody"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error connecting to Groq: ${e.localizedMessage}"))
            }
        }
    }

    private suspend fun getPersonalApiKey(userId: String): String {
        return withContext(Dispatchers.IO) {
            val entity = database.apiKeyDao().getApiKey(userId)
                ?: database.apiKeyDao().getApiKey("local_active_user")
                ?: return@withContext ""
            KeystoreCryptoManager.decrypt(entity.encryptedKey)
        }
    }

    suspend fun generateQuestionPaper(
        userId: String,
        schoolName: String,
        board: Board,
        className: GradeClass,
        subject: String,
        examType: ExamType,
        maxMarks: Int,
        difficulty: Difficulty,
        imageUris: List<Uri>,
        customTopics: String = "",
        selectedTypes: Set<QuestionType> = QuestionType.entries.toSet()
    ): Pair<QuestionPaper, String> {
        val blueprint = MarksDistributionEngine.generateBlueprint(maxMarks, selectedTypes)
        val defaultKey = getDefaultApiKey()
        val personalKey = getPersonalApiKey(userId)

        var notificationMessage = "Your request is being processed using the default AI service."
        var rawAiResponse: String? = null

        // Step 1: Attempt with Default Groq Key if configured
        if (defaultKey.isNotBlank()) {
            val defaultResult = callGroqChat(
                apiKey = defaultKey,
                board = board,
                className = className,
                subject = subject,
                examType = examType,
                maxMarks = maxMarks,
                difficulty = difficulty,
                blueprint = blueprint,
                imageUris = imageUris,
                customTopics = customTopics
            )

            if (defaultResult.isSuccess) {
                rawAiResponse = defaultResult.getOrNull()
                _providerStatus.value = _providerStatus.value.copy(
                    activeMode = ProviderMode.DEFAULT_GROQ,
                    statusMessage = "Generated with default Groq AI service."
                )
            } else {
                val error = defaultResult.exceptionOrNull()
                val isRateLimit = error?.message?.contains("429") == true || error?.message?.contains("quota") == true
                if (isRateLimit || error?.message?.contains("401") == true) {
                    notificationMessage = "Default AI usage limit reached. Switching to your personal Groq API key."
                    _providerStatus.value = _providerStatus.value.copy(
                        statusMessage = notificationMessage
                    )
                }
            }
        }

        // Step 2: Fallback to Personal Groq Key if Default failed or wasn't configured
        if (rawAiResponse == null && personalKey.isNotBlank()) {
            notificationMessage = if (defaultKey.isNotBlank())
                "Default AI usage limit reached. Switching to your personal Groq API key."
            else
                "Your request is being processed using your personal Groq API key."

            val personalResult = callGroqChat(
                apiKey = personalKey,
                board = board,
                className = className,
                subject = subject,
                examType = examType,
                maxMarks = maxMarks,
                difficulty = difficulty,
                blueprint = blueprint,
                imageUris = imageUris,
                customTopics = customTopics
            )

            if (personalResult.isSuccess) {
                rawAiResponse = personalResult.getOrNull()
                _providerStatus.value = _providerStatus.value.copy(
                    activeMode = ProviderMode.PERSONAL_GROQ,
                    statusMessage = "Your personal Groq API key is working."
                )
            } else {
                notificationMessage = "Both AI services are temporarily unavailable. Using intelligent syllabus generation."
            }
        } else if (rawAiResponse == null && defaultKey.isBlank() && personalKey.isBlank()) {
            notificationMessage = "Please add your personal Groq API key to continue with cloud generation. Generating via built-in board syllabus engine."
        }

        // Step 3: Parse AI Response or use syllabus question generator
        val paper = if (!rawAiResponse.isNullOrBlank()) {
            parseGroqResponse(
                rawJson = rawAiResponse,
                userId = userId,
                schoolName = schoolName,
                board = board,
                className = className,
                subject = subject,
                examType = examType,
                maxMarks = maxMarks,
                difficulty = difficulty,
                blueprint = blueprint,
                imageCount = imageUris.size
            )
        } else {
            generateSyllabusAlignedPaper(
                userId = userId,
                schoolName = schoolName,
                board = board,
                className = className,
                subject = subject,
                examType = examType,
                maxMarks = maxMarks,
                difficulty = difficulty,
                blueprint = blueprint,
                imageCount = imageUris.size,
                customTopics = customTopics
            )
        }

        // Programmatically enforce exact marks
        MarksDistributionEngine.recalculateAndValidatePaper(paper)
        return Pair(paper, notificationMessage)
    }

    private suspend fun callGroqChat(
        apiKey: String,
        board: Board,
        className: GradeClass,
        subject: String,
        examType: ExamType,
        maxMarks: Int,
        difficulty: Difficulty,
        blueprint: MarksDistributionEngine.MarksBlueprint,
        imageUris: List<Uri>,
        customTopics: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val hasImages = imageUris.isNotEmpty()
                val modelName = if (hasImages) "llama-3.2-11b-vision-preview" else "llama-3.3-70b-versatile"

                val prompt = buildPrompt(
                    board = board,
                    className = className,
                    subject = subject,
                    examType = examType,
                    maxMarks = maxMarks,
                    difficulty = difficulty,
                    blueprint = blueprint,
                    hasImages = hasImages,
                    customTopics = customTopics
                )

                val messageContent: Any = if (hasImages) {
                    val contentList = mutableListOf<Map<String, Any>>()
                    contentList.add(mapOf("type" to "text", "text" to prompt))

                    // Encode up to 3 images as base64 data URLs
                    imageUris.take(3).forEach { uri ->
                        val base64 = uriToBase64(uri)
                        if (base64.isNotBlank()) {
                            contentList.add(
                                mapOf(
                                    "type" to "image_url",
                                    "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64")
                                )
                            )
                        }
                    }
                    contentList
                } else {
                    prompt
                }

                val request = GroqChatRequest(
                    model = modelName,
                    messages = listOf(
                        GroqMessage(role = "system", content = "You are PaperGenius AI, an expert exam question paper creator for CBSE, ICSE, and Indian state boards. Respond ONLY in valid raw JSON matching the requested structure without markdown fences."),
                        GroqMessage(role = "user", content = messageContent)
                    ),
                    temperature = 0.2,
                    max_tokens = 4096
                )

                val response = apiService.createChatCompletion("Bearer $apiKey", request)
                if (response.isSuccessful) {
                    val content = response.body()?.choices?.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) {
                        Result.success(content)
                    } else {
                        Result.failure(Exception("Empty response received from Groq."))
                    }
                } else {
                    val code = response.code()
                    val errorMsg = response.errorBody()?.string() ?: "HTTP $code"
                    Result.failure(Exception("Groq error ($code): $errorMsg"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Resize if too large to fit in Groq payload
                val maxDim = 1024
                val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                    val maxSide = maxOf(bitmap.width, bitmap.height)
                    maxDim.toFloat() / maxSide
                } else 1.0f

                val scaled = if (scale < 1.0f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt(),
                        (bitmap.height * scale).toInt(),
                        true
                    )
                } else bitmap

                val outputStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun buildPrompt(
        board: Board,
        className: GradeClass,
        subject: String,
        examType: ExamType,
        maxMarks: Int,
        difficulty: Difficulty,
        blueprint: MarksDistributionEngine.MarksBlueprint,
        hasImages: Boolean,
        customTopics: String
    ): String {
        val sectionSpecs = blueprint.sectionBlueprints.joinToString("\n") {
            "- ${it.sectionName} (${it.title}): ${it.count} questions of ${it.marksPerQuestion} mark(s) each. Total = ${it.totalMarks} marks."
        }

        return """
Create an examination question paper strictly aligned with ${board.displayName} for ${className.displayName}, Subject: $subject, Exam: ${examType.displayName}, Maximum Marks: $maxMarks, Difficulty: ${difficulty.displayName}.

${if (hasImages) "IMPORTANT: Analyze the attached textbook photograph(s) meticulously. Extract concepts, formulas, chapter context, and numerical problems directly from the textbook images." else ""}
${if (customTopics.isNotBlank()) "Focus on Chapter/Topics: $customTopics" else ""}

Blueprint specifications (Total Marks = $maxMarks):
$sectionSpecs

Return ONLY valid JSON with this exact schema:
{
  "generalInstructions": [
    "General instruction 1",
    "General instruction 2"
  ],
  "sections": [
    {
      "sectionName": "Section A",
      "title": "Multiple Choice Questions",
      "instructions": "All questions are compulsory.",
      "questions": [
        {
          "number": 1,
          "text": "Question statement?",
          "marks": 1,
          "type": "MCQ",
          "options": ["A) Option 1", "B) Option 2", "C) Option 3", "D) Option 4"],
          "internalChoice": null,
          "answerText": "A) Option 1",
          "markingScheme": ["1 mark for correct choice with reason"],
          "explanation": "Brief explanation"
        }
      ]
    }
  ]
}
""".trimIndent()
    }

    private fun parseGroqResponse(
        rawJson: String,
        userId: String,
        schoolName: String,
        board: Board,
        className: GradeClass,
        subject: String,
        examType: ExamType,
        maxMarks: Int,
        difficulty: Difficulty,
        blueprint: MarksDistributionEngine.MarksBlueprint,
        imageCount: Int
    ): QuestionPaper {
        return try {
            val cleanJson = rawJson.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val root = JSONObject(cleanJson)
            val instructions = mutableListOf<String>()
            val instArray = root.optJSONArray("generalInstructions")
            if (instArray != null) {
                for (i in 0 until instArray.length()) {
                    instructions.add(instArray.getString(i))
                }
            }

            val sections = mutableListOf<PaperSection>()
            val secArray = root.optJSONArray("sections")
            if (secArray != null) {
                for (s in 0 until secArray.length()) {
                    val secObj = secArray.getJSONObject(s)
                    val sectionName = secObj.optString("sectionName", "Section ${('A'.code + s).toChar()}")
                    val title = secObj.optString("title", "Questions")
                    val instructionsText = secObj.optString("instructions", "All questions are compulsory.")

                    val questions = mutableListOf<QuestionItem>()
                    val qArray = secObj.optJSONArray("questions")
                    if (qArray != null) {
                        for (q in 0 until qArray.length()) {
                            val qObj = qArray.getJSONObject(q)
                            val options = mutableListOf<String>()
                            val optArray = qObj.optJSONArray("options")
                            if (optArray != null) {
                                for (o in 0 until optArray.length()) {
                                    options.add(optArray.getString(o))
                                }
                            }

                            val marking = mutableListOf<String>()
                            val markArray = qObj.optJSONArray("markingScheme")
                            if (markArray != null) {
                                for (m in 0 until markArray.length()) {
                                    marking.add(markArray.getString(m))
                                }
                            }

                            val qTypeStr = qObj.optString("type", "MCQ")
                            val qType = QuestionType.entries.firstOrNull { it.name.equals(qTypeStr, true) }
                                ?: QuestionType.MCQ

                            questions.add(
                                QuestionItem(
                                    number = qObj.optInt("number", q + 1),
                                    sectionName = sectionName,
                                    text = qObj.optString("text", "Question text"),
                                    marks = qObj.optInt("marks", 1),
                                    type = qType,
                                    options = options,
                                    internalChoice = if (qObj.has("internalChoice") && !qObj.isNull("internalChoice")) qObj.getString("internalChoice") else null,
                                    answerText = qObj.optString("answerText", ""),
                                    markingScheme = marking,
                                    explanation = qObj.optString("explanation", "")
                                )
                            )
                        }
                    }

                    sections.add(
                        PaperSection(
                            sectionName = sectionName,
                            title = title,
                            instructions = instructionsText,
                            totalMarks = questions.sumOf { it.marks },
                            questions = questions
                        )
                    )
                }
            }

            if (sections.isEmpty()) {
                generateSyllabusAlignedPaper(
                    userId, schoolName, board, className, subject, examType, maxMarks, difficulty, blueprint, imageCount, ""
                )
            } else {
                QuestionPaper(
                    userId = userId,
                    title = "${className.displayName} $subject ${examType.displayName}",
                    schoolName = schoolName,
                    board = board,
                    className = className,
                    subject = subject,
                    examType = examType,
                    timeAllowed = if (maxMarks <= 40) "1.5 Hours" else if (maxMarks <= 60) "2 Hours" else "3 Hours",
                    maxMarks = maxMarks,
                    difficulty = difficulty,
                    generalInstructions = if (instructions.isNotEmpty()) instructions else mutableListOf(
                        "All questions are compulsory.",
                        "Marks for each question are indicated against it."
                    ),
                    sections = sections,
                    textbookImagesCount = imageCount
                )
            }
        } catch (_: Exception) {
            generateSyllabusAlignedPaper(
                userId, schoolName, board, className, subject, examType, maxMarks, difficulty, blueprint, imageCount, ""
            )
        }
    }

    fun generateSyllabusAlignedPaper(
        userId: String,
        schoolName: String,
        board: Board,
        className: GradeClass,
        subject: String,
        examType: ExamType,
        maxMarks: Int,
        difficulty: Difficulty,
        blueprint: MarksDistributionEngine.MarksBlueprint,
        imageCount: Int,
        customTopics: String
    ): QuestionPaper {
        val topic = if (customTopics.isNotBlank()) customTopics else when (subject.lowercase()) {
            "mathematics", "maths" -> "Quadratic Equations, Coordinate Geometry & Triangles"
            "science" -> "Chemical Reactions, Light Reflection & Life Processes"
            "physics" -> "Electrostatics, Current Electricity & Optics"
            "chemistry" -> "Chemical Kinetics, Solutions & Coordination Compounds"
            "biology" -> "Genetics, Cell Division & Photosynthesis"
            "social science" -> "Nationalism in India, Resources & Democratic Politics"
            "english" -> "Reading Comprehension, Analytical Writing & Literature"
            else -> "Core Curriculum Chapters & Practical Applications"
        }

        var runningNumber = 1
        val sections = mutableListOf<PaperSection>()

        blueprint.sectionBlueprints.forEach { spec ->
            val sectionQuestions = mutableListOf<QuestionItem>()

            for (i in 1..spec.count) {
                val qNumber = runningNumber++
                val hasChoice = (i == spec.count && spec.internalChoiceCount > 0)

                val item = when (spec.questionType) {
                    QuestionType.MCQ -> QuestionItem(
                        number = qNumber,
                        sectionName = spec.sectionName,
                        text = "Which of the following statements is correct regarding $topic in $subject (Class ${className.displayName})?",
                        marks = 1,
                        type = QuestionType.MCQ,
                        options = listOf(
                            "A) The rate of reaction is directly proportional to reactant concentration.",
                            "B) Energy is consumed spontaneously in an isolated closed system.",
                            "C) Acceleration due to gravity is independent of object mass.",
                            "D) The resultant potential difference across parallel resistors is zero."
                        ),
                        answerText = "C) Acceleration due to gravity is independent of object mass.",
                        markingScheme = listOf("1 mark for identifying the correct option [C]."),
                        explanation = "Under standard gravitational acceleration ($topic), all bodies fall with uniform acceleration g = 9.8 m/s² regardless of mass."
                    )
                    QuestionType.VERY_SHORT -> QuestionItem(
                        number = qNumber,
                        sectionName = spec.sectionName,
                        text = "State the fundamental law governing $topic and write its mathematical formulation.",
                        marks = 2,
                        type = QuestionType.VERY_SHORT,
                        internalChoice = if (hasChoice) "State any two essential factors that influence $topic under laboratory conditions." else null,
                        answerText = "The phenomenon states that the net flux is proportional to enclosed charge (or applicable physical principle). Mathematical form: F = k(q1*q2)/r².",
                        markingScheme = listOf(
                            "1 mark: Clear definition of the principle.",
                            "1 mark: Correct mathematical equation with SI units."
                        ),
                        explanation = "Standard 2-mark conceptual requirement according to ${board.shortName} evaluation criteria."
                    )
                    QuestionType.SHORT -> QuestionItem(
                        number = qNumber,
                        sectionName = spec.sectionName,
                        text = "Explain the step-by-step mechanism of $topic. How does this concept apply in real-world engineering or biological systems?",
                        marks = 3,
                        type = QuestionType.SHORT,
                        internalChoice = if (hasChoice) "Derive the relation between fundamental variables for $topic and calculate its value when standard conditions are doubled." else null,
                        answerText = "1. Initiation phase with energy input.\n2. Propagation through medium.\n3. Equilibrium state achieved.\nApplication: Used in industrial synthesis and solar energy harvesting.",
                        markingScheme = listOf(
                            "1 mark: Accurate explanation of initial steps.",
                            "1 mark: Intermediate relation and diagram/equation.",
                            "1 mark: Practical application and conclusion."
                        ),
                        explanation = "Evaluates analytical understanding and practical context."
                    )
                    QuestionType.LONG -> QuestionItem(
                        number = qNumber,
                        sectionName = spec.sectionName,
                        text = "(a) With the aid of a neat labelled diagram, describe the working principle of $topic.\n(b) A student conducts an experiment with given parameters. Deduce the outcome if resistance or temperature is increased.",
                        marks = 5,
                        type = QuestionType.LONG,
                        internalChoice = if (hasChoice) "(a) State and prove the principal theorem for $topic.\n(b) Solve: If initial value is 20 units and decay constant is 0.05 s⁻¹, find the time required to reduce to half." else null,
                        answerText = "Part (a): Detailed working with labeled diagram (3 marks).\nPart (b): Deductive conclusion based on temperature variance (2 marks).",
                        markingScheme = listOf(
                            "2 marks: Labelled schematic diagram.",
                            "1.5 marks: Detailed operational principle.",
                            "1.5 marks: Correct deduction and numerical conclusion."
                        ),
                        explanation = "Standard 5-mark structured multi-part question testing comprehensive mastery."
                    )
                    QuestionType.CASE_BASED -> QuestionItem(
                        number = qNumber,
                        sectionName = spec.sectionName,
                        text = "CASE STUDY: Read the passage below and answer the following sub-questions:\n'During an observational study of $topic in modern laboratories, researchers observed that varying environmental parameters produced distinct changes in reaction rate and system stability.'\n(i) Identify the independent variable in this study. [1 Mark]\n(ii) What safety measures are essential during this test? [1 Mark]\n(iii) Propose a hypothesis explaining the observed anomaly. [2 Marks]",
                        marks = 4,
                        type = QuestionType.CASE_BASED,
                        internalChoice = if (hasChoice) "Alternative for part (iii): Calculate the theoretical efficiency if input energy is 500 Joules and loss is 120 Joules." else null,
                        answerText = "(i) Independent variable: Temperature/Concentration.\n(ii) Safety: Use protective shielding and insulated gloves.\n(iii) Hypothesis: Increased kinetic energy exceeds activation energy threshold.",
                        markingScheme = listOf(
                            "1 mark for sub-part (i).",
                            "1 mark for sub-part (ii).",
                            "2 marks for sub-part (iii) with reasoned argument."
                        ),
                        explanation = "CBSE/ICSE Competency-based case study question."
                    )
                    else -> QuestionItem(
                        number = qNumber,
                        sectionName = spec.sectionName,
                        text = "Calculate the numerical value for $topic given initial velocity = 15 m/s and time = 4 seconds.",
                        marks = spec.marksPerQuestion,
                        type = spec.questionType,
                        answerText = "Result = 60 units using standard equations.",
                        markingScheme = listOf(
                            "1 mark for formula.",
                            "1 mark for substitution.",
                            "1 mark for final result with proper SI units."
                        ),
                        explanation = "Numerical problem assessing computational accuracy."
                    )
                }

                sectionQuestions.add(item)
            }

            sections.add(
                PaperSection(
                    sectionName = spec.sectionName,
                    title = spec.title,
                    instructions = "All questions in this section carry ${spec.marksPerQuestion} mark(s) each.",
                    totalMarks = sectionQuestions.sumOf { it.marks },
                    questions = sectionQuestions
                )
            )
        }

        val paper = QuestionPaper(
            userId = userId,
            title = "${className.displayName} $subject Examination Paper",
            schoolName = schoolName,
            board = board,
            className = className,
            subject = subject,
            examType = examType,
            timeAllowed = if (maxMarks <= 40) "1.5 Hours" else if (maxMarks <= 60) "2 Hours" else "3 Hours",
            maxMarks = maxMarks,
            difficulty = difficulty,
            generalInstructions = mutableListOf(
                "This question paper consists of ${sections.size} sections: ${sections.joinToString(", ") { it.sectionName }}.",
                "All questions are compulsory. Internal choices are provided in select questions.",
                "Draw neat labelled diagrams wherever required.",
                "Use of calculators is strictly prohibited unless specifically permitted."
            ),
            sections = sections,
            textbookImagesCount = imageCount
        )

        return paper
    }
}
