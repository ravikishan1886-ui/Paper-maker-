package com.example.model

enum class Board(val displayName: String, val shortName: String) {
    CBSE("Central Board of Secondary Education (CBSE)", "CBSE"),
    ICSE("Indian Certificate of Secondary Education (ICSE)", "ICSE"),
    PSEB("Punjab School Education Board (PSEB)", "PSEB"),
    STATE_BOARD("State Education Board", "State Board")
}

enum class GradeClass(val displayName: String) {
    CLASS_1("Class 1"),
    CLASS_2("Class 2"),
    CLASS_3("Class 3"),
    CLASS_4("Class 4"),
    CLASS_5("Class 5"),
    CLASS_6("Class 6"),
    CLASS_7("Class 7"),
    CLASS_8("Class 8"),
    CLASS_9("Class 9"),
    CLASS_10("Class 10"),
    CLASS_11("Class 11"),
    CLASS_12("Class 12")
}

enum class ExamType(val displayName: String) {
    UNIT_TEST("Unit Test"),
    CHAPTER_TEST("Chapter Test"),
    HALF_YEARLY("Half-Yearly Examination"),
    ANNUAL("Annual Examination"),
    PRE_BOARD("Pre-Board Examination"),
    PRACTICE_PAPER("Practice Paper")
}

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MODERATE("Moderate"),
    DIFFICULT("Difficult"),
    MIXED("Mixed (Board Pattern)")
}

enum class QuestionType(val displayName: String, val typicalMarks: Int) {
    MCQ("Multiple Choice (MCQ)", 1),
    FILL_IN_BLANKS("Fill in the Blanks", 1),
    TRUE_FALSE("True or False", 1),
    VERY_SHORT("Very Short Answer (VSA)", 2),
    SHORT("Short Answer (SA)", 3),
    LONG("Long Answer (LA)", 5),
    CASE_BASED("Case-Based / Passage", 4),
    ASSERTION_REASON("Assertion & Reasoning", 1),
    NUMERICAL("Numerical Problem", 3),
    DIAGRAM("Diagram-Based Question", 4)
}

data class QuestionItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var number: Int,
    var sectionName: String,
    var text: String,
    var marks: Int,
    var type: QuestionType,
    var options: List<String> = emptyList(), // For MCQs: A, B, C, D
    var internalChoice: String? = null, // "OR" alternative question
    var answerText: String = "",
    var markingScheme: List<String> = emptyList(),
    var explanation: String = ""
)

data class PaperSection(
    val id: String = java.util.UUID.randomUUID().toString(),
    var sectionName: String, // e.g. "Section A"
    var title: String, // e.g. "Objective & Multiple Choice Questions"
    var instructions: String = "All questions in this section are compulsory.",
    var totalMarks: Int = 0,
    val questions: MutableList<QuestionItem> = mutableListOf()
)

data class QuestionPaper(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    var title: String,
    var schoolName: String = "DAV Public School / Modern Academy",
    var board: Board = Board.CBSE,
    var className: GradeClass = GradeClass.CLASS_10,
    var subject: String = "Science",
    var examType: ExamType = ExamType.ANNUAL,
    var timeAllowed: String = "3 Hours",
    var maxMarks: Int = 80,
    var difficulty: Difficulty = Difficulty.MIXED,
    var generalInstructions: MutableList<String> = mutableListOf(
        "This question paper consists of 5 sections: A, B, C, D, and E.",
        "Section A consists of 20 Multiple Choice Questions carrying 1 mark each.",
        "Section B consists of 6 Very Short Answer type questions carrying 2 marks each.",
        "Section C consists of 7 Short Answer type questions carrying 3 marks each.",
        "Section D consists of 3 Long Answer type questions carrying 5 marks each.",
        "Section E consists of 3 Case-Based units of assessment carrying 4 marks each with sub-parts.",
        "All questions are compulsory. Internal choice has been provided in some questions."
    ),
    val sections: MutableList<PaperSection> = mutableListOf(),
    var textbookImagesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun calculateTotalMarks(): Int {
        return sections.sumOf { section ->
            section.questions.sumOf { it.marks }
        }
    }

    fun getTotalQuestionsCount(): Int {
        return sections.sumOf { it.questions.size }
    }
}

enum class ProviderMode {
    DEFAULT_GROQ,
    PERSONAL_GROQ,
    SMART_SYLLABUS_FALLBACK
}

data class ApiProviderStatus(
    val provider: String = "Groq AI",
    val defaultKeyConfigured: Boolean = false,
    val defaultKeyWorking: Boolean = false,
    val personalKeyConfigured: Boolean = false,
    val personalKeyWorking: Boolean = false,
    val personalKeyMasked: String? = null,
    val activeMode: ProviderMode = ProviderMode.DEFAULT_GROQ,
    val statusMessage: String = "Your request is being processed using the default AI service.",
    val isTesting: Boolean = false,
    val testResultMessage: String? = null
)
