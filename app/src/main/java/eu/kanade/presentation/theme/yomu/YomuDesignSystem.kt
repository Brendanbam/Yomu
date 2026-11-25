package eu.kanade.presentation.theme.yomu

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.domain.ui.model.YomuShelfMaterial

object YomuColors {
    val EspressoBackground = Color(0xFF100D0B)
    val DarkWoodSurface = Color(0xFF1D1713)
    val ElevatedWalnut = Color(0xFF302219)
    val WoodenShelf = Color(0xFF754929)
    val AntiqueGold = Color(0xFFD6AD73)
    val WarmIvory = Color(0xFFF4E9D7)
    val AmethystPurple = Color(0xFF9460DF)
    val DownloadedSage = Color(0xFF83AA8A)
    val SecondaryText = Color(0xFFB7A99A)
    val SubtleBorder = Color(0xFF443229)
    val ErrorRed = Color(0xFFD96B60)
}

object YomuSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object YomuShapes {
    val bookCover = RoundedCornerShape(topStart = 2.dp, topEnd = 3.dp, bottomEnd = 3.dp, bottomStart = 2.dp)
    val card = RoundedCornerShape(14.dp)
    val heroBanner = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(50)
    val button = RoundedCornerShape(12.dp)
    val chapterRow = RoundedCornerShape(12.dp)
    val chip = RoundedCornerShape(20.dp)
}

@Immutable
data class YomuShelfPalette(
    val alcoveBackTop: Color,
    val alcoveBackBottom: Color,
    val alcovePanelLine: Color,
    val lanternGlow: Color,
    val shelfTopLit: Color,
    val shelfTopBase: Color,
    val shelfBevelHighlight: Color,
    val shelfFrontTop: Color,
    val shelfFrontBottom: Color,
    val shelfGrainDark: Color,
    val shelfGrainLight: Color,
    val shelfUnderShadow: Color,
    val bookPageEdge: Color,
    val bookPageLine: Color,
    val bookSpineShadow: Color,
    val goldAccent: Color,
    val sageAccent: Color,
    val subtleBorder: Color,
    val cardSurface: Color,
    val elevatedSurface: Color,
    val isLight: Boolean,
)

object YomuShelfPalettes {
    val DarkWalnut = YomuShelfPalette(
        alcoveBackTop = Color(0xFF1F140D),
        alcoveBackBottom = Color(0xFF120C08),
        alcovePanelLine = Color(0xFF2E1E14),
        lanternGlow = Color(0xFFFFBE73),
        shelfTopLit = Color(0xFF9E6B40),
        shelfTopBase = Color(0xFF6B4122),
        shelfBevelHighlight = Color(0xFFC49260),
        shelfFrontTop = Color(0xFF7D4E2C),
        shelfFrontBottom = Color(0xFF4B2B15),
        shelfGrainDark = Color(0xFF3A200E),
        shelfGrainLight = Color(0xFF9C693F),
        shelfUnderShadow = Color(0xFF070402),
        bookPageEdge = Color(0xFFE7DAC6),
        bookPageLine = Color(0xFFBBA992),
        bookSpineShadow = Color(0xFF0A0604),
        goldAccent = YomuColors.AntiqueGold,
        sageAccent = YomuColors.DownloadedSage,
        subtleBorder = YomuColors.SubtleBorder,
        cardSurface = YomuColors.DarkWoodSurface,
        elevatedSurface = YomuColors.ElevatedWalnut,
        isLight = false,
    )

    val BlackEbony = YomuShelfPalette(
        alcoveBackTop = Color(0xFF100E0E),
        alcoveBackBottom = Color(0xFF050404),
        alcovePanelLine = Color(0xFF1B1817),
        lanternGlow = Color(0xFFD9C5A8),
        shelfTopLit = Color(0xFF3E3631),
        shelfTopBase = Color(0xFF211C19),
        shelfBevelHighlight = Color(0xFF5D524A),
        shelfFrontTop = Color(0xFF2B2522),
        shelfFrontBottom = Color(0xFF13100E),
        shelfGrainDark = Color(0xFF090807),
        shelfGrainLight = Color(0xFF423A34),
        shelfUnderShadow = Color(0xFF000000),
        bookPageEdge = Color(0xFFDCD3C5),
        bookPageLine = Color(0xFFA89E91),
        bookSpineShadow = Color(0xFF000000),
        goldAccent = Color(0xFFC8B496),
        sageAccent = Color(0xFF7CA684),
        subtleBorder = Color(0xFF2B2420),
        cardSurface = Color(0xFF121010),
        elevatedSurface = Color(0xFF1E1A18),
        isLight = false,
    )

