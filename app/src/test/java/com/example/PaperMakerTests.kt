package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.MarksDistributionEngine
import com.example.export.PdfExportManager
import com.example.model.Board
import com.example.model.Difficulty
import com.example.model.ExamType
import com.example.model.GradeClass
import com.example.model.PaperSection
import com.example.model.QuestionItem
import com.example.model.QuestionPaper
import com.example.model.QuestionType
import com.example.network.GroqClient
import com.example.security.KeystoreCryptoManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaperMakerTests {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `test Groq key validation fails gracefully with blank key`() = runBlocking {
        val client = GroqClient()
        val result = client.testApiKey("")
        assertTrue("Blank key must fail", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("blank") == true)
    }

    @Test
    fun `test Keystore AES encryption and decryption`() {
        val testKey = "gsk_test1234567890abcdefghijklmnopqrstuvwxyz"
        val encrypted = KeystoreCryptoManager.encrypt(testKey)
        assertTrue(encrypted.isNotBlank())
        val decrypted = KeystoreCryptoManager.decrypt(encrypted)
        assertEquals("Decrypted key must equal original key", testKey, decrypted)

        val masked = KeystoreCryptoManager.maskApiKey(testKey)
        assertTrue("Masked key must show prefix", masked.startsWith("gsk_"))
        assertTrue("Masked key must show suffix", masked.endsWith("wxyz"))
        assertTrue("Masked key must contain dots", masked.contains("•"))
    }

    @Test
    fun `test PDF generation creates valid file with RAVI KISHAN watermark`() {
        val questions = mutableListOf<QuestionItem>()
        for (i in 1..10) {
            questions.add(
                QuestionItem(
                    number = i,
                    sectionName = "Section A",
                    text = "Sample Question $i testing watermark layout and pagination.",
                    marks = 1,
                    type = QuestionType.MCQ,
                    options = listOf("A) Choice 1", "B) Choice 2", "C) Choice 3", "D) Choice 4"),
                    answerText = "A) Choice 1"
                )
            )
        }

        val section = PaperSection(
            sectionName = "Section A",
            title = "Multiple Choice Questions",
            instructions = "All questions are compulsory.",
            totalMarks = 10,
            questions = questions
        )

        val paper = QuestionPaper(
            userId = "test_user",
            title = "Class 10 Science Annual Exam",
            schoolName = "DPS RK Puram",
            board = Board.CBSE,
            className = GradeClass.CLASS_10,
            subject = "Science",
            examType = ExamType.ANNUAL,
            timeAllowed = "3 Hours",
            maxMarks = 80,
            difficulty = Difficulty.MEDIUM,
            generalInstructions = mutableListOf("Read all questions carefully.", "Attempt all sections."),
            sections = listOf(section)
        )

        val pdfFile = PdfExportManager.createPdf(context, paper, isAnswerKey = false)
        assertNotNull(pdfFile)
        assertTrue("PDF file must exist", pdfFile.exists())
        assertTrue("PDF file size must be greater than 1KB", pdfFile.length() > 1024)

        // Verify watermark constant
        assertEquals("RAVI KISHAN", PdfExportManager.WATERMARK_TEXT)
        assertEquals("Created with Paper-maker", PdfExportManager.FOOTER_APP_TEXT)

        // Verify plain text contains watermark and footer branding
        val plainText = PdfExportManager.formatAsPlainText(paper, isAnswerKey = false)
        assertTrue(plainText.contains("RAVI KISHAN"))
        assertTrue(plainText.contains("Created with Paper-maker"))
    }

    @Test
    fun `test multi-page PDF generation with extensive questions`() {
        val sections = mutableListOf<PaperSection>()
        var qNum = 1

        listOf("Section A" to 15, "Section B" to 10, "Section C" to 8, "Section D" to 5).forEach { (secName, count) ->
            val qList = mutableListOf<QuestionItem>()
            for (i in 1..count) {
                qList.add(
                    QuestionItem(
                        number = qNum++,
                        sectionName = secName,
                        text = "Detailed conceptual examination question #$qNum discussing board curriculum principles in depth with comprehensive context, diagrams, and numerical reasoning steps.",
                        marks = 3,
                        type = QuestionType.SHORT,
                        answerText = "Step-by-step complete solution satisfying CBSE marking scheme standard criteria.",
                        markingScheme = listOf("1 mark for formula", "1 mark for calculation", "1 mark for answer")
                    )
                )
            }
            sections.add(
                PaperSection(
                    sectionName = secName,
                    title = "Structured Questions",
                    instructions = "Answer all questions.",
                    totalMarks = count * 3,
                    questions = qList
                )
            )
        }

        val paper = QuestionPaper(
            userId = "test_user",
            title = "Class 12 Physics Pre-Board Examination",
            schoolName = "St. Xavier's Senior Secondary School",
            board = Board.CBSE,
            className = GradeClass.CLASS_12,
            subject = "Physics",
            examType = ExamType.PRE_BOARD,
            timeAllowed = "3 Hours",
            maxMarks = 70,
            difficulty = Difficulty.HARD,
            generalInstructions = mutableListOf(
                "This paper contains 38 questions in 4 sections.",
                "Draw neat labelled diagrams wherever necessary.",
                "Calculators are not permitted."
            ),
            sections = sections
        )

        // Generate Question Paper PDF
        val qpFile = PdfExportManager.createPdf(context, paper, isAnswerKey = false)
        assertTrue(qpFile.exists())
        assertTrue("Multi-page PDF file size should be substantial", qpFile.length() > 3000)

        // Generate Answer Key PDF
        val akFile = PdfExportManager.createPdf(context, paper, isAnswerKey = true)
        assertTrue(akFile.exists())
        assertTrue(akFile.length() > 3000)
    }

    @Test
    fun `test MarksDistributionEngine arithmetic guarantees`() {
        val blueprint80 = MarksDistributionEngine.generateBlueprint(80)
        assertEquals("Total marks in blueprint must equal requested total", 80, blueprint80.totalMarks)
        val sum80 = blueprint80.sectionBlueprints.sumOf { it.totalMarks }
        assertEquals(80, sum80)

        val blueprint70 = MarksDistributionEngine.generateBlueprint(70)
        assertEquals(70, blueprint70.totalMarks)
        val sum70 = blueprint70.sectionBlueprints.sumOf { it.totalMarks }
        assertEquals(70, sum70)
    }
}
