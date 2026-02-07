package com.bloodworktracker.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BloodRed80,
    onPrimary = BloodRed10,
    primaryContainer = BloodRed40,
    onPrimaryContainer = BloodRed80,
    secondary = HealthGreen80,
    onSecondary = HealthGreen10,
    secondaryContainer = HealthGreen40,
    onSecondaryContainer = HealthGreen80,
    tertiary = Pink80,
    onTertiary = Pink40,
    error = CriticalRed80,
    onError = CriticalRed10
)

private val LightColorScheme = lightColorScheme(
    primary = BloodRed40,
    onPrimary = Color.White,
    primaryContainer = BloodRed80,
    onPrimaryContainer = BloodRed10,
    secondary = HealthGreen40,
    onSecondary = Color.White,
    secondaryContainer = HealthGreen80,
    onSecondaryContainer = HealthGreen10,
    tertiary = Pink40,
    onTertiary = Color.White,
    error = CriticalRed40,
    onError = Color.White
)

@Composable
fun BloodworkTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}