    val NaturalOak = YomuShelfPalette(
        alcoveBackTop = Color(0xFFF3E9DA),
        alcoveBackBottom = Color(0xFFE8DAC6),
        alcovePanelLine = Color(0xFFD7C5AC),
        lanternGlow = Color(0xFFFFF4DE),
        shelfTopLit = Color(0xFFD9B282),
        shelfTopBase = Color(0xFFB58652),
        shelfBevelHighlight = Color(0xFFEED0A6),
        shelfFrontTop = Color(0xFFC3935E),
        shelfFrontBottom = Color(0xFF8F6335),
        shelfGrainDark = Color(0xFF754E27),
        shelfGrainLight = Color(0xFFDDB787),
        shelfUnderShadow = Color(0xFF6B4C2E),
        bookPageEdge = Color(0xFFFAF4EA),
        bookPageLine = Color(0xFFD5C7B4),
        bookSpineShadow = Color(0xFF5B432C),
        goldAccent = Color(0xFF9C6D2B),
        sageAccent = Color(0xFF4D7B57),
        subtleBorder = Color(0xFFD7C6B1),
        cardSurface = Color(0xFFF1E7D8),
        elevatedSurface = Color(0xFFE7D8C3),
        isLight = true,
    )

    val CherryWood = YomuShelfPalette(
        alcoveBackTop = Color(0xFF28151B),
        alcoveBackBottom = Color(0xFF160B0E),
        alcovePanelLine = Color(0xFF3A1E26),
        lanternGlow = Color(0xFFFFB5C2),
        shelfTopLit = Color(0xFF9A4D56),
        shelfTopBase = Color(0xFF692D36),
        shelfBevelHighlight = Color(0xFFC1737C),
        shelfFrontTop = Color(0xFF79353F),
        shelfFrontBottom = Color(0xFF481B22),
        shelfGrainDark = Color(0xFF361218),
        shelfGrainLight = Color(0xFFA0515C),
        shelfUnderShadow = Color(0xFF0A0406),
        bookPageEdge = Color(0xFFEEE0DD),
        bookPageLine = Color(0xFFC0ABA7),
        bookSpineShadow = Color(0xFF0D0507),
        goldAccent = Color(0xFFDFB982),
        sageAccent = Color(0xFF83AA8A),
        subtleBorder = Color(0xFF472C33),
        cardSurface = Color(0xFF24151A),
        elevatedSurface = Color(0xFF361F27),
        isLight = false,
    )
}

@Immutable
data class YomuVisualStyle(
    val appTheme: AppTheme = AppTheme.MIDNIGHT_WALNUT,
    val shelfPalette: YomuShelfPalette = YomuShelfPalettes.DarkWalnut,
    val lightingIntensity: Float = 0.75f,
    val bookThickness: Float = 0.70f,
    val shadowIntensity: Float = 0.80f,
    val decorationIntensity: Float = 0.85f,
    val animationIntensity: Float = 0.80f,
    val reducedMotion: Boolean = false,
    val systemAnimatorScale: Float = 1f,
    val showShelfTitles: Boolean = true,
    val showShelfProgress: Boolean = true,
    val smartShelvesEnabled: Boolean = true,
) {
    val effectiveMotionScale: Float
        get() = if (reducedMotion || systemAnimatorScale <= 0f) {
            0f
        } else {
            (animationIntensity * systemAnimatorScale).coerceIn(0f, 1f)
        }

    val isOriginalMihon: Boolean
        get() = appTheme == AppTheme.DEFAULT
}

val LocalYomuStyle = staticCompositionLocalOf { YomuVisualStyle() }

fun resolveShelfPalette(
    appTheme: AppTheme,
    shelfMaterial: YomuShelfMaterial,
    isDark: Boolean,
): YomuShelfPalette {
    return when (shelfMaterial) {
        YomuShelfMaterial.DARK_WALNUT -> YomuShelfPalettes.DarkWalnut
        YomuShelfMaterial.BLACK_EBONY -> YomuShelfPalettes.BlackEbony
        YomuShelfMaterial.NATURAL_OAK -> YomuShelfPalettes.NaturalOak
        YomuShelfMaterial.CHERRY_WOOD -> YomuShelfPalettes.CherryWood
        YomuShelfMaterial.AUTO -> when (appTheme) {
            AppTheme.BLACK_EBONY -> YomuShelfPalettes.BlackEbony
            AppTheme.NATURAL_OAK -> YomuShelfPalettes.NaturalOak
            AppTheme.CHERRY_BLOSSOM -> if (isDark) YomuShelfPalettes.CherryWood else YomuShelfPalettes.NaturalOak
            AppTheme.MIDNIGHT_WALNUT -> if (isDark) YomuShelfPalettes.DarkWalnut else YomuShelfPalettes.NaturalOak
            else -> if (isDark) YomuShelfPalettes.DarkWalnut else YomuShelfPalettes.NaturalOak
        }
    }
}

val YomuSerifFontFamily = FontFamily.Serif
val YomuSansFontFamily = FontFamily.SansSerif

val YomuTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = YomuSerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = 0.5.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = YomuSerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.4.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = YomuSerifFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.25.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.15).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = YomuSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
)
