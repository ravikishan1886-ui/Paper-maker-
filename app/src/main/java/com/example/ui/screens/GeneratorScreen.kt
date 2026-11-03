package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.engine.MarksDistributionEngine
import com.example.model.Board
import com.example.model.Difficulty
import com.example.model.ExamType
import com.example.model.GradeClass
import com.example.model.ProviderMode
import com.example.model.QuestionType
import com.example.ui.AppScreen
import com.example.ui.PaperGeniusViewModel
import com.example.ui.components.PaperGeniusFooter
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: PaperGeniusViewModel,
    modifier: Modifier = Modifier
) {
    val schoolName by viewModel.schoolName.collectAsState()
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val customSubject by viewModel.customSubject.collectAsState()
    val selectedExamType by viewModel.selectedExamType.collectAsState()
    val selectedMaxMarks by viewModel.selectedMaxMarks.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val customTopics by viewModel.customTopics.collectAsState()
    val uploadedImages by viewModel.uploadedImageUris.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStatus by viewModel.generationStatus.collectAsState()
    val providerStatus by viewModel.providerStatus.collectAsState()
    val selectedTypes by viewModel.selectedQuestionTypes.collectAsState()

    // Form options
    var includeInternalChoice by remember { mutableStateOf(true) }
    var includeCompetency by remember { mutableStateOf(true) }
    var separateAnswerKey by remember { mutableStateOf(true) }
    var stepMarkingScheme by remember { mutableStateOf(true) }
    var examTitleHeading by remember { mutableStateOf("Annual Examination 2026-27") }

    // Dropdowns
    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    // Inline Groq Key Editor State
    var showInlineKeyEditor by remember { mutableStateOf(false) }
    var inlineKeyInput by remember { mutableStateOf("") }
    var inlinePasswordVisible by remember { mutableStateOf(false) }
    var inlineTesting by remember { mutableStateOf(false) }
    var inlineFeedback by remember { mutableStateOf<String?>(null) }
    var inlineSuccess by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addUploadedImages(uris)
        }
    }

    val standardSubjects = listOf(
        "Science", "Mathematics", "English", "Social Science",
        "Physics", "Chemistry", "Biology", "Hindi", "Punjabi", "Computer Science"
    )

    val currentBlueprint = remember(selectedMaxMarks, selectedTypes) {
        MarksDistributionEngine.generateBlueprint(selectedMaxMarks, selectedTypes)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // HERO SECTION (Matching Video)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_banner_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Badge Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                        )
                        Text(
                            text = "Turn Textbook Pages Into Exam-Ready Question Papers",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = IndigoPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Large Headline
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Your Question Paper",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDark,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "In Seconds with AI",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = IndigoPrimary,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Subtitle
                Text(
                    text = "Snap photos of your textbook pages with a mobile phone: PaperGenius AI reads key concepts, mathematical formulas, and diagrams to formulate balanced examination papers for CBSE, PSEB, and ICSE boards.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B),
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                // Action Buttons: Generate Question Paper, Explore Features, Sample 80M
                // (NO APK BUTTONS OR ICONS - as instructed by user)
                Button(
                    onClick = { viewModel.generatePaper() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("hero_generate_paper_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Question Paper",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setScreen(AppScreen.FEATURES) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("explore_features_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp), tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore Features", fontSize = 12.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.selectedMaxMarks.value = 80
                            viewModel.selectedDifficulty.value = Difficulty.MIXED
                            viewModel.selectedSubject.value = "Science"
                            viewModel.selectedClass.value = GradeClass.CLASS_10
                            viewModel.generatePaper()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("sample_80m_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sample (80 M)", fontSize = 12.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Highlight chips row
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FeatureHighlightPill("CBSE, PSEB & ICSE")
                    FeatureHighlightPill("40-100 Marks Plan")
                    FeatureHighlightPill("Step-by-Step Solutions")
                    FeatureHighlightPill("A4 PDF & Print Ready")
                }
            }
        }

        // -------------------------------------------------------------
        // DARK BLUE CARD: Cloud Storage & Multi-Device Access (Firebase Powered)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cloud Storage & Multi-Device Access",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF312E81)
                    ) {
                        Text(
                            text = "Firebase Powered",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFA5B4FC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Save your papers across all your devices. Sign in to cloud sync question papers to your permanent educator account.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CloudCheck("Real-time Cloud Sync")
                    CloudCheck("Instant Draft Restore")
                }

                Button(
                    onClick = { viewModel.setScreen(AppScreen.MY_PAPERS) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View My Cloud Papers", fontWeight = FontWeight.Bold)
                }
            }
        }

        // -------------------------------------------------------------
        // USER GROQ API KEY BANNER (Keep key logic & mechanics intact!)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("groq_api_key_banner_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (providerStatus.personalKeyConfigured) EmeraldContainer else Color(0xFFEEF2FF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (providerStatus.personalKeyConfigured) EmeraldSuccess else IndigoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (providerStatus.personalKeyConfigured) "Your Personal Groq Key is Active" else "Use Your Own Groq API Key",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (providerStatus.personalKeyConfigured) Color(0xFF065F46) else NavyDark
                                )
                            )
                            Text(
                                text = if (providerStatus.personalKeyConfigured)
                                    "Ready to generate papers with your personal Groq key (${providerStatus.personalKeyMasked})"
                                else
                                    "Add your Groq API key for zero-wait high-speed generation",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (providerStatus.personalKeyConfigured) Color(0xFF047857) else Color(0xFF475569)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showInlineKeyEditor = !showInlineKeyEditor },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (providerStatus.personalKeyConfigured) Color(0xFF065F46) else IndigoPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (providerStatus.personalKeyConfigured) "Manage" else "+ Add Key",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                AnimatedVisibility(visible = showInlineKeyEditor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Enter your personal Groq API key (gsk_...). It will be encrypted and used to generate your question papers directly.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = Color(0xFF64748B))
                        )

                        OutlinedTextField(
                            value = inlineKeyInput,
                            onValueChange = { inlineKeyInput = it; inlineFeedback = null },
                            label = { Text("Groq API Key (gsk_...)") },
                            placeholder = { Text("gsk_...") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { inlinePasswordVisible = !inlinePasswordVisible }) {
                                    Icon(
                                        imageVector = if (inlinePasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (inlinePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (inlineKeyInput.isNotBlank()) {
                                        inlineTesting = true
                                        viewModel.savePersonalApiKey(inlineKeyInput) { success, msg ->
                                            inlineTesting = false
                                            inlineSuccess = success
                                            inlineFeedback = msg
                                            if (success) {
                                                inlineKeyInput = ""
                                                showInlineKeyEditor = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                enabled = !inlineTesting && inlineKeyInput.isNotBlank(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (inlineTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verifying...")
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save & Apply")
                                }
                            }

                            if (providerStatus.personalKeyConfigured) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.removePersonalApiKey()
                                        showInlineKeyEditor = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Text("Remove Key")
                                }
                            }
                        }

                        if (!inlineFeedback.isNullOrBlank()) {
                            Text(
                                text = inlineFeedback ?: "",
                                color = if (inlineSuccess) Color(0xFF065F46) else Color(0xFFDC2626),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // FORM CARD 1: Education Board & Class
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IndigoContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 14.sp)
                    }
                    Text(
                        text = "Education Board & Class",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                }

                // Education Board
                Text("EDUCATION BOARD *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(Board.CBSE, Board.PSEB, Board.ICSE, Board.STATE_BOARD).forEach { board ->
                        val isSelected = selectedBoard == board
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedBoard.value = board },
                            label = {
                                Text(
                                    board.shortName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("board_chip_${board.name}"),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) IndigoPrimary else Color(0xFF94A3B8),
                                selectedBorderColor = IndigoPrimary,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = Color(0xFF0F172A),
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Class / Standard
                Text("CLASS / STANDARD *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClass.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedTrailingIconColor = IndigoPrimary,
                            unfocusedTrailingIconColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        GradeClass.entries.forEach { grade ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        grade.displayName,
                                        color = Color(0xFF0F172A),
                                        fontWeight = if (grade == selectedClass) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    viewModel.selectedClass.value = grade
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject
                Text("SUBJECT *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubject,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedTrailingIconColor = IndigoPrimary,
                            unfocusedTrailingIconColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        standardSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        sub,
                                        color = Color(0xFF0F172A),
                                        fontWeight = if (sub == selectedSubject) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    viewModel.selectedSubject.value = sub
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Custom subject override
                OutlinedTextField(
                    value = customSubject,
                    onValueChange = { viewModel.customSubject.value = it },
                    label = { Text("+ Enter Custom Subject (Optional)", color = Color(0xFF475569)) },
                    placeholder = { Text("e.g. Environmental Science, Economics", color = Color(0xFF64748B)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Color(0xFF94A3B8),
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = Color(0xFF475569),
                        focusedPlaceholderColor = Color(0xFF64748B),
                        unfocusedPlaceholderColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Exam Type
                Text("EXAM TYPE *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExamType.entries.forEach { type ->
                        val isSelected = selectedExamType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedExamType.value = type },
                            label = {
                                Text(
                                    type.displayName,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) IndigoPrimary else Color(0xFF94A3B8),
                                selectedBorderColor = IndigoPrimary,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = Color(0xFF0F172A),
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // FORM CARD 2: Examination Blueprint & Marks Scheme
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IndigoContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 14.sp)
                    }
                    Text(
                        text = "Examination Blueprint & Marks Scheme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                }

                // Marks Selector
                Text("TOTAL MARKS *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(40, 50, 60, 70, 80, 90, 100).forEach { marks ->
                        val isSelected = selectedMaxMarks == marks
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedMaxMarks.value = marks },
                            label = {
                                Text(
                                    "${marks}M",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            },
                            modifier = Modifier.testTag("marks_chip_$marks"),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) IndigoPrimary else Color(0xFF94A3B8),
                                selectedBorderColor = IndigoPrimary,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = Color(0xFF0F172A),
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Difficulty Level (Easy, Moderate, Difficult, Mixed)
                Text("DIFFICULTY LEVEL *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(Difficulty.EASY, Difficulty.MODERATE, Difficulty.DIFFICULT, Difficulty.MIXED).forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedDifficulty.value = diff },
                            label = {
                                Text(
                                    text = if (diff == Difficulty.MIXED) "Mixed (Recommended)" else diff.displayName,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PurpleAccent else Color(0xFF94A3B8),
                                selectedBorderColor = PurpleAccent,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = Color(0xFF0F172A),
                                selectedContainerColor = PurpleAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Question Types Included (Assertion & reasoning, Numerical, Diagram, etc.)
                Text("QUESTION TYPES INCLUDED *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        QuestionType.MCQ,
                        QuestionType.ASSERTION_REASON,
                        QuestionType.NUMERICAL,
                        QuestionType.DIAGRAM,
                        QuestionType.VERY_SHORT,
                        QuestionType.SHORT,
                        QuestionType.LONG,
                        QuestionType.CASE_BASED
                    ).forEach { type ->
                        val isSelected = selectedTypes.contains(type)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleQuestionType(type) },
                            label = {
                                Text(
                                    type.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) IndigoPrimary else Color(0xFF94A3B8),
                                selectedBorderColor = IndigoPrimary,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = Color(0xFF0F172A),
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Assessment Features & Solutions Checkboxes
                Text("ASSESSMENT FEATURES & SOLUTIONS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    CheckboxRow("Include Internal Choice (\"OR\" questions)", includeInternalChoice) { includeInternalChoice = it }
                    CheckboxRow("Include Competency & Case-Based", includeCompetency) { includeCompetency = it }
                    CheckboxRow("Generate Separate Answer Key", separateAnswerKey) { separateAnswerKey = it }
                    CheckboxRow("Step-by-Step Marking Scheme", stepMarkingScheme) { stepMarkingScheme = it }
                }

                // Optional inputs
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { viewModel.schoolName.value = it },
                    label = { Text("School / Institute Name (Optional)", color = Color(0xFF475569)) },
                    placeholder = { Text("e.g. St. Xavier's Senior Secondary School", color = Color(0xFF64748B)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Color(0xFF94A3B8),
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = Color(0xFF475569),
                        focusedPlaceholderColor = Color(0xFF64748B),
                        unfocusedPlaceholderColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = examTitleHeading,
                    onValueChange = { examTitleHeading = it },
                    label = { Text("Exam Title / Heading (Optional)", color = Color(0xFF475569)) },
                    placeholder = { Text("e.g. Annual Board Practice Examination 2026", color = Color(0xFF64748B)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Color(0xFF94A3B8),
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = Color(0xFF475569),
                        focusedPlaceholderColor = Color(0xFF64748B),
                        unfocusedPlaceholderColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // FORM CARD 3: Textbook Photos
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(IndigoContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 14.sp)
                        }
                        Text(
                            text = "Textbook Photos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                        )
                    }

                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF1F5F9)) {
                        Text(
                            text = "${uploadedImages.size} Pages",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF475569)),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Dropzone Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Click to upload or take photos",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                        )
                        Text(
                            text = "JPG, PNG, or WEBP of textbook or notebook pages",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.5.sp)
                        )
                    }
                }

                // Quick Test sample pages (matching video)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Quick Test: Add Sample Textbook Page", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF64748B)))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.customTopics.value = "Chemical Reactions & Equations, Redox, Oxidation, Precipitation"
                                viewModel.selectedSubject.value = "Science"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8FAFC),
                                contentColor = NavyDark
                            )
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp), tint = IndigoPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chemical Reactions", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.customTopics.value = "Quadratic Equations, Discriminant, Roots, Word Problems"
                                viewModel.selectedSubject.value = "Mathematics"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8FAFC),
                                contentColor = NavyDark
                            )
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp), tint = IndigoPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quadratic Equations", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Thumbnails
                if (uploadedImages.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        itemsIndexed(uploadedImages) { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Page $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clickable { viewModel.removeUploadedImage(index) }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // EXAM SECTION BLUEPRINT SUMMARY CARD
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXAM SECTION BLUEPRINT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                    )
                    Text(
                        text = "$selectedMaxMarks Total Marks",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = IndigoPrimary)
                    )
                }

                currentBlueprint.sectionBlueprints.forEach { spec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${spec.sectionName}: ${spec.count} Qs x ${spec.marksPerQuestion}M",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = NavyDark)
                        )
                        Text(
                            text = "${spec.totalMarks} Marks",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        )
                    }
                }

                Text(
                    text = "Guaranteed arithmetic symmetry: sum of all section marks equals exactly $selectedMaxMarks marks.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF64748B))
                )
            }
        }

        // -------------------------------------------------------------
        // PRIMARY ACTION BUTTON: Generate Paper (80M)
        // -------------------------------------------------------------
        Button(
            onClick = { viewModel.generatePaper() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_paper_action_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            enabled = !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Generating Paper with Groq AI...",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Paper ($selectedMaxMarks M)",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Status Card when Generating
        AnimatedVisibility(visible = isGenerating) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndigoContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = IndigoPrimary, strokeWidth = 2.dp)
                    Text(
                        text = generationStatus.ifBlank { "Processing AI request with Groq..." },
                        style = MaterialTheme.typography.bodySmall.copy(color = NavyDark, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Professional Footer (Strictly NO APK buttons or icons)
        PaperGeniusFooter(onNavigate = { viewModel.setScreen(it) })
    }
}

@Composable
private fun FeatureHighlightPill(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF475569)))
        }
    }
}

@Composable
private fun CloudCheck(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(14.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 11.sp))
    }
}

@Composable
private fun CheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(color = NavyDark, fontWeight = FontWeight.Medium)
        )
    }
}
