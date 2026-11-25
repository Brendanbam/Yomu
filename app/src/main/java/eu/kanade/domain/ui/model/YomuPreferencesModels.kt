package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR

enum class YomuAccentColor(val titleRes: StringResource) {
    AMETHYST(MR.strings.yomu_accent_amethyst),
    ANTIQUE_GOLD(MR.strings.yomu_accent_gold),
    SAKURA_ROSE(MR.strings.yomu_accent_rose),
    JADE_SAGE(MR.strings.yomu_accent_sage),
    IMPERIAL_CRIMSON(MR.strings.yomu_accent_crimson),
}

enum class YomuShelfMaterial(val titleRes: StringResource) {
    AUTO(MR.strings.yomu_material_auto),
    DARK_WALNUT(MR.strings.yomu_material_walnut),
    BLACK_EBONY(MR.strings.yomu_material_ebony),
    NATURAL_OAK(MR.strings.yomu_material_oak),
    CHERRY_WOOD(MR.strings.yomu_material_cherry),
}

enum class YomuHomeSection(val id: String, val titleRes: StringResource) {
    HERO_BANNER("hero", MR.strings.yomu_nav_home),
    CONTINUE_READING("continue", MR.strings.yomu_home_continue_reading),
    RECENTLY_UPDATED("updated", MR.strings.yomu_home_recently_updated),
    CURRENTLY_READING("reading", MR.strings.yomu_home_currently_reading),
    REDISCOVER("rediscover", MR.strings.yomu_home_rediscover),
    OFFLINE_COLLECTION("offline", MR.strings.yomu_home_offline_collection),
    QUICK_ACCESS("quick", MR.strings.yomu_home_quick_access),
    ;

    companion object {
        val defaultOrder: String = entries.joinToString(",") { it.id }

        fun parseOrder(serialized: String): List<YomuHomeSection> {
            val map = entries.associateBy { it.id }
            val parsed = serialized
                .split(",")
                .mapNotNull { map[it.trim()] }
                .distinct()
            return parsed.ifEmpty { entries }
        }
    }
}
