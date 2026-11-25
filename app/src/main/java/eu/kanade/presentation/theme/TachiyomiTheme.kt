package eu.kanade.presentation.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.domain.ui.model.YomuAccentColor
import eu.kanade.domain.ui.model.YomuShelfMaterial
import eu.kanade.presentation.theme.colorscheme.BaseColorScheme
import eu.kanade.presentation.theme.colorscheme.BlackEbonyColorScheme
import eu.kanade.presentation.theme.colorscheme.CatppuccinColorScheme
import eu.kanade.presentation.theme.colorscheme.CherryBlossomColorScheme
import eu.kanade.presentation.theme.colorscheme.GreenAppleColorScheme
import eu.kanade.presentation.theme.colorscheme.LavenderColorScheme
import eu.kanade.presentation.theme.colorscheme.MidnightDuskColorScheme
import eu.kanade.presentation.theme.colorscheme.MidnightWalnutColorScheme
import eu.kanade.presentation.theme.colorscheme.MonetColorScheme
import eu.kanade.presentation.theme.colorscheme.MonochromeColorScheme
import eu.kanade.presentation.theme.colorscheme.NaturalOakColorScheme
import eu.kanade.presentation.theme.colorscheme.NordColorScheme
import eu.kanade.presentation.theme.colorscheme.StrawberryColorScheme
import eu.kanade.presentation.theme.colorscheme.TachiyomiColorScheme
import eu.kanade.presentation.theme.colorscheme.TakoColorScheme
import eu.kanade.presentation.theme.colorscheme.TealTurqoiseColorScheme
import eu.kanade.presentation.theme.colorscheme.TidalWaveColorScheme
import eu.kanade.presentation.theme.colorscheme.TokyoNightColorScheme
import eu.kanade.presentation.theme.colorscheme.YinYangColorScheme
import eu.kanade.presentation.theme.colorscheme.YotsubaColorScheme
import eu.kanade.presentation.theme.colorscheme.withYomuAccent
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuTypography
import eu.kanade.presentation.theme.yomu.YomuVisualStyle
import eu.kanade.presentation.theme.yomu.resolveShelfPalette
import mihon.app.di.appGraph
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun TachiyomiTheme(
    appTheme: AppTheme? = null,
    amoled: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val uiPreferences = remember { context.appGraph.uiPreferences }
    val prefAppTheme by uiPreferences.appTheme.collectAsState()
    val prefAmoled by uiPreferences.themeDarkAmoled.collectAsState()
    val accentColor by uiPreferences.yomuAccentColor.collectAsState()
    val shelfMaterial by uiPreferences.yomuShelfMaterial.collectAsState()
    val lightingIntensity by uiPreferences.yomuLightingIntensity.collectAsState()
    val bookThickness by uiPreferences.yomuBookThickness.collectAsState()
    val shadowIntensity by uiPreferences.yomuShadowIntensity.collectAsState()
    val decorationIntensity by uiPreferences.yomuDecorationIntensity.collectAsState()
    val animationIntensity by uiPreferences.yomuAnimationIntensity.collectAsState()
    val reducedMotion by uiPreferences.yomuReducedMotion.collectAsState()
    val showShelfTitles by uiPreferences.yomuShowShelfTitles.collectAsState()
    val showShelfProgress by uiPreferences.yomuShowShelfProgress.collectAsState()
    val smartShelvesEnabled by uiPreferences.yomuSmartShelvesEnabled.collectAsState()

    val resolvedTheme = appTheme ?: prefAppTheme
    val resolvedAmoled = amoled ?: prefAmoled

    BaseTachiyomiTheme(
        appTheme = resolvedTheme,
        isAmoled = resolvedAmoled,
        accentColor = if (appTheme == null) accentColor else YomuAccentColor.AMETHYST,
        shelfMaterial = if (appTheme == null) shelfMaterial else YomuShelfMaterial.AUTO,
        lightingIntensity = lightingIntensity / 100f,
        bookThickness = bookThickness / 100f,
        shadowIntensity = shadowIntensity / 100f,
        decorationIntensity = decorationIntensity / 100f,
        animationIntensity = animationIntensity / 100f,
        reducedMotion = reducedMotion,
        showShelfTitles = showShelfTitles,
        showShelfProgress = showShelfProgress,
        smartShelvesEnabled = smartShelvesEnabled,
        content = content,
    )
}

@Composable
fun TachiyomiPreviewTheme(
    appTheme: AppTheme = AppTheme.MIDNIGHT_WALNUT,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit,
) = BaseTachiyomiTheme(appTheme = appTheme, isAmoled = isAmoled, content = content)

