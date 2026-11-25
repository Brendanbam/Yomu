package eu.kanade.presentation.more.settings.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.TabletUiMode
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.domain.ui.model.TimeFormat
import eu.kanade.domain.ui.model.YomuAccentColor
import eu.kanade.domain.ui.model.YomuShelfMaterial
import eu.kanade.domain.ui.model.YomuShelfPreset
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.widget.AppThemeModePreferenceWidget
import eu.kanade.presentation.more.settings.widget.AppThemePreferenceWidget
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuBookshelfTier
import eu.kanade.presentation.theme.yomu.YomuColors
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import mihon.app.di.appGraph
import mihon.core.navigation.AppLanguageRoute
import mihon.core.navigation.LocalNavigator
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import kotlin.time.Clock

@Serializable
data object SettingsAppearanceScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_appearance

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val uiPreferences = remember { context.appGraph.uiPreferences }

        return listOf(
            getThemeGroup(uiPreferences = uiPreferences),
            getYomuSanctuaryGroup(uiPreferences = uiPreferences),
            getDisplayGroup(uiPreferences = uiPreferences),
        )
    }

    @Composable
    private fun getYomuSanctuaryGroup(
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val context = LocalContext.current
        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.yomu_pref_category_sanctuary),
            preferenceItems = listOf(
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(MR.strings.yomu_pref_preview_shelf),
                ) {
                    YomuLiveBookshelfPreview()
                },
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(MR.strings.yomu_shelf_presets),
                ) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        YomuShelfPreset.entries.forEach { preset ->
                            item(key = preset.name) {
                                AssistChip(
                                    onClick = { uiPreferences.applyShelfPreset(preset) },
                                    label = { Text(stringResource(preset.titleRes)) },
                                )
                            }
                        }
                    }
                },
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.startOnLibrary,
                    title = stringResource(MR.strings.yomu_pref_start_on_library),
                    subtitle = stringResource(MR.strings.yomu_pref_start_on_library_summary),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.yomuTabletSplitPane,
                    title = stringResource(MR.strings.yomu_pref_tablet_split_pane),
                    subtitle = stringResource(MR.strings.yomu_pref_tablet_split_pane_summary),
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.yomuAccentColor,
                    entries = YomuAccentColor.entries.associateWith { stringResource(it.titleRes) },
                    title = stringResource(MR.strings.yomu_pref_accent_color),
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.yomuShelfMaterial,
                    entries = YomuShelfMaterial.entries.associateWith { stringResource(it.titleRes) },
                    title = stringResource(MR.strings.yomu_pref_shelf_material),
                ),
                Preference.PreferenceItem.SliderPreference(
                    preference = uiPreferences.yomuLightingIntensity,
                    title = stringResource(MR.strings.yomu_pref_lighting_intensity),
                    valueRange = 0..100 step 5,
                    valueText = { "$it%" },
                ),
                Preference.PreferenceItem.SliderPreference(
                    preference = uiPreferences.yomuBookThickness,
                    title = stringResource(MR.strings.yomu_pref_book_thickness),
                    valueRange = 0..100 step 5,
                    valueText = { "$it%" },
                ),
                Preference.PreferenceItem.SliderPreference(
                    preference = uiPreferences.yomuShadowIntensity,
                    title = stringResource(MR.strings.yomu_pref_shadow_intensity),
                    valueRange = 0..100 step 5,
                    valueText = { "$it%" },
                ),
                Preference.PreferenceItem.SliderPreference(
                    preference = uiPreferences.yomuDecorationIntensity,
                    title = stringResource(MR.strings.yomu_pref_decoration_intensity),
                    valueRange = 0..100 step 5,
                    valueText = { "$it%" },
                ),
                Preference.PreferenceItem.SliderPreference(
                    preference = uiPreferences.yomuAnimationIntensity,
                    title = stringResource(MR.strings.yomu_pref_animation_intensity),
                    valueRange = 0..100 step 5,
                    valueText = { "$it%" },
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.yomuShowShelfTitles,
                    title = stringResource(MR.strings.yomu_pref_show_shelf_titles),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.yomuShowShelfProgress,
                    title = stringResource(MR.strings.yomu_pref_show_shelf_progress),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.yomuSmartShelvesEnabled,
                    title = stringResource(MR.strings.yomu_pref_smart_shelves),
                    subtitle = stringResource(MR.strings.yomu_pref_smart_shelves_summary),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.yomuReducedMotion,
                    title = stringResource(MR.strings.yomu_pref_reduced_motion),
                    subtitle = stringResource(MR.strings.yomu_pref_reduced_motion_summary),
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(MR.strings.yomu_pref_reset_defaults),
                    subtitle = stringResource(MR.strings.yomu_pref_reset_defaults_summary),
                    onClick = {
                        uiPreferences.resetYomuDefaults()
                        setAppCompatDelegateThemeMode(ThemeMode.DARK)
                        context.toast(MR.strings.yomu_pref_reset_defaults)
                    },
                ),
            ),
        )
    }

    @Composable
    private fun getThemeGroup(
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val context = LocalContext.current

        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        val appThemePref = uiPreferences.appTheme
        val appTheme by appThemePref.collectAsState()

        val amoledPref = uiPreferences.themeDarkAmoled
        val amoled by amoledPref.collectAsState()

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_category_theme),
            preferenceItems = listOf(
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(MR.strings.pref_app_theme),
                ) {
                    Column {
                        AppThemeModePreferenceWidget(
                            value = themeMode,
                            onItemClick = {
                                themeModePref.set(it)
                                setAppCompatDelegateThemeMode(it)
                            },
                        )

                        AppThemePreferenceWidget(
                            value = appTheme,
                            amoled = amoled,
                            onItemClick = { appThemePref.set(it) },
                        )
                    }
                },
                Preference.PreferenceItem.SwitchPreference(
                    preference = amoledPref,
                    title = stringResource(MR.strings.pref_dark_theme_pure_black),
                    visible = themeMode != ThemeMode.LIGHT,
                    onValueChanged = {
                        (context as? Activity)?.let { ActivityCompat.recreate(it) }
                        true
                    },
                ),
            ),
        )
    }

    @Composable
    private fun getDisplayGroup(
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val locale = LocalLocale.current
        val is24HourSystem = rememberIs24HourSystem()

        val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime() }

        val dateFormat by uiPreferences.dateFormat.collectAsState()
        val formattedDateNow = remember(dateFormat) {
            UiPreferences.dateFormat(dateFormat, locale.platformLocale).format(now)
        }

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_category_display),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(MR.strings.pref_app_language),
                    onClick = { navigator.push(AppLanguageRoute) },
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.tabletUiMode,
                    entries = TabletUiMode.entries
                        .associateWith { stringResource(it.titleRes) },
                    title = stringResource(MR.strings.pref_tablet_ui_mode),
                    onValueChanged = {
                        context.toast(MR.strings.requires_app_restart)
                        true
                    },
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.dateFormat,
                    entries = DateFormats
                        .associateWith {
                            val formattedDate = UiPreferences.dateFormat(it, locale.platformLocale).format(now)
                            "${it.ifEmpty { stringResource(MR.strings.label_default) }} ($formattedDate)"
                        },
                    title = stringResource(MR.strings.pref_date_format),
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.timeFormat,
                    entries = TimeFormat.entries
                        .associateWith {
                            val formattedTime = UiPreferences.timeFormat(
                                it,
                                locale.platformLocale,
                                is24HourSystem,
                            ).format(now)
                            "${stringResource(it.titleRes)} ($formattedTime)"
                        },
                    title = stringResource(MR.strings.pref_time_format),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.relativeTime,
                    title = stringResource(MR.strings.pref_relative_format),
                    subtitle = stringResource(
                        MR.strings.pref_relative_format_summary,
                        stringResource(MR.strings.relative_time_today),
                        formattedDateNow,
                    ),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.imagesInDescription,
                    title = stringResource(MR.strings.pref_display_images_description),
                ),
            ),
        )
    }
}

