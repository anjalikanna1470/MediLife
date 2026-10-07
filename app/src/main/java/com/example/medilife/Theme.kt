package com.example.medilife

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// MediLife Premium Healthcare Color Palette
val TealPrimary = Color(0xFF006B7D)
val TealPrimaryContainer = Color(0xFFE0F3F7)
val OnTealContainer = Color(0xFF002026)

val BlueSecondary = Color(0xFF007F8F)
val BlueSecondaryContainer = Color(0xFFD9F0F2)

val NeutralLightBg = Color(0xFFF7FBFD)
val NeutralSurface = Color(0xFFFFFFFF)
val NeutralSurfaceVariant = Color(0xFFF0F4F7)
val OnSurfaceMain = Color(0xFF0F1D22)
val OnSurfaceSub = Color(0xFF404E53)

val BorderLight = Color(0xFFE1E8EC)

// Dark Palette
val DarkBg = Color(0xFF0B1418)
val DarkSurface = Color(0xFF132026)
val DarkSurfaceVariant = Color(0xFF1B2B32)
val DarkOnSurfaceMain = Color(0xFFE8F2F6)
val DarkOnSurfaceSub = Color(0xFFA0B2BC)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = BlueSecondary,
    secondaryContainer = BlueSecondaryContainer,
    background = NeutralLightBg,
    surface = NeutralSurface,
    surfaceVariant = NeutralSurfaceVariant,
    onBackground = OnSurfaceMain,
    onSurface = OnSurfaceMain,
    onSurfaceVariant = OnSurfaceSub,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF62DBEE),
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF005A68),
    onPrimaryContainer = Color(0xFFE3FAFC),
    secondary = Color(0xFF62DBEE),
    onSecondary = Color(0xFF003640),
    secondaryContainer = Color(0xFF17434A),
    onSecondaryContainer = Color(0xFFD9F5F7),
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurfaceMain,
    onSurface = DarkOnSurfaceMain,
    onSurfaceVariant = DarkOnSurfaceSub,
    outline = Color(0xFF2A3D46)
)

val MediLifeTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun MediLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MediLifeTypography,
        content = content
    )
}
