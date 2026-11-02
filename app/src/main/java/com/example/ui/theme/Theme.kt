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
    primary = CatPrimaryAmber,
    onPrimary = CatOnPrimary,
    primaryContainer = CatPrimaryContainer,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = CatSecondaryTeal,
    onSecondary = CatOnSecondary,
    secondaryContainer = CatSecondaryContainer,
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = CatTertiaryPurple,
    background = CatAubergineDark,
    onBackground = Color(0xFFF0EDF6),
    surface = CatSurfaceDark,
    onSurface = Color(0xFFEDE9F5),
    surfaceVariant = CatSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBC4D8),
    outline = CatCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD84315),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCCBC),
    onPrimaryContainer = Color(0xFF3E1100),
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF00363A),
    tertiary = Color(0xFF7B1FA2),
    background = Color(0xFFFAF7FC),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFEBE5F0),
    onSurfaceVariant = Color(0xFF4A4553),
    outline = Color(0xFFC7BFCF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve polished custom feline dark aesthetic
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
