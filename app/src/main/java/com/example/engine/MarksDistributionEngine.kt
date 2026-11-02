package com.example.engine

import com.example.model.Board
import com.example.model.Difficulty
import com.example.model.ExamType
import com.example.model.GradeClass
import com.example.model.PaperSection
import com.example.model.QuestionItem
import com.example.model.QuestionPaper
import com.example.model.QuestionType

object MarksDistributionEngine {

    data class MarksBlueprint(
        val totalMarks: Int,
        val sectionBlueprints: List<SectionSpec>
    )

    data class SectionSpec(
        val sectionName: String,
        val title: String,
        val questionType: QuestionType,
        val marksPerQuestion: Int,
        val count: Int,
        val internalChoiceCount: Int = 1
    ) {
        val totalMarks: Int get() = count * marksPerQuestion
    }

    fun generateBlueprint(
        maxMarks: Int,
        allowedTypes: Set<QuestionType> = QuestionType.entries.toSet()
    ): MarksBlueprint {
        val standardSections = when (maxMarks) {
            40 -> listOf(
                SectionSpec("Section A", "Objective & Multiple Choice Questions", QuestionType.MCQ, 1, 10, 0),
                SectionSpec("Section B", "Very Short Answer Type Questions", QuestionType.VERY_SHORT, 2, 4, 1),
                SectionSpec("Section C", "Short Answer Type Questions", QuestionType.SHORT, 3, 4, 1),
                SectionSpec("Section D", "Long Answer Type Questions", QuestionType.LONG, 5, 2, 1)
            )
            50 -> listOf(
                SectionSpec("Section A", "Multiple Choice & Objective Questions", QuestionType.MCQ, 1, 12, 0),
                SectionSpec("Section B", "Very Short Answer Questions", QuestionType.VERY_SHORT, 2, 5, 1),
                SectionSpec("Section C", "Short Answer Questions", QuestionType.SHORT, 3, 6, 2),
                SectionSpec("Section D", "Long Answer Questions", QuestionType.LONG, 5, 2, 1)
            )
            60 -> listOf(
                SectionSpec("Section A", "Multiple Choice & Assertion-Reasoning", QuestionType.MCQ, 1, 15, 0),
                SectionSpec("Section B", "Very Short Answer Questions", QuestionType.VERY_SHORT, 2, 6, 1),
                SectionSpec("Section C", "Short Answer Questions", QuestionType.SHORT, 3, 6, 2),
                SectionSpec("Section D", "Long Answer Questions", QuestionType.LONG, 5, 3, 1)
            )
            70 -> listOf(
                SectionSpec("Section A", "Objective Type Questions & Assertion-Reasoning", QuestionType.MCQ, 1, 16, 0),
                SectionSpec("Section B", "Very Short Answer Questions", QuestionType.VERY_SHORT, 2, 5, 1),
                SectionSpec("Section C", "Short Answer Questions", QuestionType.SHORT, 3, 7, 2),
                SectionSpec("Section D", "Case-Based / Integrated Assessment", QuestionType.CASE_BASED, 4, 2, 1),
                SectionSpec("Section E", "Long Answer Questions", QuestionType.LONG, 5, 3, 1)
            )
            80 -> listOf(
                SectionSpec("Section A", "Multiple Choice Questions & Assertion-Reasoning", QuestionType.MCQ, 1, 20, 0),
                SectionSpec("Section B", "Very Short Answer Type Questions", QuestionType.VERY_SHORT, 2, 6, 2),
                SectionSpec("Section C", "Short Answer Type Questions", QuestionType.SHORT, 3, 7, 2),
                SectionSpec("Section D", "Long Answer Type Questions", QuestionType.LONG, 5, 3, 2),
                SectionSpec("Section E", "Case-Based / Data-Based Integrated Questions", QuestionType.CASE_BASED, 4, 3, 1)
            )
            90 -> listOf(
                SectionSpec("Section A", "Objective & Multiple Choice Questions", QuestionType.MCQ, 1, 25, 0),
                SectionSpec("Section B", "Very Short Answer Questions", QuestionType.VERY_SHORT, 2, 7, 2),
                SectionSpec("Section C", "Short Answer Questions", QuestionType.SHORT, 3, 7, 2),
                SectionSpec("Section D", "Case-Based / Applied Questions", QuestionType.CASE_BASED, 4, 3, 1),
                SectionSpec("Section E", "Long Answer Questions", QuestionType.LONG, 5, 3, 2),
                SectionSpec("Section F", "Higher Order Thinking (HOTS) Questions", QuestionType.NUMERICAL, 3, 1, 0)
            )
            100 -> listOf(
                SectionSpec("Section A", "Multiple Choice & Objective Questions", QuestionType.MCQ, 1, 20, 0),
                SectionSpec("Section B", "Very Short Answer Questions", QuestionType.VERY_SHORT, 2, 8, 2),
                SectionSpec("Section C", "Short Answer Questions", QuestionType.SHORT, 3, 8, 2),
                SectionSpec("Section D", "Case-Based / Passage Questions", QuestionType.CASE_BASED, 4, 5, 1),
                SectionSpec("Section E", "Long Answer Type Questions", QuestionType.LONG, 5, 4, 2)
            )
            else -> {
                // Custom marks allocation logic (e.g. 45, 65, 75, etc.)
                val mcqCount = (maxMarks * 0.25).toInt().coerceAtLeast(5)
                val vsaCount = ((maxMarks * 0.20) / 2).toInt().coerceAtLeast(2)
                val saCount = ((maxMarks * 0.30) / 3).toInt().coerceAtLeast(2)
                val allocatedSoFar = (mcqCount * 1) + (vsaCount * 2) + (saCount * 3)
                val remaining = maxMarks - allocatedSoFar

                val laCount = (remaining / 5).coerceAtLeast(1)
                val finalRemainder = remaining - (laCount * 5)

                val specs = mutableListOf(
                    SectionSpec("Section A", "Objective & Multiple Choice Questions", QuestionType.MCQ, 1, mcqCount + finalRemainder, 0),
                    SectionSpec("Section B", "Very Short Answer Questions", QuestionType.VERY_SHORT, 2, vsaCount, 1),
                    SectionSpec("Section C", "Short Answer Questions", QuestionType.SHORT, 3, saCount, 1),
                    SectionSpec("Section D", "Long Answer Questions", QuestionType.LONG, 5, laCount, 1)
                )
                specs
            }
        }

        val effectiveSections = if (allowedTypes.size < QuestionType.entries.size && allowedTypes.isNotEmpty()) {
            val filtered = standardSections.filter { spec -> spec.questionType in allowedTypes }
            if (filtered.isNotEmpty()) {
                val currentTotal = filtered.sumOf { it.totalMarks }
                if (currentTotal == maxMarks) {
                    filtered
                } else {
                    val diff = maxMarks - currentTotal
                    filtered.mapIndexed { idx, spec ->
                        if (idx == 0) {
                            val newCount = (spec.count + diff / spec.marksPerQuestion).coerceAtLeast(1)
                            spec.copy(count = newCount)
                        } else spec
                    }
                }
            } else {
                standardSections
            }
        } else {
            standardSections
        }

        return MarksBlueprint(maxMarks, effectiveSections)
    }

