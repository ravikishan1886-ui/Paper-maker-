package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.AppScreen
import com.example.ui.PaperGeniusViewModel
import com.example.ui.components.PaperGeniusBottomBar
import com.example.ui.components.PaperGeniusTopBar
import com.example.ui.components.QuickApiKeyDialog
import com.example.ui.components.StatusNotificationBanner
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AiSettingsScreen
import com.example.ui.screens.AuthDialog
import com.example.ui.screens.FeaturesScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.MyPapersScreen
import com.example.ui.screens.PaperPreviewScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PaperGeniusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                PaperGeniusApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PaperGeniusApp(viewModel: PaperGeniusViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    val providerStatus by viewModel.providerStatus.collectAsState()
    val savedPapers by viewModel.savedPapers.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        if (!userMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(userMessage!!)
        }
    }

    // Hardware BackHandler: if on any sub-screen, return to Generator
    BackHandler(enabled = currentScreen != AppScreen.GENERATOR) {
        viewModel.setScreen(AppScreen.GENERATOR)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            PaperGeniusTopBar(
                currentScreen = currentScreen,
                currentUser = currentUser,
                isDemoMode = isDemoMode,
                providerStatus = providerStatus,
                onUserClick = {
                    if (currentUser == null || isDemoMode) {
                        viewModel.openLogin()
                    } else {
                        viewModel.setScreen(AppScreen.PROFILE)
                    }
                },
                onProviderClick = {
                    viewModel.openApiKeyDialog()
                },
                onGenerateClick = {
                    viewModel.setScreen(AppScreen.GENERATOR)
                },
                onNavigate = { screen ->
                    viewModel.setScreen(screen)
                }
            )
        },
        bottomBar = {
            PaperGeniusBottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) },
                savedPapersCount = savedPapers.size
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live status notifications
            StatusNotificationBanner(
                message = userMessage,
                onDismiss = { viewModel.clearUserMessage() }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        AppScreen.GENERATOR -> GeneratorScreen(viewModel = viewModel)
                        AppScreen.PREVIEW -> PaperPreviewScreen(viewModel = viewModel)
                        AppScreen.MY_PAPERS -> MyPapersScreen(viewModel = viewModel)
                        AppScreen.FEATURES -> FeaturesScreen(viewModel = viewModel)
                        AppScreen.ABOUT -> AboutScreen(viewModel = viewModel)
                        AppScreen.AI_SETTINGS -> AiSettingsScreen(viewModel = viewModel)
                        AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Authentication Modal Overlay
        AuthDialog(viewModel = viewModel)

        // Universal Groq API Key Modal Overlay
        QuickApiKeyDialog(viewModel = viewModel)
    }
}
