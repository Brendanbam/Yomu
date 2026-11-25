package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import eu.kanade.domain.ui.model.YomuAccentColor

/**
 * Yomu — Your Manga Sanctuary color schemes.
 *
 * Midnight Walnut is the signature default palette matching the primary bookshelf design:
 * Espresso background (#100D0B), Dark wood surface (#1D1713), Elevated walnut (#302219),
 * Antique gold (#D6AD73), Warm ivory (#F4E9D7), Amethyst purple (#9460DF), Sage green (#83AA8A).
 */
internal object MidnightWalnutColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF9460DF),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF3E2662),
        onPrimaryContainer = Color(0xFFEAD9FF),
        inversePrimary = Color(0xFF6E3EB8),
        secondary = Color(0xFFD6AD73),
        onSecondary = Color(0xFF231708),
        secondaryContainer = Color(0xFF3E2C1B),
        onSecondaryContainer = Color(0xFFF6DFBA),
        tertiary = Color(0xFF83AA8A),
        onTertiary = Color(0xFF0E2112),
        tertiaryContainer = Color(0xFF243928),
        onTertiaryContainer = Color(0xFFCDE5D1),
        background = Color(0xFF100D0B),
        onBackground = Color(0xFFF4E9D7),
        surface = Color(0xFF100D0B),
        onSurface = Color(0xFFF4E9D7),
        surfaceVariant = Color(0xFF1D1713),
        onSurfaceVariant = Color(0xFFB7A99A),
        surfaceTint = Color(0xFF9460DF),
        inverseSurface = Color(0xFFF4E9D7),
        inverseOnSurface = Color(0xFF1D1713),
        error = Color(0xFFD96B60),
        onError = Color(0xFF2C0906),
        errorContainer = Color(0xFF54201B),
        onErrorContainer = Color(0xFFFFDAD5),
        outline = Color(0xFF5A4539),
        outlineVariant = Color(0xFF443229),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF36281F),
        surfaceDim = Color(0xFF0C0907),
        surfaceContainerLowest = Color(0xFF0A0806),
        surfaceContainerLow = Color(0xFF16110E),
        surfaceContainer = Color(0xFF1D1713),
        surfaceContainerHigh = Color(0xFF271D17),
        surfaceContainerHighest = Color(0xFF302219),
    )

    override val lightScheme = NaturalOakColorScheme.lightScheme
}

internal object BlackEbonyColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF9460DF),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF2E1E4A),
        onPrimaryContainer = Color(0xFFE7D6FF),
        inversePrimary = Color(0xFF6B3CB3),
        secondary = Color(0xFFC8B496),
        onSecondary = Color(0xFF1A140C),
        secondaryContainer = Color(0xFF29221B),
        onSecondaryContainer = Color(0xFFEBDCC4),
        tertiary = Color(0xFF7CA684),
        onTertiary = Color(0xFF0A1D0E),
        tertiaryContainer = Color(0xFF1D3021),
        onTertiaryContainer = Color(0xFFC7E2CC),
        background = Color(0xFF050404),
        onBackground = Color(0xFFF2EAE0),
        surface = Color(0xFF050404),
        onSurface = Color(0xFFF2EAE0),
        surfaceVariant = Color(0xFF121010),
        onSurfaceVariant = Color(0xFFA89E94),
        surfaceTint = Color(0xFF9460DF),
        inverseSurface = Color(0xFFF2EAE0),
        inverseOnSurface = Color(0xFF121010),
        error = Color(0xFFD96B60),
        onError = Color(0xFF2A0805),
        errorContainer = Color(0xFF4E1D19),
        onErrorContainer = Color(0xFFFFDAD5),
        outline = Color(0xFF3C332E),
        outlineVariant = Color(0xFF28221F),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF24201E),
        surfaceDim = Color(0xFF030202),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF0B0A0A),
        surfaceContainer = Color(0xFF121010),
        surfaceContainerHigh = Color(0xFF1B1817),
        surfaceContainerHighest = Color(0xFF24201E),
    )

    override val lightScheme = NaturalOakColorScheme.lightScheme
}

internal object NaturalOakColorScheme : BaseColorScheme() {

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF7845C4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEBDCFF),
        onPrimaryContainer = Color(0xFF2A0F56),
        inversePrimary = Color(0xFFCBB0FA),
        secondary = Color(0xFF8F632C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF7E2C2),
        onSecondaryContainer = Color(0xFF301E07),
        tertiary = Color(0xFF4D7B57),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD2EAD7),
        onTertiaryContainer = Color(0xFF102A16),
        background = Color(0xFFFBF6EE),
        onBackground = Color(0xFF231914),
        surface = Color(0xFFFBF6EE),
        onSurface = Color(0xFF231914),
        surfaceVariant = Color(0xFFEDE2D2),
        onSurfaceVariant = Color(0xFF5D4D40),
        surfaceTint = Color(0xFF7845C4),
        inverseSurface = Color(0xFF2C211B),
        inverseOnSurface = Color(0xFFF6ECE0),
        error = Color(0xFFBA453B),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD5),
        onErrorContainer = Color(0xFF410905),
        outline = Color(0xFFA8947F),
        outlineVariant = Color(0xFFD7C6B1),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFFFBF5),
        surfaceDim = Color(0xFFDFD3C2),
        surfaceContainerLowest = Color(0xFFFFFDF9),
        surfaceContainerLow = Color(0xFFF6EFE4),
        surfaceContainer = Color(0xFFF1E7D8),
        surfaceContainerHigh = Color(0xFFEADDCB),
        surfaceContainerHighest = Color(0xFFE2D2BC),
    )

    // Natural Oak prefers warm light daylight even if dark mode is active, while keeping dark readable
    override val darkScheme = lightScheme
}

