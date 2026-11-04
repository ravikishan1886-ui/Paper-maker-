package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MarksDistributionEngine
import com.example.export.PdfExportManager
import com.example.model.PaperSection
import com.example.model.QuestionItem
import com.example.model.QuestionPaper
import com.example.model.QuestionType
import com.example.ui.PaperGeniusViewModel
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaperPreviewScreen(
    viewModel: PaperGeniusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paper by viewModel.activePaper.collectAsState()
    val showAnswerKey by viewModel.showAnswerKey.collectAsState()
    val editingQuestion by viewModel.editingQuestion.collectAsState()

    var showAddDialogForSection by remember { mutableStateOf<String?>(null) }

    if (paper == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "No Question Paper Generated Yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                )
                Text(
                    text = "Configure your syllabus options or upload textbook photos to create a new exam paper.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, textAlign = TextAlign.Center)
                )
                Button(
                    onClick = { viewModel.setScreen(com.example.ui.AppScreen.GENERATOR) },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.testTag("go_to_generator_button")
                ) {
                    Text("Go to Generator")
                }
            }
        }
        return
    }

    val currentPaper = paper!!
    val totalMarksAllocated = currentPaper.calculateTotalMarks()
    val isMarksValid = totalMarksAllocated == currentPaper.maxMarks

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("preview_action_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Mode Switch: Student Paper vs Teacher Key
                    TabRow(
                        selectedTabIndex = if (showAnswerKey) 1 else 0,
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = IndigoPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = !showAnswerKey,
                            onClick = { if (showAnswerKey) viewModel.toggleAnswerKeyView() },
                            text = { Text("Student Paper", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = showAnswerKey,
                            onClick = { if (!showAnswerKey) viewModel.toggleAnswerKeyView() },
                            text = { Text("Teacher Answer Key", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }

                    // Action Buttons Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    PdfExportManager.downloadPdf(context, currentPaper, showAnswerKey)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("download_pdf_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showAnswerKey) "Download Key PDF" else "Download PDF", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                PdfExportManager.sharePdf(context, currentPaper, showAnswerKey)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("share_pdf_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                PdfExportManager.printOrSavePdf(context, currentPaper, showAnswerKey)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("print_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                PdfExportManager.copyPaperToClipboard(context, currentPaper, showAnswerKey)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_text_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Text", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.saveCurrentPaperToHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_paper_button")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to My Papers", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Marks Distribution Engine Verification Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("marks_distribution_banner"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMarksValid) EmeraldContainer else Color(0xFFFEF3C7)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isMarksValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isMarksValid) EmeraldSuccess else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isMarksValid)
                                "Marks Engine Verified: Exact $totalMarksAllocated / ${currentPaper.maxMarks} Marks"
                            else
                                "Marks Mismatch: $totalMarksAllocated / ${currentPaper.maxMarks} Marks",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isMarksValid) Color(0xFF065F46) else Color(0xFF92400E)
                            )
                        )
                        Text(
                            text = if (isMarksValid)
                                "All sections deterministic: Section totals match target allocation exactly."
                            else
                                "Question edits altered total marks. Please adjust question marks to equal target.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (isMarksValid) Color(0xFF047857) else Color(0xFFB45309)
                            )
                        )
                    }
                }
            }
        }

        // Formal Examination Paper Sheet (A4 Styled Card)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("exam_paper_sheet"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Personal Watermark Branding Ribbon
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(12.dp))
                            Text(
                                text = "PDF WATERMARK: RAVI KISHAN • CREATED WITH PAPER-MAKER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    }

                    // School Name
                    Text(
                        text = currentPaper.schoolName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDark,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentPaper.examType.displayName.uppercase()} (2026-27)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "BOARD: ${currentPaper.board.shortName}  |  CLASS: ${currentPaper.className.displayName}  |  SUBJECT: ${currentPaper.subject.uppercase()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        ),
                        textAlign = TextAlign.Center
                    )

                    if (showAnswerKey) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PurpleContainer
                        ) {
                            Text(
                                text = "TEACHER ANSWER KEY & DETAILED MARKING SCHEME",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Time & Max Marks row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Time: ${currentPaper.timeAllowed}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Max. Marks: ${currentPaper.maxMarks}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    // General Instructions (Only on question paper)
                    if (!showAnswerKey && currentPaper.generalInstructions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "General Instructions:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                            )
                            currentPaper.generalInstructions.forEachIndexed { idx, inst ->
                                Text(
                                    text = "(${idx + 1}) $inst",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = Color(0xFF334155))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    }
                }
            }
        }

        // Paper Sections and Questions
        currentPaper.sections.forEach { section ->
            item(key = section.id) {
                SectionCard(
                    section = section,
                    showAnswerKey = showAnswerKey,
                    onEditQuestion = { q -> viewModel.setEditingQuestion(q) },
                    onDeleteQuestion = { qId -> viewModel.deleteQuestion(qId) },
                    onRegenerateQuestion = { q -> viewModel.regenerateSingleQuestion(q) },
                    onAddQuestion = { showAddDialogForSection = section.sectionName }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Question Dialog
    if (editingQuestion != null) {
        EditQuestionDialog(
            question = editingQuestion!!,
            onDismiss = { viewModel.setEditingQuestion(null) },
            onSave = { updated -> viewModel.saveEditedQuestion(updated) }
        )
    }

    // Add Question Dialog
    if (showAddDialogForSection != null) {
        AddQuestionDialog(
            sectionName = showAddDialogForSection!!,
            onDismiss = { showAddDialogForSection = null },
            onAdd = { newItem ->
                viewModel.addQuestionToSection(showAddDialogForSection!!, newItem)
                showAddDialogForSection = null
            }
        )
    }
}

@Composable
fun SectionCard(
    section: PaperSection,
    showAnswerKey: Boolean,
    onEditQuestion: (QuestionItem) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onRegenerateQuestion: (QuestionItem) -> Unit,
    onAddQuestion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("section_card_${section.sectionName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${section.sectionName.uppercase()}: ${section.title.uppercase()}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    )
                    if (section.instructions.isNotBlank()) {
                        Text(
                            text = section.instructions,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IndigoContainer
                ) {
                    Text(
                        text = "${section.totalMarks} Marks",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Questions in Section
            section.questions.forEach { q ->
                QuestionRow(
                    question = q,
                    showAnswerKey = showAnswerKey,
                    onEdit = { onEditQuestion(q) },
                    onDelete = { onDeleteQuestion(q.id) },
                    onRegenerate = { onRegenerateQuestion(q) }
                )
                HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)
            }

            // Add question button
            TextButton(
                onClick = onAddQuestion,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Question to ${section.sectionName}", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuestionRow(
    question: QuestionItem,
    showAnswerKey: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Q${question.number}. ${question.text}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = NavyDark,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "[${question.marks}]",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // MCQ Options
        if (question.type == QuestionType.MCQ && question.options.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                question.options.forEach { opt ->
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155), fontSize = 12.sp)
                    )
                }
            }
        }

        // Internal Choice (OR)
        if (!question.internalChoice.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                    )
                    Text(
                        text = question.internalChoice ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                    )
                }
            }
        }

        // Answer Key Panel (if Teacher Answer Key is toggled)
        if (showAnswerKey) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PurpleContainer,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Answer: ${question.answerText.ifBlank { "Solution as per syllabus standard." }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                    )

                    if (question.markingScheme.isNotEmpty()) {
                        Text(
                            text = "Marking Breakdown:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = NavyDark
                            )
                        )
                        question.markingScheme.forEach { step ->
                            Text(
                                text = "• $step",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF475569))
                            )
                        }
                    }

                    if (question.explanation.isNotBlank()) {
                        Text(
                            text = "Explanation: ${question.explanation}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF475569))
                        )
                    }
                }
            }
        }

        // Action Toolbar: Edit, Regenerate, Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Question", tint = IndigoPrimary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Regenerate Question", tint = PurpleAccent, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EditQuestionDialog(
    question: QuestionItem,
    onDismiss: () -> Unit,
    onSave: (QuestionItem) -> Unit
) {
    var text by remember { mutableStateOf(question.text) }
    var marksText by remember { mutableStateOf(question.marks.toString()) }
    var internalChoice by remember { mutableStateOf(question.internalChoice ?: "") }
    var answerText by remember { mutableStateOf(question.answerText) }
    var explanation by remember { mutableStateOf(question.explanation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Question #${question.number}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Question Statement") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = marksText,
                    onValueChange = { marksText = it },
                    label = { Text("Allocated Marks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = internalChoice,
                    onValueChange = { internalChoice = it },
                    label = { Text("Internal Choice (OR alternative question)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Answer Key / Model Solution") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marks = marksText.toIntOrNull() ?: question.marks
                    val updated = question.copy(
                        text = text,
                        marks = marks.coerceAtLeast(1),
                        internalChoice = if (internalChoice.isNotBlank()) internalChoice else null,
                        answerText = answerText,
                        explanation = explanation
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddQuestionDialog(
    sectionName: String,
    onDismiss: () -> Unit,
    onAdd: (QuestionItem) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var marksText by remember { mutableStateOf("1") }
    var answerText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Question to $sectionName") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Question Statement") },
                    placeholder = { Text("e.g. State Ohm's law with mathematical formulation.") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = marksText,
                    onValueChange = { marksText = it },
                    label = { Text("Marks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Answer Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        val marks = marksText.toIntOrNull() ?: 1
                        val item = QuestionItem(
                            number = 999, // Will be renumbered by MarksEngine
                            sectionName = sectionName,
                            text = text,
                            marks = marks.coerceAtLeast(1),
                            type = QuestionType.SHORT,
                            answerText = answerText
                        )
                        onAdd(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
