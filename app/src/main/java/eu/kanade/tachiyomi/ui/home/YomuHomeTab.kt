package eu.kanade.tachiyomi.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.LocalResultEventBus
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.home.YomuHomeScreen
import eu.kanade.presentation.more.settings.screen.SettingsDataScreen
import eu.kanade.presentation.more.settings.screen.openSettings
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import mihon.core.navigation.BrowseSourceRoute
import mihon.core.navigation.CategoryRoute
import mihon.core.navigation.DownloadQueueRoute
import mihon.core.navigation.HistoryRoute
import mihon.core.navigation.LocalNavigator
import mihon.core.navigation.MangaRoute
import mihon.core.navigation.StatsRoute
import mihon.presentation.core.util.isTabletUi

@Composable
fun YomuHomeTab() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val resultEventBus = LocalResultEventBus.current
    val isTablet = isTabletUi()

    val viewModel = metroViewModel<YomuHomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    YomuHomeScreen(
        state = state,
        onClickSettings = { navigator.openSettings(twoPane = isTablet) },
        onClickManga = { navigator.push(MangaRoute(it)) },
        onClickResume = { mangaId, targetChapterId ->
            context.startActivity(ReaderActivity.newIntent(context, mangaId, targetChapterId))
        },
        onClickHistory = { navigator.push(HistoryRoute) },
        onClickUpdates = { resultEventBus.sendResult<TabEvent>(result = TabEvent.Updates) },
        onClickLibrary = { resultEventBus.sendResult<TabEvent>(result = TabEvent.Library()) },
        onClickBrowse = { resultEventBus.sendResult<TabEvent>(result = TabEvent.Browse()) },
        onClickDownloads = { navigator.push(DownloadQueueRoute) },
        onClickCategories = { navigator.push(CategoryRoute) },
        onClickStats = { navigator.push(StatsRoute) },
        onClickRestoreBackup = { navigator.openSettings(SettingsDataScreen, isTablet) },
        onClickSource = { sourceId -> navigator.push(BrowseSourceRoute(sourceId, null)) },
        onRetry = viewModel::retry,
        onUpdateSectionsOrder = viewModel::updateSectionsOrder,
        onResetSectionsOrder = viewModel::resetSectionsOrder,
    )

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            (context as? MainActivity)?.ready = true
        }
    }
}
