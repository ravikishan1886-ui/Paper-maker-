package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color.White,
    primaryContainer = NavySurface,
    onPrimaryContainer = IndigoContainer,
    secondary = PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = NavySurface,
    onSecondaryContainer = PurpleContainer,
    tertiary = EmeraldSuccess,
    background = NavyDark,
    surface = NavySurface,
    onBackground = SlateBackground,
    onSurface = SlateBackground,
    surfaceVariant = NavyLight,
    onSurfaceVariant = SlateBorder,
    outline = NavyLight
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = OnIndigoContainer,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = PurpleContainer,
    onSecondaryContainer = PurpleAccent,
    tertiary = EmeraldSuccess,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF1E293B), // Slate 800 for high-contrast visible text & labels
    outline = Color(0xFF94A3B8), // Slate 400 for clearly defined borders
    outlineVariant = Color(0xFFCBD5E1),
    error = RoseError,
    errorContainer = RoseContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Enforce crisp high-contrast light SaaS theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
  )
}
