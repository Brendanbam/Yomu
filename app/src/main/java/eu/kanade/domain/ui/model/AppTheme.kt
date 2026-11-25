package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR

enum class AppTheme(val titleRes: StringResource?) {
    MIDNIGHT_WALNUT(MR.strings.theme_yomu_midnight_walnut),
    BLACK_EBONY(MR.strings.theme_yomu_black_ebony),
    NATURAL_OAK(MR.strings.theme_yomu_natural_oak),
    CHERRY_BLOSSOM(MR.strings.theme_yomu_cherry_blossom),
    DEFAULT(MR.strings.theme_original_mihon),
    MONET(MR.strings.theme_monet),
    CATPPUCCIN(MR.strings.theme_catppuccin),
    GREEN_APPLE(MR.strings.theme_greenapple),
    LAVENDER(MR.strings.theme_lavender),
    MIDNIGHT_DUSK(MR.strings.theme_midnightdusk),
    NORD(MR.strings.theme_nord),
    STRAWBERRY_DAIQUIRI(MR.strings.theme_strawberrydaiquiri),
    TAKO(MR.strings.theme_tako),
    TEALTURQUOISE(MR.strings.theme_tealturquoise),
    TIDAL_WAVE(MR.strings.theme_tidalwave),
    YINYANG(MR.strings.theme_yinyang),
    YOTSUBA(MR.strings.theme_yotsuba),
    TOKYONIGHT(MR.strings.theme_tokyonight),
    MONOCHROME(MR.strings.theme_monochrome),

    // Deprecated
    DARK_BLUE(null),
    HOT_PINK(null),
    BLUE(null),
}
