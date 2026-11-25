package eu.kanade.domain.yomu

import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.domain.ui.model.YomuAccentColor
import eu.kanade.domain.ui.model.YomuHomeSection
import eu.kanade.domain.ui.model.YomuShelfMaterial
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.InMemoryPreferenceStore

class YomuHomePreferencesTest {

    @Test
    fun `default order parses canonical calm Home sections`() {
        YomuHomeSection.parseOrder(YomuHomeSection.defaultOrder) shouldBe YomuHomeSection.defaultSections
    }

    @Test
    fun `custom reordered subset deduplicates and skips unknown section ids`() {
        val parsed = YomuHomeSection.parseOrder("quick, continue, unknown_id, continue, reading")
        parsed shouldBe listOf(
            YomuHomeSection.QUICK_ACCESS,
            YomuHomeSection.CONTINUE_READING,
            YomuHomeSection.CURRENTLY_READING,
        )
    }

    @Test
    fun `disabling all sections persists explicit NONE sentinel and parses back as empty`() {
        val serialized = YomuHomeSection.serializeOrder(emptyList())
        serialized shouldBe YomuHomeSection.EMPTY_SENTINEL
        YomuHomeSection.parseOrder(serialized).shouldBeEmpty()
    }

    @Test
    fun `blank or invalid serialized values safely fall back to defaults`() {
        YomuHomeSection.parseOrder("") shouldBe YomuHomeSection.defaultSections
        YomuHomeSection.parseOrder("   ") shouldBe YomuHomeSection.defaultSections
        YomuHomeSection.parseOrder("bogus,invalid") shouldBe YomuHomeSection.defaultSections
    }

    @Test
    fun `resetYomuDefaults restores Midnight Walnut and default sanctuary preferences`() {
        val prefs = UiPreferences(InMemoryPreferenceStore())
        prefs.themeMode.set(ThemeMode.LIGHT)
        prefs.appTheme.set(AppTheme.NATURAL_OAK)
        prefs.yomuAccentColor.set(YomuAccentColor.IMPERIAL_CRIMSON)
        prefs.yomuShelfMaterial.set(YomuShelfMaterial.BLACK_EBONY)
        prefs.yomuLightingIntensity.set(20)
        prefs.yomuBookThickness.set(10)
        prefs.yomuShadowIntensity.set(0)
        prefs.yomuDecorationIntensity.set(15)
        prefs.yomuAnimationIntensity.set(0)
        prefs.yomuReducedMotion.set(true)
        prefs.yomuShowShelfTitles.set(false)
        prefs.yomuShowShelfProgress.set(false)
        prefs.yomuSmartShelvesEnabled.set(false)
        prefs.yomuTabletSplitPane.set(false)
        prefs.yomuHomeSectionsOrder.set(YomuHomeSection.EMPTY_SENTINEL)

        prefs.resetYomuDefaults()

        prefs.themeMode.get() shouldBe ThemeMode.DARK
        prefs.appTheme.get() shouldBe AppTheme.MIDNIGHT_WALNUT
        prefs.yomuAccentColor.get() shouldBe YomuAccentColor.AMETHYST
        prefs.yomuShelfMaterial.get() shouldBe YomuShelfMaterial.AUTO
        prefs.yomuLightingIntensity.get() shouldBe 75
        prefs.yomuBookThickness.get() shouldBe 70
        prefs.yomuShadowIntensity.get() shouldBe 80
        prefs.yomuDecorationIntensity.get() shouldBe 85
        prefs.yomuAnimationIntensity.get() shouldBe 80
        prefs.yomuReducedMotion.get() shouldBe false
        prefs.yomuShowShelfTitles.get() shouldBe true
        prefs.yomuShowShelfProgress.get() shouldBe true
        prefs.yomuSmartShelvesEnabled.get() shouldBe true
        prefs.yomuTabletSplitPane.get() shouldBe true
        prefs.yomuHomeSectionsOrder.get() shouldBe YomuHomeSection.defaultOrder
    }
}
