package com.bloodworktracker.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 color scheme for bloodwork tracking
val BloodRed80 = Color(0xFFFFB3BA)
val BloodRed40 = Color(0xFFDC143C)
val BloodRed10 = Color(0xFF8B0000)

val HealthGreen80 = Color(0xFFBBF7D0)
val HealthGreen40 = Color(0xFF10B981)
val HealthGreen10 = Color(0xFF064E3B)

val WarningOrange80 = Color(0xFFFED7AA)
val WarningOrange40 = Color(0xFFEA580C)
val WarningOrange10 = Color(0xFF9A3412)

val CriticalRed80 = Color(0xFFFCA5A5)
val CriticalRed40 = Color(0xFFEF4444)
val CriticalRed10 = Color(0xFF991B1B)

val PurpleGrey80 = Color(0xFFCCC2DC)
val PurpleGrey40 = Color(0xFF625b71)

val Pink80 = Color(0xFFEFB8C8)
val Pink40 = Color(0xFF7D5260)

// Status colors for blood values
val StatusNormal = HealthGreen40
val StatusHigh = WarningOrange40
val StatusLow = WarningOrange40
val StatusCriticalHigh = CriticalRed40
val StatusCriticalLow = CriticalRed40