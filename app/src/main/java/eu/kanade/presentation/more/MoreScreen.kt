package eu.kanade.presentation.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuSpacing
import eu.kanade.presentation.theme.yomu.YomuSurfaceCard
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Help
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.CloudOff
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Eyeglasses2
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Schedule
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.Storage
import mihon.icons.materialsymbols.roundedfilled.VolunteerActivism
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickHistory: () -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickSupport: () -> Unit,
    onClickAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val style = LocalYomuStyle.current

    Scaffold { contentPadding ->
        ScrollbarLazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 20.dp,
            ),
        ) {
            item {
                LogoHeader(
                    iconPadding = PaddingValues(vertical = 28.dp),
                )
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                YomuSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = YomuSpacing.lg),
                ) {
                    Column {
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.label_downloaded_only),
                            subtitle = stringResource(MR.strings.downloaded_only_summary),
                            icon = MaterialSymbols.Rounded.CloudOff,
                            checked = downloadedOnly,
                            onCheckedChanged = onDownloadedOnlyChange,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.pref_incognito_mode),
                            subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                            icon = MaterialSymbols.Rounded.Eyeglasses2,
                            checked = incognitoMode,
                            onCheckedChanged = onIncognitoModeChange,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                val downloadQueueState = downloadQueueStateProvider()
                YomuSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = YomuSpacing.lg),
                ) {
                    Column {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.history),
                            icon = MaterialSymbols.Rounded.Schedule,
                            onPreferenceClick = onClickHistory,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_download_queue),
                            subtitle = when (downloadQueueState) {
                                DownloadQueueState.Stopped -> null
                                is DownloadQueueState.Paused -> {
                                    val pending = downloadQueueState.pending
                                    if (pending == 0) {
                                        stringResource(MR.strings.paused)
                                    } else {
                                        "${stringResource(MR.strings.paused)} • ${
                                            pluralStringResource(
                                                MR.plurals.download_queue_summary,
                                                count = pending,
                                                pending,
                                            )
                                        }"
                                    }
                                }
                                is DownloadQueueState.Downloading -> {
                                    val pending = downloadQueueState.pending
                                    pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                                }
                            },
                            icon = MaterialSymbols.Rounded.Download,
                            onPreferenceClick = onClickDownloadQueue,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.yomu_shelf_manager),
                            subtitle = stringResource(MR.strings.categories),
                            icon = MaterialSymbols.AutoMirroredRounded.Label,
                            onPreferenceClick = onClickCategories,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_stats),
                            icon = MaterialSymbols.Rounded.QueryStats,
                            onPreferenceClick = onClickStats,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_data_storage),
                            icon = MaterialSymbols.Rounded.Storage,
                            onPreferenceClick = onClickDataAndStorage,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                YomuSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = YomuSpacing.lg),
                ) {
                    Column {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_settings),
                            icon = MaterialSymbols.Rounded.Settings,
                            onPreferenceClick = onClickSettings,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_support_us),
                            icon = MaterialSymbols.RoundedFilled.VolunteerActivism,
                            onPreferenceClick = onClickSupport,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.pref_category_about),
                            icon = MaterialSymbols.Rounded.Info,
                            onPreferenceClick = onClickAbout,
                        )
                        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.5f))
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_help),
                            icon = MaterialSymbols.AutoMirroredRounded.Help,
                            onPreferenceClick = { uriHandler.openUri(Constants.URL_HELP) },
                        )
                    }
                }
            }
        }
    }
}