@Composable
private fun BaseTachiyomiTheme(
    appTheme: AppTheme,
    isAmoled: Boolean,
    accentColor: YomuAccentColor = YomuAccentColor.AMETHYST,
    shelfMaterial: YomuShelfMaterial = YomuShelfMaterial.AUTO,
    lightingIntensity: Float = 0.75f,
    bookThickness: Float = 0.70f,
    shadowIntensity: Float = 0.80f,
    decorationIntensity: Float = 0.85f,
    animationIntensity: Float = 0.80f,
    reducedMotion: Boolean = false,
    showShelfTitles: Boolean = true,
    showShelfProgress: Boolean = true,
    smartShelvesEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val effectiveDark = when (appTheme) {
        AppTheme.NATURAL_OAK -> false
        AppTheme.BLACK_EBONY -> true
        else -> systemDark
    }
    val shelfPalette = remember(appTheme, shelfMaterial, effectiveDark) {
        resolveShelfPalette(appTheme, shelfMaterial, effectiveDark)
    }
    val yomuStyle = remember(
        appTheme,
        shelfPalette,
        lightingIntensity,
        bookThickness,
        shadowIntensity,
        decorationIntensity,
        animationIntensity,
        reducedMotion,
        showShelfTitles,
        showShelfProgress,
        smartShelvesEnabled,
    ) {
        YomuVisualStyle(
            appTheme = appTheme,
            shelfPalette = shelfPalette,
            lightingIntensity = lightingIntensity,
            bookThickness = bookThickness,
            shadowIntensity = shadowIntensity,
            decorationIntensity = decorationIntensity,
            animationIntensity = animationIntensity,
            reducedMotion = reducedMotion,
            showShelfTitles = showShelfTitles,
            showShelfProgress = showShelfProgress,
            smartShelvesEnabled = smartShelvesEnabled,
        )
    }

    val colorScheme = remember(appTheme, effectiveDark, isAmoled, accentColor) {
        getThemeColorScheme(
            context = context,
            appTheme = appTheme,
            isDark = effectiveDark,
            isAmoled = isAmoled,
            accentColor = accentColor,
        )
    }

    CompositionLocalProvider(LocalYomuStyle provides yomuStyle) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = YomuTypography,
            content = content,
        )
    }
}

private fun getThemeColorScheme(
    context: Context,
    appTheme: AppTheme,
    isDark: Boolean,
    isAmoled: Boolean,
    accentColor: YomuAccentColor,
): ColorScheme {
    val base = if (appTheme == AppTheme.MONET) {
        MonetColorScheme(context)
    } else {
        colorSchemes.getOrDefault(appTheme, MidnightWalnutColorScheme)
    }
    val scheme = base.getColorScheme(
        isDark = isDark,
        isAmoled = isAmoled,
        overrideDarkSurfaceContainers = appTheme != AppTheme.MONET &&
            appTheme != AppTheme.MIDNIGHT_WALNUT &&
            appTheme != AppTheme.BLACK_EBONY &&
            appTheme != AppTheme.CHERRY_BLOSSOM,
    )
    val isYomuTheme = appTheme in setOf(
        AppTheme.MIDNIGHT_WALNUT,
        AppTheme.BLACK_EBONY,
        AppTheme.NATURAL_OAK,
        AppTheme.CHERRY_BLOSSOM,
    )
    return if (isYomuTheme) {
        scheme.withYomuAccent(accentColor = accentColor, isLight = !isDark)
    } else {
        scheme
    }
}

private val colorSchemes: Map<AppTheme, BaseColorScheme> = mapOf(
    AppTheme.MIDNIGHT_WALNUT to MidnightWalnutColorScheme,
    AppTheme.BLACK_EBONY to BlackEbonyColorScheme,
    AppTheme.NATURAL_OAK to NaturalOakColorScheme,
    AppTheme.CHERRY_BLOSSOM to CherryBlossomColorScheme,
    AppTheme.DEFAULT to TachiyomiColorScheme,
    AppTheme.CATPPUCCIN to CatppuccinColorScheme,
    AppTheme.TOKYONIGHT to TokyoNightColorScheme,
    AppTheme.GREEN_APPLE to GreenAppleColorScheme,
    AppTheme.LAVENDER to LavenderColorScheme,
    AppTheme.MIDNIGHT_DUSK to MidnightDuskColorScheme,
    AppTheme.MONOCHROME to MonochromeColorScheme,
    AppTheme.NORD to NordColorScheme,
    AppTheme.STRAWBERRY_DAIQUIRI to StrawberryColorScheme,
    AppTheme.TAKO to TakoColorScheme,
    AppTheme.TEALTURQUOISE to TealTurqoiseColorScheme,
    AppTheme.TIDAL_WAVE to TidalWaveColorScheme,
    AppTheme.YINYANG to YinYangColorScheme,
    AppTheme.YOTSUBA to YotsubaColorScheme,
)
