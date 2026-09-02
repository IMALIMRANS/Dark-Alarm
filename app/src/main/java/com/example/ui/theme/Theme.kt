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

private val DarkColorScheme =
  darkColorScheme(
    primary = PolishPrimaryBadge,
    onPrimary = PolishOnActiveContainer,
    primaryContainer = PolishDarkActiveContainer,
    onPrimaryContainer = PolishActiveContainer,
    secondary = PolishPrimary,
    onSecondary = Color.White,
    secondaryContainer = PolishDarkInactiveContainer,
    onSecondaryContainer = PolishDarkTextPrimary,
    tertiary = PolishSuccess,
    onTertiary = Color.White,
    background = PolishDarkBg,
    onBackground = PolishDarkTextPrimary,
    surface = PolishDarkSurface,
    onSurface = PolishDarkTextPrimary,
    surfaceVariant = PolishDarkInactiveContainer,
    onSurfaceVariant = PolishDarkTextSecondary,
    outline = PolishMuted,
    outlineVariant = PolishBorder,
    error = PolishError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishActiveContainer,
    onPrimaryContainer = PolishOnActiveContainer,
    secondary = PolishPrimary,
    onSecondary = Color.White,
    secondaryContainer = PolishInactiveContainer,
    onSecondaryContainer = PolishTextPrimary,
    tertiary = PolishSuccess,
    onTertiary = Color.White,
    background = PolishLavenderBg,
    onBackground = PolishTextPrimary,
    surface = PolishLavenderBg,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishInactiveContainer,
    onSurfaceVariant = PolishTextSecondary,
    outline = PolishBorder,
    outlineVariant = PolishInactiveTrack,
    error = PolishError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
