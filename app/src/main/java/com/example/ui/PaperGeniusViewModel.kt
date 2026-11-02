package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthRepository
import com.example.auth.name
import com.example.data.FirestoreRepository
import com.example.data.PaperDatabase
import com.example.data.PaperEntity
import com.example.engine.MarksDistributionEngine
import com.example.model.Board
import com.example.model.Difficulty
import com.example.model.ExamType
import com.example.model.GradeClass
import com.example.model.PaperSection
import com.example.model.QuestionItem
import com.example.model.QuestionPaper
import com.example.model.QuestionType
import com.example.network.GroqRepository
import com.example.security.KeystoreCryptoManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class AppScreen {
    GENERATOR,
    PREVIEW,
    MY_PAPERS,
    FEATURES,
    ABOUT,
    AI_SETTINGS,
    PROFILE
}

class PaperGeniusViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    private val firestoreRepo = FirestoreRepository(application)
    private val groqRepo = GroqRepository(application)
    private val database = PaperDatabase.getDatabase(application)

    val currentUser: StateFlow<FirebaseUser?> = authRepo.currentUser
    val isDemoMode: StateFlow<Boolean> = authRepo.isDemoMode
    val providerStatus = groqRepo.providerStatus

    private val _currentScreen = MutableStateFlow(AppScreen.GENERATOR)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _showAuthModal = MutableStateFlow(false)
    val showAuthModal: StateFlow<Boolean> = _showAuthModal.asStateFlow()

    private val _authIsSignup = MutableStateFlow(false)
    val authIsSignup: StateFlow<Boolean> = _authIsSignup.asStateFlow()

    val showApiKeyDialog = MutableStateFlow(false)
    val selectedQuestionTypes = MutableStateFlow(QuestionType.entries.toSet())

    fun openApiKeyDialog() {
        showApiKeyDialog.value = true
    }

    fun closeApiKeyDialog() {
        showApiKeyDialog.value = false
    }

    fun toggleQuestionType(type: QuestionType) {
        val current = selectedQuestionTypes.value.toMutableSet()
        if (current.contains(type)) {
            if (current.size > 1) {
                current.remove(type)
            }
        } else {
            current.add(type)
        }
        selectedQuestionTypes.value = current
    }

    fun selectAllQuestionTypes() {
        selectedQuestionTypes.value = QuestionType.entries.toSet()
    }

    // Generator Form State
    var schoolName = MutableStateFlow("Delhi Public School / St. Xavier's Academy")
    var selectedBoard = MutableStateFlow(Board.CBSE)
    var selectedClass = MutableStateFlow(GradeClass.CLASS_10)
    var selectedSubject = MutableStateFlow("Science")
    var customSubject = MutableStateFlow("")
    var selectedExamType = MutableStateFlow(ExamType.ANNUAL)
    var selectedMaxMarks = MutableStateFlow(80)
    var selectedDifficulty = MutableStateFlow(Difficulty.MIXED)
    var customTopics = MutableStateFlow("")
    var uploadedImageUris = MutableStateFlow<List<Uri>>(emptyList())

    // Generation Process
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStatus = MutableStateFlow("")
    val generationStatus: StateFlow<String> = _generationStatus.asStateFlow()

    // Active Question Paper & Preview
    private val _activePaper = MutableStateFlow<QuestionPaper?>(null)
    val activePaper: StateFlow<QuestionPaper?> = _activePaper.asStateFlow()

    private val _showAnswerKey = MutableStateFlow(false)
    val showAnswerKey: StateFlow<Boolean> = _showAnswerKey.asStateFlow()

    private val _editingQuestion = MutableStateFlow<QuestionItem?>(null)
    val editingQuestion: StateFlow<QuestionItem?> = _editingQuestion.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Saved Papers
    private val _savedPapers = MutableStateFlow<List<PaperEntity>>(emptyList())
    val savedPapers: StateFlow<List<PaperEntity>> = _savedPapers.asStateFlow()

    var searchQuery = MutableStateFlow("")

    init {
        val initialUid = currentUser.value?.uid ?: "local_active_user"
        viewModelScope.launch {
            groqRepo.loadUserApiKey(initialUid)
        }
        viewModelScope.launch {
            currentUser.collect { user ->
                val uid = user?.uid ?: "local_active_user"
                groqRepo.loadUserApiKey(uid)
                if (user != null && !isDemoMode.value) {
                    firestoreRepo.syncUserProfile(user.uid, user.displayName ?: "Educator", user.email ?: "")
                    loadUserSavedPapers(user.uid)
                } else {
                    _savedPapers.value = emptyList()
                }
            }
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openLogin() {
        _authIsSignup.value = false
        _showAuthModal.value = true
    }

    fun openSignup() {
        _authIsSignup.value = true
        _showAuthModal.value = true
    }

    fun closeAuthModal() {
        _showAuthModal.value = false
    }

    fun toggleAuthMode() {
        _authIsSignup.value = !_authIsSignup.value
    }

    fun signInWithGoogle(context: Context, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = authRepo.signInWithGoogle(context)
            if (res.isSuccess) {
                val user = res.getOrNull()
                if (user != null) {
                    firestoreRepo.syncUserProfile(user.uid, user.name, user.email ?: "")
                    groqRepo.loadUserApiKey(user.uid)
                    loadUserSavedPapers(user.uid)
                }
                _showAuthModal.value = false
                _userMessage.value = "Welcome back, ${user?.name ?: "Educator"}! Synced with Cloud Firestore."
                onComplete(true, "Success")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Google Sign-In failed"
                onComplete(false, err)
            }
        }
    }

    fun exploreAsGuest() {
        authRepo.startDemoMode()
        _showAuthModal.value = false
        _userMessage.value = "Guest Demo Mode enabled! You can explore and generate papers freely."
    }

    fun logout() {
        authRepo.logout()
        _activePaper.value = null
        _currentScreen.value = AppScreen.GENERATOR
        _userMessage.value = "Logged out successfully."
    }

    fun addUploadedImages(newUris: List<Uri>) {
        val current = uploadedImageUris.value.toMutableList()
        current.addAll(newUris)
        uploadedImageUris.value = current
    }

    fun removeUploadedImage(index: Int) {
        val current = uploadedImageUris.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            uploadedImageUris.value = current
        }
    }

    fun clearUploadedImages() {
        uploadedImageUris.value = emptyList()
    }

    fun generatePaper() {
        val user = currentUser.value
        val userId = user?.uid ?: "local_active_user"

        _isGenerating.value = true
        _generationStatus.value = if (uploadedImageUris.value.isNotEmpty())
            "Analyzing textbook photograph(s) using Groq Vision AI..."
        else
            "Structuring curriculum sections and blueprints..."

        viewModelScope.launch {
            val subjectName = if (customSubject.value.isNotBlank()) customSubject.value else selectedSubject.value

            val (generatedPaper, notifyMessage) = groqRepo.generateQuestionPaper(
                userId = userId,
                schoolName = schoolName.value,
                board = selectedBoard.value,
                className = selectedClass.value,
                subject = subjectName,
                examType = selectedExamType.value,
                maxMarks = selectedMaxMarks.value,
                difficulty = selectedDifficulty.value,
                imageUris = uploadedImageUris.value,
                customTopics = customTopics.value,
                selectedTypes = selectedQuestionTypes.value
            )

            _activePaper.value = generatedPaper
            _isGenerating.value = false
            _currentScreen.value = AppScreen.PREVIEW
            _userMessage.value = notifyMessage
        }
    }

    fun toggleAnswerKeyView() {
        _showAnswerKey.value = !_showAnswerKey.value
    }

    fun setEditingQuestion(q: QuestionItem?) {
        _editingQuestion.value = q
    }

    fun saveEditedQuestion(updated: QuestionItem) {
        val paper = _activePaper.value ?: return
        paper.sections.forEach { section ->
            val idx = section.questions.indexOfFirst { it.id == updated.id }
            if (idx != -1) {
                section.questions[idx] = updated
            }
        }
        MarksDistributionEngine.recalculateAndValidatePaper(paper)
        _activePaper.value = paper.copy()
        _editingQuestion.value = null
        _userMessage.value = "Question #${updated.number} updated. Marks re-validated."
    }

    fun deleteQuestion(questionId: String) {
        val paper = _activePaper.value ?: return
        paper.sections.forEach { section ->
            section.questions.removeAll { it.id == questionId }
        }
        MarksDistributionEngine.recalculateAndValidatePaper(paper)
        _activePaper.value = paper.copy()
        _userMessage.value = "Question removed. Marks recalculated."
    }

    fun addQuestionToSection(sectionName: String, newItem: QuestionItem) {
        val paper = _activePaper.value ?: return
        val section = paper.sections.firstOrNull { it.sectionName == sectionName }
        if (section != null) {
            section.questions.add(newItem)
            MarksDistributionEngine.recalculateAndValidatePaper(paper)
            _activePaper.value = paper.copy()
            _userMessage.value = "New question added to $sectionName."
        }
    }

    fun regenerateSingleQuestion(q: QuestionItem) {
        val paper = _activePaper.value ?: return
        val subjectName = paper.subject
        val newText = when (q.type) {
            QuestionType.MCQ -> "Which principle governs the behavior of $subjectName systems when temperature and pressure conditions are doubled?"
            QuestionType.VERY_SHORT -> "Define the fundamental theorem of $subjectName and state its SI unit."
            QuestionType.SHORT -> "Differentiate between the primary mechanisms of $subjectName with suitable examples."
            QuestionType.LONG -> "(a) Derive the foundational expression for $subjectName.\n(b) Illustrate with a comprehensive schematic diagram."
            QuestionType.ASSERTION_REASON -> "Assertion (A): $subjectName undergoes predictable transformation under standard state.\nReason (R): Entropy increases in all spontaneous thermodynamic equilibria."
            QuestionType.NUMERICAL -> "Calculate the magnitude of force and resulting kinetic equilibrium in a $subjectName system with mass 12.5 kg accelerating at 3.4 m/s²."
            QuestionType.DIAGRAM -> "Refer to the schematic below representing the $subjectName circuit:\n(a) Identify components marked X and Y.\n(b) What happens when switch K is closed?"
            else -> "Solve the given analytical problem for $subjectName under standard reference conditions."
        }
        q.text = newText
        q.answerText = "Standard solution aligning with revised question formulation."
        _activePaper.value = paper.copy()
        _userMessage.value = "Question #${q.number} regenerated."
    }

    fun saveCurrentPaperToHistory() {
        val user = currentUser.value
        if (user == null || isDemoMode.value) {
            _userMessage.value = "Please sign in with Google to save papers to your permanent history."
            _showAuthModal.value = true
            return
        }

        val paper = _activePaper.value ?: return
        viewModelScope.launch {
            val entity = serializePaperToEntity(paper, user.uid)
            database.paperDao().insertPaper(entity)
            // Persist to Cloud Firestore under /users/{userId}/papers/{paperId}
            firestoreRepo.savePaper(user.uid, paper, entity.sectionsJson)
            loadUserSavedPapers(user.uid)
            _userMessage.value = "Paper '${paper.title}' saved to My Papers & synced to Cloud Firestore!"
        }
    }

    private fun loadUserSavedPapers(userId: String) {
        viewModelScope.launch {
            database.paperDao().getPapersByUser(userId).collect { list ->
                _savedPapers.value = list
            }
        }
    }

    fun openSavedPaper(entity: PaperEntity) {
        val paper = deserializeEntityToPaper(entity)
        _activePaper.value = paper
        _currentScreen.value = AppScreen.PREVIEW
        _userMessage.value = "Loaded '${paper.title}'."
    }

    fun duplicateSavedPaper(entity: PaperEntity) {
        val paper = deserializeEntityToPaper(entity)
        val duplicated = paper.copy(
            id = UUID.randomUUID().toString(),
            title = "${paper.title} (Copy)"
        )
        viewModelScope.launch {
            val newEntity = serializePaperToEntity(duplicated, duplicated.userId)
            database.paperDao().insertPaper(newEntity)
            val user = currentUser.value
            if (user != null && !isDemoMode.value) {
                firestoreRepo.savePaper(user.uid, duplicated, newEntity.sectionsJson)
            }
            _userMessage.value = "Paper duplicated!"
        }
    }

    fun deleteSavedPaper(paperId: String) {
        viewModelScope.launch {
            database.paperDao().deletePaper(paperId)
            val user = currentUser.value
            if (user != null && !isDemoMode.value) {
                firestoreRepo.deletePaper(user.uid, paperId)
            }
            _userMessage.value = "Paper deleted."
        }
    }

    // AI API Key Actions
    fun savePersonalApiKey(key: String, onResult: (Boolean, String) -> Unit) {
        val uid = currentUser.value?.uid ?: "local_active_user"
        viewModelScope.launch {
            val res = groqRepo.savePersonalApiKey(uid, key)
            if (res.isSuccess) {
                val user = currentUser.value
                if (user != null && !isDemoMode.value) {
                    val encrypted = KeystoreCryptoManager.encrypt(key.trim())
                    val masked = KeystoreCryptoManager.maskApiKey(key.trim())
                    firestoreRepo.savePersonalGroqApiKey(
                        userId = user.uid,
                        displayName = user.name,
                        email = user.email ?: "",
                        encryptedKey = encrypted,
                        maskedKey = masked
                    )
                }
                _userMessage.value = "Personal Groq API key saved and verified! You can now run paper making with your key."
                showApiKeyDialog.value = false
                onResult(true, res.getOrNull() ?: "Success")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Validation failed"
                onResult(false, err)
            }
        }
    }

    fun testPersonalApiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = groqRepo.testGroqKey(key)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Verified")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Test failed")
            }
        }
    }

    fun removePersonalApiKey() {
        val uid = currentUser.value?.uid ?: "local_active_user"
        viewModelScope.launch {
            groqRepo.removePersonalApiKey(uid)
            val user = currentUser.value
            if (user != null && !isDemoMode.value) {
                firestoreRepo.removePersonalGroqApiKey(user.uid)
            }
            _userMessage.value = "Personal Groq API key removed."
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // JSON Serialization Helpers
    private fun serializePaperToEntity(paper: QuestionPaper, userId: String): PaperEntity {
        val instArray = JSONArray()
        paper.generalInstructions.forEach { instArray.put(it) }

        val secArray = JSONArray()
        paper.sections.forEach { sec ->
            val sObj = JSONObject()
            sObj.put("id", sec.id)
            sObj.put("sectionName", sec.sectionName)
            sObj.put("title", sec.title)
            sObj.put("instructions", sec.instructions)
            sObj.put("totalMarks", sec.totalMarks)

            val qArray = JSONArray()
            sec.questions.forEach { q ->
                val qObj = JSONObject()
                qObj.put("id", q.id)
                qObj.put("number", q.number)
                qObj.put("sectionName", q.sectionName)
                qObj.put("text", q.text)
                qObj.put("marks", q.marks)
                qObj.put("type", q.type.name)

                val optArray = JSONArray()
                q.options.forEach { optArray.put(it) }
                qObj.put("options", optArray)

                qObj.put("internalChoice", q.internalChoice ?: JSONObject.NULL)
                qObj.put("answerText", q.answerText)

                val mArray = JSONArray()
                q.markingScheme.forEach { mArray.put(it) }
                qObj.put("markingScheme", mArray)

                qObj.put("explanation", q.explanation)
                qArray.put(qObj)
            }
            sObj.put("questions", qArray)
            secArray.put(sObj)
        }

        return PaperEntity(
            id = paper.id,
            userId = userId,
            title = paper.title,
            schoolName = paper.schoolName,
            boardName = paper.board.name,
            className = paper.className.name,
            subject = paper.subject,
            examType = paper.examType.name,
            timeAllowed = paper.timeAllowed,
            maxMarks = paper.maxMarks,
            difficulty = paper.difficulty.name,
            instructionsJson = instArray.toString(),
            sectionsJson = secArray.toString(),
            textbookImagesCount = paper.textbookImagesCount,
            createdAt = paper.createdAt
        )
    }

    private fun deserializeEntityToPaper(entity: PaperEntity): QuestionPaper {
        val instructions = mutableListOf<String>()
        try {
            val iArray = JSONArray(entity.instructionsJson)
            for (i in 0 until iArray.length()) {
                instructions.add(iArray.getString(i))
            }
        } catch (_: Exception) {}

        val sections = mutableListOf<PaperSection>()
        try {
            val sArray = JSONArray(entity.sectionsJson)
            for (s in 0 until sArray.length()) {
                val sObj = sArray.getJSONObject(s)
                val secName = sObj.optString("sectionName", "Section")
                val title = sObj.optString("title", "Questions")
                val instructionsText = sObj.optString("instructions", "")
                val questions = mutableListOf<QuestionItem>()

                val qArray = sObj.optJSONArray("questions")
                if (qArray != null) {
                    for (q in 0 until qArray.length()) {
                        val qObj = qArray.getJSONObject(q)
                        val opts = mutableListOf<String>()
                        val oArray = qObj.optJSONArray("options")
                        if (oArray != null) {
                            for (o in 0 until oArray.length()) {
                                opts.add(oArray.getString(o))
                            }
                        }

                        val marking = mutableListOf<String>()
                        val mArray = qObj.optJSONArray("markingScheme")
                        if (mArray != null) {
                            for (m in 0 until mArray.length()) {
                                marking.add(mArray.getString(m))
                            }
                        }

                        val qType = QuestionType.entries.firstOrNull { it.name == qObj.optString("type") }
                            ?: QuestionType.MCQ

                        questions.add(
                            QuestionItem(
                                id = qObj.optString("id", UUID.randomUUID().toString()),
                                number = qObj.optInt("number", q + 1),
                                sectionName = secName,
                                text = qObj.optString("text", ""),
                                marks = qObj.optInt("marks", 1),
                                type = qType,
                                options = opts,
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
                        id = sObj.optString("id", UUID.randomUUID().toString()),
                        sectionName = secName,
                        title = title,
                        instructions = instructionsText,
                        totalMarks = questions.sumOf { it.marks },
                        questions = questions
                    )
                )
            }
        } catch (_: Exception) {}

        val board = Board.entries.firstOrNull { it.name == entity.boardName } ?: Board.CBSE
        val className = GradeClass.entries.firstOrNull { it.name == entity.className } ?: GradeClass.CLASS_10
        val examType = ExamType.entries.firstOrNull { it.name == entity.examType } ?: ExamType.ANNUAL
        val difficulty = Difficulty.entries.firstOrNull { it.name == entity.difficulty } ?: Difficulty.MIXED

        return QuestionPaper(
            id = entity.id,
            userId = entity.userId,
            title = entity.title,
            schoolName = entity.schoolName,
            board = board,
            className = className,
            subject = entity.subject,
            examType = examType,
            timeAllowed = entity.timeAllowed,
            maxMarks = entity.maxMarks,
            difficulty = difficulty,
            generalInstructions = instructions,
            sections = sections,
            textbookImagesCount = entity.textbookImagesCount,
            createdAt = entity.createdAt
        )
    }
}
