package com.example.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.QuestionPaper
import com.example.model.QuestionType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfExportManager {

    private const val PAGE_WIDTH = 595 // A4 standard point width
    private const val PAGE_HEIGHT = 842 // A4 standard point height
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    fun createPdf(context: Context, paper: QuestionPaper, isAnswerKey: Boolean): File {
        val document = PdfDocument()
        val titleText = if (isAnswerKey) "Answer_Key_${paper.title}" else "Question_Paper_${paper.title}"
        val sanitized = titleText.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(context.cacheDir, "$sanitized.pdf")

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintNormal = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintSmall = Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
            isAntiAlias = true
        }

        val paintDivider = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = MARGIN + 10f

        fun checkPageBreak(requiredHeight: Float) {
            if (y + requiredHeight > PAGE_HEIGHT - MARGIN) {
                // Draw footer page number
                canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, paintSmall.apply { textAlign = Paint.Align.CENTER })
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 10f
            }
        }

        // Header Section
        val school = paper.schoolName.ifBlank { "PAPERGENIUS ACADEMY" }.uppercase()
        canvas.drawText(school, PAGE_WIDTH / 2f, y, paintTitle)
        y += 18f

        val examLine = "${paper.examType.displayName.uppercase()} — SESSION 2026-27"
        canvas.drawText(examLine, PAGE_WIDTH / 2f, y, paintSubtitle)
        y += 14f

        val metaLine = "BOARD: ${paper.board.shortName}  |  CLASS: ${paper.className.displayName}  |  SUBJECT: ${paper.subject.uppercase()}"
        canvas.drawText(metaLine, PAGE_WIDTH / 2f, y, paintSubtitle)
        y += 16f

        if (isAnswerKey) {
            val badgePaint = Paint().apply {
                color = Color.rgb(124, 58, 237)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("TEACHER'S EVALUATION SCHEME & ANSWER KEY", PAGE_WIDTH / 2f, y, badgePaint)
            y += 18f
        }

        // Two-column sub-header
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintDivider)
        y += 14f
        canvas.drawText("Time Allowed: ${paper.timeAllowed}", MARGIN, y, paintBold)
        val maxMarksText = "Maximum Marks: ${paper.maxMarks}"
        canvas.drawText(maxMarksText, PAGE_WIDTH - MARGIN - paintBold.measureText(maxMarksText), y, paintBold)
        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintDivider)
        y += 18f

        // General Instructions (Only on question paper)
        if (!isAnswerKey && paper.generalInstructions.isNotEmpty()) {
            canvas.drawText("General Instructions:", MARGIN, y, paintBold)
            y += 14f
            paper.generalInstructions.forEachIndexed { idx, inst ->
                val lines = wrapText("(${idx + 1}) $inst", CONTENT_WIDTH, paintNormal)
                checkPageBreak(lines.size * 12f)
                lines.forEach { line ->
                    canvas.drawText(line, MARGIN + 8f, y, paintNormal)
                    y += 12f
                }
            }
            y += 8f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintDivider)
            y += 16f
        }

        // Sections and Questions
        paper.sections.forEach { section ->
            checkPageBreak(30f)
            canvas.drawText("${section.sectionName.uppercase()}: ${section.title.uppercase()} [${section.totalMarks} Marks]", MARGIN, y, paintBold)
            y += 12f
            if (section.instructions.isNotBlank()) {
                canvas.drawText(section.instructions, MARGIN, y, paintSmall)
                y += 14f
            }

            section.questions.forEach { q ->
                val qHeader = "Q${q.number}. "
                val marksText = "[${q.marks}]"
                val qText = qHeader + q.text

                val lines = wrapText(qText, CONTENT_WIDTH - 30f, paintNormal)
                checkPageBreak(lines.size * 13f + 40f)

                lines.forEachIndexed { i, line ->
                    canvas.drawText(line, MARGIN, y, paintNormal)
                    if (i == 0) {
                        canvas.drawText(marksText, PAGE_WIDTH - MARGIN - paintBold.measureText(marksText), y, paintBold)
                    }
                    y += 13f
                }

                // MCQ Options
                if (q.type == QuestionType.MCQ && q.options.isNotEmpty()) {
                    q.options.forEach { opt ->
                        checkPageBreak(12f)
                        canvas.drawText("   $opt", MARGIN + 10f, y, paintNormal)
                        y += 12f
                    }
                }

                // Internal Choice
                if (!q.internalChoice.isNullOrBlank()) {
                    checkPageBreak(28f)
                    y += 2f
                    canvas.drawText("OR", PAGE_WIDTH / 2f, y, paintBold.apply { textAlign = Paint.Align.CENTER })
                    y += 12f
                    val orLines = wrapText(q.internalChoice ?: "", CONTENT_WIDTH - 20f, paintNormal)
                    orLines.forEach { orLine ->
                        canvas.drawText(orLine, MARGIN + 10f, y, paintNormal.apply { textAlign = Paint.Align.LEFT })
                        y += 12f
                    }
                }

                // Answer Key Content (Only shown in Answer Key PDF)
                if (isAnswerKey) {
                    checkPageBreak(35f)
                    val ansBoxPaint = Paint().apply {
                        color = Color.rgb(243, 244, 246)
                    }
                    val ansTextPaint = Paint().apply {
                        color = Color.rgb(79, 70, 229)
                        textSize = 9.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }

                    y += 4f
                    val ansLines = wrapText("Answer: ${q.answerText.ifBlank { "Solution as per syllabus standard." }}", CONTENT_WIDTH - 20f, ansTextPaint)
                    ansLines.forEach { aLine ->
                        canvas.drawText(aLine, MARGIN + 12f, y, ansTextPaint)
                        y += 13f
                    }

                    if (q.markingScheme.isNotEmpty()) {
                        q.markingScheme.forEach { step ->
                            val sLines = wrapText("• Marking: $step", CONTENT_WIDTH - 25f, paintSmall)
                            sLines.forEach { sLine ->
                                canvas.drawText(sLine, MARGIN + 18f, y, paintSmall)
                                y += 11f
                            }
                        }
                    }

                    if (q.explanation.isNotBlank()) {
                        val expLines = wrapText("Explanation: ${q.explanation}", CONTENT_WIDTH - 25f, paintSmall)
                        expLines.forEach { expLine ->
                            canvas.drawText(expLine, MARGIN + 18f, y, paintSmall)
                            y += 11f
                        }
                    }
                }

                y += 10f
            }
            y += 8f
        }

        // Draw final page footer
        canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, paintSmall.apply { textAlign = Paint.Align.CENTER })
        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        paragraphs.forEach { para ->
            val words = para.split(" ")
            var currentLine = ""
            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    currentLine = candidate
                } else {
                    if (currentLine.isNotEmpty()) result.add(currentLine)
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) result.add(currentLine)
        }
        return if (result.isEmpty()) listOf(text) else result
    }

    fun printOrSavePdf(context: Context, paper: QuestionPaper, isAnswerKey: Boolean) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            ?: run {
                Toast.makeText(context, "Print service unavailable", Toast.LENGTH_SHORT).show()
                return
            }

        val jobName = if (isAnswerKey) "PaperGenius_Answer_Key_${paper.title}" else "PaperGenius_Question_Paper_${paper.title}"

        val adapter = object : PrintDocumentAdapter() {
            private var tempFile: File? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                tempFile = createPdf(context, paper, isAnswerKey)
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                val file = tempFile
                if (file == null || destination == null) {
                    callback?.onWriteFailed("File generation error")
                    return
                }

                try {
                    FileInputStream(file).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            val buf = ByteArray(1024)
                            var bytesRead: Int
                            while (input.read(buf).also { bytesRead = it } > 0) {
                                output.write(buf, 0, bytesRead)
                            }
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }

    fun formatAsPlainText(paper: QuestionPaper, isAnswerKey: Boolean): String {
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("${paper.schoolName.ifBlank { "PAPERGENIUS ACADEMY" }}\n")
        sb.append("${paper.examType.displayName.uppercase()} — ${paper.board.shortName}\n")
        sb.append("Class: ${paper.className.displayName} | Subject: ${paper.subject}\n")
        sb.append("Time Allowed: ${paper.timeAllowed} | Max Marks: ${paper.maxMarks}\n")
        sb.append("=========================================\n\n")

        if (isAnswerKey) {
            sb.append("--- TEACHER ANSWER KEY & MARKING SCHEME ---\n\n")
        } else {
            sb.append("GENERAL INSTRUCTIONS:\n")
            paper.generalInstructions.forEachIndexed { i, inst ->
                sb.append("${i + 1}. $inst\n")
            }
            sb.append("\n")
        }

        paper.sections.forEach { section ->
            sb.append("${section.sectionName}: ${section.title} [${section.totalMarks} Marks]\n")
            if (section.instructions.isNotBlank()) {
                sb.append("Note: ${section.instructions}\n")
            }
            sb.append("-----------------------------------------\n")

            section.questions.forEach { q ->
                sb.append("Q${q.number}. ${q.text}  [${q.marks} Mark${if (q.marks > 1) "s" else ""}]\n")
                if (q.type == QuestionType.MCQ && q.options.isNotEmpty()) {
                    q.options.forEach { opt ->
                        sb.append("    $opt\n")
                    }
                }
                if (!q.internalChoice.isNullOrBlank()) {
                    sb.append("    OR\n    ${q.internalChoice}\n")
                }

                if (isAnswerKey) {
                    sb.append("    -> Answer: ${q.answerText}\n")
                    if (q.markingScheme.isNotEmpty()) {
                        q.markingScheme.forEach { step ->
                            sb.append("       • Marking: $step\n")
                        }
                    }
                    if (q.explanation.isNotBlank()) {
                        sb.append("       • Explanation: ${q.explanation}\n")
                    }
                }
                sb.append("\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    fun copyPaperToClipboard(context: Context, paper: QuestionPaper, isAnswerKey: Boolean) {
        val text = formatAsPlainText(paper, isAnswerKey)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            if (isAnswerKey) "Answer Key - ${paper.title}" else "Question Paper - ${paper.title}",
            text
        )
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