@Composable
private fun YomuLiveBookshelfPreview() {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val primary = MaterialTheme.colorScheme.primary
    val sampleCovers = remember {
        listOf(
            Triple("Lantern Chronicles", listOf(Color(0xFF42273A), Color(0xFF1E1320)), 0.72f to 4),
            Triple("Midnight Tea House", listOf(Color(0xFF213A34), Color(0xFF10201C)), 0.35f to 12),
            Triple("Walnut Garden", listOf(Color(0xFF4D3220), Color(0xFF21140C)), 1.0f to 0),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(YomuShapes.card)
            .border(1.dp, palette.subtleBorder, YomuShapes.card),
    ) {
        YomuBookshelfTier(
            rowIndex = 0,
            horizontalPadding = 16.dp,
            topAlcovePadding = 14.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                sampleCovers.forEach { (title, gradient, meta) ->
                    val (progress, unread) = meta
                    val thicknessDp = (4.5f * (0.4f + 0.6f * style.bookThickness)).dp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(MangaCover.Book.ratio),
                    ) {
                        // Miniature parchment edge
                        if (thicknessDp > 1.dp) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(top = 2.dp, bottom = 1.dp)
                                    .clip(YomuShapes.bookCover)
                                    .background(palette.bookPageEdge),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = thicknessDp)
                                .clip(YomuShapes.bookCover)
                                .background(Brush.verticalGradient(gradient))
                                .border(0.8.dp, palette.goldAccent.copy(alpha = 0.35f), YomuShapes.bookCover),
                        ) {
                            if (style.showShelfTitles) {
                                Text(
                                    text = title,
                                    color = YomuColors.WarmIvory,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 6.dp, end = 6.dp, bottom = 7.dp),
                                )
                            }
                            if (style.showShelfProgress && progress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                            .background(primary),
                                    )
                                }
                            }
                        }
                        if (unread > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 3.dp, end = 3.dp)
                                    .clip(CircleShape)
                                    .background(primary)
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = unread.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val DateFormats = listOf(
    "", // Default
    "MM/dd/yy",
    "dd/MM/yy",
    "yyyy-MM-dd",
    "dd MMM yyyy",
    "MMM dd, yyyy",
)
