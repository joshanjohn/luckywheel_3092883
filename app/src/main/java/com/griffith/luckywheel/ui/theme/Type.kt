package com.griffith.luckywheel.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),
        fontProvider = provider,
    )
)

val BubbleFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Knewave"),
        fontProvider = provider,
    )
)

val MeriendaFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Merienda"),
        fontProvider = provider,
    )
)


val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = provider,
    )
)

val ArcadeFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Silkscreen"),
        fontProvider = provider,
    )
)

val RetroFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Press Start 2P"),
        fontProvider = provider,
    )
)

val BungeeFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Bungee"),
        fontProvider = provider,
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = BungeeFontFamily, fontSize = 36.sp, letterSpacing = 1.sp),
    displayMedium = baseline.displayMedium.copy(fontFamily = BungeeFontFamily, fontSize = 28.sp),
    displaySmall = baseline.displaySmall.copy(fontFamily = RetroFontFamily, fontSize = 18.sp, lineHeight = 24.sp),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = ArcadeFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = ArcadeFontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = ArcadeFontFamily, fontSize = 20.sp),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)

