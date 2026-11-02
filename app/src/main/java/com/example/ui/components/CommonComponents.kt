package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApiProviderStatus
import com.example.model.ProviderMode
import com.example.ui.AppScreen
import com.example.ui.PaperGeniusViewModel
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PurpleAccent
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperGeniusTopBar(
    currentScreen: AppScreen,
    currentUser: FirebaseUser?,
    isDemoMode: Boolean,
    providerStatus: ApiProviderStatus,
    onUserClick: () -> Unit,
    onProviderClick: () -> Unit = {},
    onGenerateClick: () -> Unit = {},
    onNavigate: (AppScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        TopAppBar(
            modifier = Modifier.testTag("app_top_bar"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "PaperGenius Logo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "PaperGenius AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "CBSE • PSEB • ICSE",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                }
            },
            actions = {
                // Interactive Provider status pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when (providerStatus.activeMode) {
                        ProviderMode.DEFAULT_GROQ -> IndigoContainer
                        ProviderMode.PERSONAL_GROQ -> EmeraldContainer
                        ProviderMode.SMART_SYLLABUS_FALLBACK -> Color(0xFFFEF3C7)
                    },
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onProviderClick() }
                        .testTag("ai_provider_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when (providerStatus.activeMode) {
                                        ProviderMode.DEFAULT_GROQ -> IndigoPrimary
                                        ProviderMode.PERSONAL_GROQ -> EmeraldSuccess
                                        ProviderMode.SMART_SYLLABUS_FALLBACK -> Color(0xFFD97706)
                                    }
                                )
                        )
                        Text(
                            text = when (providerStatus.activeMode) {
                                ProviderMode.DEFAULT_GROQ -> "Groq"
                                ProviderMode.PERSONAL_GROQ -> "My Key"
                                ProviderMode.SMART_SYLLABUS_FALLBACK -> "Fallback"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = NavyDark
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Configure Key",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Generate Button in TopBar (as seen in video)
                Button(
                    onClick = onGenerateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("top_bar_generate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Generate",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // User Profile / Login Button
                IconButton(
                    onClick = onUserClick,
                    modifier = Modifier.testTag("user_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Account",
                        tint = if (currentUser != null && !isDemoMode) PurpleAccent else Color(0xFF64748B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Subheader navigation pills (matching video: About, Privacy, Get API, Login)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPill(
                title = "Home",
                selected = currentScreen == AppScreen.GENERATOR,
                onClick = { onNavigate(AppScreen.GENERATOR) }
            )
            NavPill(
                title = "About",
                selected = currentScreen == AppScreen.ABOUT,
                onClick = { onNavigate(AppScreen.ABOUT) }
            )
            NavPill(
                title = "Features",
                selected = currentScreen == AppScreen.FEATURES,
                onClick = { onNavigate(AppScreen.FEATURES) }
            )
            NavPill(
                title = "Privacy",
                selected = currentScreen == AppScreen.ABOUT,
                onClick = { onNavigate(AppScreen.ABOUT) }
            )
            NavPill(
                title = "Get API",
                selected = currentScreen == AppScreen.AI_SETTINGS,
                icon = Icons.Default.Key,
                onClick = { onNavigate(AppScreen.AI_SETTINGS) }
            )
            NavPill(
                title = if (currentUser != null && !isDemoMode) "Account" else "Login",
                selected = currentScreen == AppScreen.PROFILE,
                onClick = onUserClick
            )
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))
    }
}

@Composable
private fun NavPill(
    title: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) IndigoPrimary else Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) IndigoPrimary else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else IndigoPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else Color(0xFF475569)
                )
            )
        }
    }
}