    fun recalculateAndValidatePaper(paper: QuestionPaper): ValidationResult {
        var runningNumber = 1
        var totalPaperMarks = 0
        val errors = mutableListOf<String>()

        paper.sections.forEach { section ->
            var sectionTotal = 0
            section.questions.forEach { q ->
                q.number = runningNumber++
                q.sectionName = section.sectionName
                if (q.marks <= 0) {
                    errors.add("Question #${q.number} has invalid marks: ${q.marks}. Marks must be > 0.")
                    q.marks = 1
                }
                sectionTotal += q.marks
            }
            section.totalMarks = sectionTotal
            totalPaperMarks += sectionTotal
        }

        val isValid = (totalPaperMarks == paper.maxMarks) && errors.isEmpty()
        val difference = totalPaperMarks - paper.maxMarks

        return ValidationResult(
            isValid = isValid,
            actualTotalMarks = totalPaperMarks,
            targetMaxMarks = paper.maxMarks,
            difference = difference,
            errors = errors
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val actualTotalMarks: Int,
        val targetMaxMarks: Int,
        val difference: Int,
        val errors: List<String>
    ) {
        val summaryMessage: String
            get() = when {
                isValid -> "Marks distribution is verified: Exact $actualTotalMarks / $targetMaxMarks marks."
                difference > 0 -> "Total marks ($actualTotalMarks) exceed target ($targetMaxMarks) by +$difference marks."
                else -> "Total marks ($actualTotalMarks) are short of target ($targetMaxMarks) by $difference marks."
            }
    }
}
