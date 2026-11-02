package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProviderMode
import com.example.ui.PaperGeniusViewModel
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer

@Composable
fun AiSettingsScreen(
    viewModel: PaperGeniusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val providerStatus by viewModel.providerStatus.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()

    var inputKey by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "AI API Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            )
            Text(
                text = "Manage your Groq AI providers, personal API keys, and automatic failover.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
        }

        // Current Provider Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_ai_provider_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Primary AI Provider",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B))
                        )
                        Text(
                            text = "Groq AI (Ultra-Low Latency)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (providerStatus.activeMode) {
                            ProviderMode.DEFAULT_GROQ -> IndigoContainer
                            ProviderMode.PERSONAL_GROQ -> EmeraldContainer
                            ProviderMode.SMART_SYLLABUS_FALLBACK -> Color(0xFFFEF3C7)
                        }
                    ) {
                        Text(
                            text = when (providerStatus.activeMode) {
                                ProviderMode.DEFAULT_GROQ -> "Default Active"
                                ProviderMode.PERSONAL_GROQ -> "Personal Active"
                                ProviderMode.SMART_SYLLABUS_FALLBACK -> "Fallback Mode"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (providerStatus.activeMode) {
                                    ProviderMode.DEFAULT_GROQ -> IndigoPrimary
                                    ProviderMode.PERSONAL_GROQ -> EmeraldSuccess
                                    ProviderMode.SMART_SYLLABUS_FALLBACK -> Color(0xFFB45309)
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Live status message
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = providerStatus.statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = NavyDark
                            )
                        )
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Dual Status Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Groq Key:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (providerStatus.defaultKeyConfigured) EmeraldSuccess else Color.Gray)
                            )
                            Text(
                                text = if (providerStatus.defaultKeyConfigured) "Configured (AI Studio)" else "Not Set",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personal Groq Key:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (providerStatus.personalKeyConfigured) EmeraldSuccess else Color(0xFFF59E0B))
                            )
                            Text(
                                text = if (providerStatus.personalKeyConfigured) "Configured & Encrypted" else "Not Added",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }

        // Personal API Key Management Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("personal_groq_key_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Personal Groq API Key",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    )
                }

                Text(
                    text = "Add your personal key from console.groq.com. When the application's default Groq API quota is reached, PaperGenius automatically falls back to your personal key so generation never halts.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), lineHeight = 18.sp)
                )

                // Masked representation display if key is already saved
                if (providerStatus.personalKeyConfigured && !providerStatus.personalKeyMasked.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active Key (Encrypted in Keystore):", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                Text(
                                    text = providerStatus.personalKeyMasked ?: "••••••••",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                )
                            }

                            Row {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.removePersonalApiKey()
                                        testFeedback = null
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    modifier = Modifier.testTag("remove_api_key_button")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Input Field for New / Updated Key
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Your Groq API Key") },
                    placeholder = { Text("Enter your personal Groq API key (gsk_...)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_groq_key_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Action Buttons: Save, Test, Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputKey.isBlank()) {
                                Toast.makeText(context, "Please enter an API key", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isTestingConnection = true
                            testFeedback = null
                            viewModel.savePersonalApiKey(inputKey) { success, msg ->
                                isTestingConnection = false
                                isTestSuccess = success
                                testFeedback = msg
                                if (success) inputKey = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isTestingConnection && inputKey.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_api_key_button")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Key")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (inputKey.isBlank()) {
                                Toast.makeText(context, "Enter a key above to test", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            isTestingConnection = true
                            testFeedback = null
                            viewModel.testPersonalApiKey(inputKey) { success, msg ->
                                isTestingConnection = false
                                isTestSuccess = success
                                testFeedback = msg
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isTestingConnection && inputKey.isNotBlank(),
                        modifier = Modifier.testTag("test_api_key_button")
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test")
                    }

                    if (inputKey.isNotBlank()) {
                        OutlinedButton(
                            onClick = { inputKey = "" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_api_key_button")
                        ) {
                            Text("Clear")
                        }
                    }
                }

                // Test Results Feedback Box
                AnimatedVisibility(visible = !testFeedback.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTestSuccess) EmeraldContainer else Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth().testTag("api_test_result_box")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isTestSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isTestSuccess) EmeraldSuccess else Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = testFeedback ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isTestSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Priority and Automatic Fallback Architecture Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Automatic Fallback Priority Engine",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    )
                }

                Text(
                    text = "1. Priority 1 (Default Groq API): Requests start here using the application's pre-configured cloud key.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                )
                Text(
                    text = "2. Priority 2 (Personal Groq API): If the default key hits HTTP 429 rate limit or quota exceeded, PaperGenius seamlessly switches to your personal key with zero duplicate papers.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                )
                Text(
                    text = "3. Priority 3 (Syllabus Engine): If network or both keys are offline, intelligent curriculum synthesis ensures uninterrupted paper generation.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                )

                Divider(color = Color(0xFFF1F5F9))

                Text(
                    text = "Security Guarantee: Your personal API key is encrypted using AES-256-GCM via the Android Keystore. It is never logged, never exposed in plain text, and can be deleted at any time.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