@Composable
fun StatusNotificationBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!message.isNullOrBlank()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("status_notification_banner"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = NavyDark
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaperGeniusBottomBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    savedPapersCount: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = IndigoPrimary,
            selectedTextColor = IndigoPrimary,
            indicatorColor = IndigoContainer,
            unselectedIconColor = Color(0xFF64748B),
            unselectedTextColor = Color(0xFF64748B)
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.GENERATOR,
            onClick = { onScreenSelected(AppScreen.GENERATOR) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.GENERATOR) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                    contentDescription = "Generator",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Generator", fontSize = 11.sp, fontWeight = if (currentScreen == AppScreen.GENERATOR) FontWeight.Bold else FontWeight.Medium) },
            modifier = Modifier.testTag("nav_generator_tab"),
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PREVIEW,
            onClick = { onScreenSelected(AppScreen.PREVIEW) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PREVIEW) Icons.Filled.Preview else Icons.Outlined.Preview,
                    contentDescription = "Preview",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Preview", fontSize = 11.sp, fontWeight = if (currentScreen == AppScreen.PREVIEW) FontWeight.Bold else FontWeight.Medium) },
            modifier = Modifier.testTag("nav_preview_tab"),
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.MY_PAPERS,
            onClick = { onScreenSelected(AppScreen.MY_PAPERS) },
            alwaysShowLabel = true,
            icon = {
                BadgedBox(
                    badge = {
                        if (savedPapersCount > 0) {
                            Badge(containerColor = IndigoPrimary) {
                                Text("$savedPapersCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.MY_PAPERS) Icons.Filled.Description else Icons.Outlined.Description,
                        contentDescription = "My Papers",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            label = { Text("My Papers", fontSize = 11.sp, fontWeight = if (currentScreen == AppScreen.MY_PAPERS) FontWeight.Bold else FontWeight.Medium) },
            modifier = Modifier.testTag("nav_my_papers_tab"),
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AI_SETTINGS,
            onClick = { onScreenSelected(AppScreen.AI_SETTINGS) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.AI_SETTINGS) Icons.Filled.Key else Icons.Outlined.Key,
                    contentDescription = "AI Settings",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("AI Keys", fontSize = 11.sp, fontWeight = if (currentScreen == AppScreen.AI_SETTINGS) FontWeight.Bold else FontWeight.Medium) },
            modifier = Modifier.testTag("nav_ai_settings_tab"),
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onScreenSelected(AppScreen.PROFILE) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Account", fontSize = 11.sp, fontWeight = if (currentScreen == AppScreen.PROFILE) FontWeight.Bold else FontWeight.Medium) },
            modifier = Modifier.testTag("nav_profile_tab"),
            colors = navItemColors
        )
    }
}

@Composable
fun QuickApiKeyDialog(
    viewModel: PaperGeniusViewModel
) {
    val showDialog by viewModel.showApiKeyDialog.collectAsState()
    if (!showDialog) return

    var keyInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.closeApiKeyDialog() },
        modifier = Modifier.testTag("quick_api_key_dialog"),
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = IndigoPrimary)
                Text(
                    text = "Configure Groq API Key",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter your personal Groq API key from console.groq.com. It is encrypted in hardware Keystore and Firestore, and used as priority failover whenever the default AI reaches rate limits.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 12.sp)
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it; testFeedback = null },
                    label = { Text("Groq API Key (gsk_...)", color = Color(0xFF334155)) },
                    placeholder = { Text("gsk_...", color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key",
                                tint = Color(0xFF475569)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_api_key_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (!testFeedback.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTestSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isTestSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isTestSuccess) EmeraldSuccess else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = testFeedback ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (isTestSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyInput.isNotBlank()) {
                        isTesting = true
                        viewModel.savePersonalApiKey(keyInput) { success, msg ->
                            isTesting = false
                            isTestSuccess = success
                            testFeedback = msg
                            if (success) {
                                keyInput = ""
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                enabled = !isTesting && keyInput.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verifying...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Apply")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeApiKeyDialog() }) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun PaperGeniusFooter(
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IndigoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = "PaperGenius AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "Turn Textbook Pages Into Exam-Ready Question Papers",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            // Navigation links row (Strictly NO APK buttons or APK download icons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onNavigate(AppScreen.GENERATOR) }) {
                    Text("Home", fontSize = 11.5.sp, color = Color(0xFF475569))
                }
                TextButton(onClick = { onNavigate(AppScreen.GENERATOR) }) {
                    Text("Generate", fontSize = 11.5.sp, color = Color(0xFF475569))
                }
                TextButton(onClick = { onNavigate(AppScreen.MY_PAPERS) }) {
                    Text("My Papers", fontSize = 11.5.sp, color = Color(0xFF475569))
                }
                TextButton(onClick = { onNavigate(AppScreen.FEATURES) }) {
                    Text("Features", fontSize = 11.5.sp, color = Color(0xFF475569))
                }
                TextButton(onClick = { onNavigate(AppScreen.ABOUT) }) {
                    Text("About", fontSize = 11.5.sp, color = Color(0xFF475569))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onNavigate(AppScreen.ABOUT) }) {
                    Text("Privacy & Security", fontSize = 11.sp, color = IndigoPrimary, fontWeight = FontWeight.SemiBold)
                }
                Text("•", color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
                TextButton(onClick = { onNavigate(AppScreen.AI_SETTINGS) }) {
                    Text("Groq AI API Settings", fontSize = 11.sp, color = IndigoPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Text(
                text = "© 2026 PaperGenius AI. Engineered for CBSE, PSEB & ICSE Educators.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