internal object CherryBlossomColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE07A94),
        onPrimary = Color(0xFF2B0812),
        primaryContainer = Color(0xFF592334),
        onPrimaryContainer = Color(0xFFFFD9E2),
        inversePrimary = Color(0xFFA44560),
        secondary = Color(0xFFDFB982),
        onSecondary = Color(0xFF261808),
        secondaryContainer = Color(0xFF452D1E),
        onSecondaryContainer = Color(0xFFF9E2BE),
        tertiary = Color(0xFF83AA8A),
        onTertiary = Color(0xFF0E2112),
        tertiaryContainer = Color(0xFF253A29),
        onTertiaryContainer = Color(0xFFCDE6D1),
        background = Color(0xFF150C0F),
        onBackground = Color(0xFFF7ECE8),
        surface = Color(0xFF150C0F),
        onSurface = Color(0xFFF7ECE8),
        surfaceVariant = Color(0xFF24151A),
        onSurfaceVariant = Color(0xFFC4AEAC),
        surfaceTint = Color(0xFFE07A94),
        inverseSurface = Color(0xFFF7ECE8),
        inverseOnSurface = Color(0xFF24151A),
        error = Color(0xFFE27368),
        onError = Color(0xFF2D0A07),
        errorContainer = Color(0xFF59211C),
        onErrorContainer = Color(0xFFFFDAD5),
        outline = Color(0xFF634148),
        outlineVariant = Color(0xFF472C33),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF3E242B),
        surfaceDim = Color(0xFF10080B),
        surfaceContainerLowest = Color(0xFF0E0709),
        surfaceContainerLow = Color(0xFF1C1014),
        surfaceContainer = Color(0xFF24151A),
        surfaceContainerHigh = Color(0xFF2F1C22),
        surfaceContainerHighest = Color(0xFF3A222A),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFB54967),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3F0B1B),
        inversePrimary = Color(0xFFFFB0C4),
        secondary = Color(0xFF8F6230),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF8E1C1),
        onSecondaryContainer = Color(0xFF301D07),
        tertiary = Color(0xFF4D7B57),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD2EAD7),
        onTertiaryContainer = Color(0xFF102A16),
        background = Color(0xFFFDF5F6),
        onBackground = Color(0xFF26171A),
        surface = Color(0xFFFDF5F6),
        onSurface = Color(0xFF26171A),
        surfaceVariant = Color(0xFFF0DFE2),
        onSurfaceVariant = Color(0xFF614A4E),
        surfaceTint = Color(0xFFB54967),
        inverseSurface = Color(0xFF332024),
        inverseOnSurface = Color(0xFFFBEEF0),
        error = Color(0xFFBA453B),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD5),
        onErrorContainer = Color(0xFF410905),
        outline = Color(0xFFAA8E93),
        outlineVariant = Color(0xFFDEC8CC),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFFFAFB),
        surfaceDim = Color(0xFFE4D1D5),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF9ECEE),
        surfaceContainer = Color(0xFFF4E4E7),
        surfaceContainerHigh = Color(0xFFEDDADF),
        surfaceContainerHighest = Color(0xFFE6CFD5),
    )
}

internal fun ColorScheme.withYomuAccent(
    accentColor: YomuAccentColor,
    isLight: Boolean,
): ColorScheme {
    if (accentColor == YomuAccentColor.AMETHYST) return this
    return when (accentColor) {
        YomuAccentColor.AMETHYST -> this
        YomuAccentColor.ANTIQUE_GOLD -> copy(
            primary = if (isLight) Color(0xFF9C6D2B) else Color(0xFFD6AD73),
            onPrimary = if (isLight) Color(0xFFFFFFFF) else Color(0xFF211507),
            primaryContainer = if (isLight) Color(0xFFF7E2C1) else Color(0xFF4B361B),
            onPrimaryContainer = if (isLight) Color(0xFF2F1D06) else Color(0xFFFFE7C2),
            surfaceTint = if (isLight) Color(0xFF9C6D2B) else Color(0xFFD6AD73),
        )
        YomuAccentColor.SAKURA_ROSE -> copy(
            primary = if (isLight) Color(0xFFB54967) else Color(0xFFE07A94),
            onPrimary = if (isLight) Color(0xFFFFFFFF) else Color(0xFF2B0812),
            primaryContainer = if (isLight) Color(0xFFFFD9E2) else Color(0xFF592334),
            onPrimaryContainer = if (isLight) Color(0xFF3F0B1B) else Color(0xFFFFD9E2),
            surfaceTint = if (isLight) Color(0xFFB54967) else Color(0xFFE07A94),
        )
        YomuAccentColor.JADE_SAGE -> copy(
            primary = if (isLight) Color(0xFF3F7751) else Color(0xFF83AA8A),
            onPrimary = if (isLight) Color(0xFFFFFFFF) else Color(0xFF0D2112),
            primaryContainer = if (isLight) Color(0xFFCDE9D4) else Color(0xFF233B28),
            onPrimaryContainer = if (isLight) Color(0xFF0F2A16) else Color(0xFFD2ECD7),
            surfaceTint = if (isLight) Color(0xFF3F7751) else Color(0xFF83AA8A),
        )
        YomuAccentColor.IMPERIAL_CRIMSON -> copy(
            primary = if (isLight) Color(0xFFB33A3A) else Color(0xFFDF6860),
            onPrimary = if (isLight) Color(0xFFFFFFFF) else Color(0xFF2E0806),
            primaryContainer = if (isLight) Color(0xFFFFDAD6) else Color(0xFF5C211D),
            onPrimaryContainer = if (isLight) Color(0xFF410604) else Color(0xFFFFDAD6),
            surfaceTint = if (isLight) Color(0xFFB33A3A) else Color(0xFFDF6860),
        )
    }
}
