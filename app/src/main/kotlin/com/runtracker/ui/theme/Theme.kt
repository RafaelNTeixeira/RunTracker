package com.runtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary            = GreenPrimary,
    onPrimary          = Color.Black,
    primaryContainer   = GreenDim,
    onPrimaryContainer = GreenPrimary,
    secondary          = OrangeAccent,
    onSecondary        = Color.Black,
    background         = DarkBg,
    onBackground       = TextPrimary,
    surface            = DarkSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = TextSecondary,
    outline            = DarkBorder,
    error              = RedAccent,
    onError            = Color.White
)

@Composable
fun RunTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
