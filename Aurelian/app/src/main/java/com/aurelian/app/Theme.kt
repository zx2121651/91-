package com.aurelian.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    secondary = GoldLight,
    tertiary = Silver,
    background = DeepBlack,
    surface = DeepBlack,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Silver,
    onSurface = Silver,
)

private val LightColorScheme = darkColorScheme( // Keep it dark theme only for "Aurelian Night"
    primary = Gold,
    secondary = GoldLight,
    tertiary = Silver,
    background = DeepBlack,
    surface = DeepBlack,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Silver,
    onSurface = Silver,
)

@Composable
fun AurelianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
