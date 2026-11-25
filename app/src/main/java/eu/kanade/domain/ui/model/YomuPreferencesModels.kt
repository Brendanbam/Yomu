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

enum class YomuShelfPreset(
    val titleRes: StringResource,
    val material: YomuShelfMaterial,
    val lighting: Int,
    val thickness: Int,
    val shadow: Int,
    val texture: Int,
) {
    WALNUT(MR.strings.yomu_material_walnut, YomuShelfMaterial.DARK_WALNUT, 75, 70, 80, 85),
    EBONY(MR.strings.yomu_material_ebony, YomuShelfMaterial.BLACK_EBONY, 40, 75, 90, 60),
    OAK(MR.strings.yomu_material_oak, YomuShelfMaterial.NATURAL_OAK, 90, 60, 35, 75),
    CHERRY(MR.strings.yomu_material_cherry, YomuShelfMaterial.CHERRY_WOOD, 70, 80, 65, 90),
}

enum class YomuHomeSection(val id: String, val titleRes: StringResource) {
    HERO_BANNER("hero", MR.strings.yomu_nav_home),
    CONTINUE_READING("continue", MR.strings.yomu_home_continue_reading),
    RECENTLY_UPDATED("updated", MR.strings.yomu_home_recently_updated),
    CURRENTLY_READING("reading", MR.strings.yomu_home_currently_reading),
    REDISCOVER("rediscover", MR.strings.yomu_home_rediscover),
    OFFLINE_COLLECTION("offline", MR.strings.yomu_home_offline_collection),
    READING_STATS("stats", MR.strings.yomu_home_section_stats),
    QUICK_ACCESS("quick", MR.strings.yomu_home_quick_access),
    ;

    companion object {
        const val EMPTY_SENTINEL: String = "NONE"

        val defaultOrder: String = entries.joinToString(",") { it.id }

        fun serializeOrder(sections: List<YomuHomeSection>): String {
            if (sections.isEmpty()) return EMPTY_SENTINEL
            return sections.distinct().joinToString(",") { it.id }
        }

        fun parseOrder(serialized: String): List<YomuHomeSection> {
            val trimmed = serialized.trim()
            if (trimmed.equals(EMPTY_SENTINEL, ignoreCase = true)) {
                return emptyList()
            }
            if (trimmed.isEmpty()) {
                return entries
            }
            val map = entries.associateBy { it.id }
            val parsed = trimmed
                .split(",")
                .mapNotNull { map[it.trim()] }
                .distinct()
            return parsed.ifEmpty { entries }
        }
    }
}
