package eu.kanade.tachiyomi.ui.library

import androidx.activity.compose.BackHandler
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.category.components.ChangeCategoryDialog
import eu.kanade.presentation.library.DeleteLibraryMangaDialog
import eu.kanade.presentation.library.LibrarySettingsDialog
import eu.kanade.presentation.library.components.LibraryContent
import eu.kanade.presentation.library.components.LibraryToolbar
import eu.kanade.presentation.manga.components.LibraryBottomActionMenu
import eu.kanade.presentation.more.onboarding.GETTING_STARTED_URL
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.presentation.theme.yomu.yomuFocusHighlight
import eu.kanade.tachiyomi.data.library.LibraryUpdateWorker
import eu.kanade.tachiyomi.ui.home.ShowBottomNavEvent
import eu.kanade.tachiyomi.ui.home.TopLevelRoute
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import mihon.core.navigation.CategoryRoute
import mihon.core.navigation.GlobalSearchRoute
import mihon.core.navigation.LocalNavigator
import mihon.core.navigation.MangaRoute
import mihon.core.navigation.MigrationConfigRoute
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Help
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.source.local.isLocal

@Composable
fun LibraryTab() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val resultBus = LocalResultEventBus.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val style = LocalYomuStyle.current

    val viewModel = metroViewModel<LibraryViewModel>()
    val settingsViewModel = metroViewModel<LibrarySettingsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val splitPaneEnabled by remember { context.appGraph.uiPreferences.yomuTabletSplitPane }.collectAsState()
    var inspectedMangaId by rememberSaveable { mutableStateOf<Long?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val onClickRefresh: suspend (Category?) -> Boolean = { category ->
        val started = LibraryUpdateWorker.startNow(context.workManager, category)
        scope.launch {
            val msgRes = when {
                !started -> MR.strings.update_already_running
                category != null -> MR.strings.updating_category
                else -> MR.strings.updating_library
            }
            snackbarHostState.showSnackbar(context.stringResource(msgRes))
        }
        started
    }

    Scaffold(
        topBar = { scrollBehavior ->
            val title = state.getToolbarTitle(
                defaultTitle = stringResource(MR.strings.label_library),
                defaultCategoryTitle = stringResource(MR.strings.label_default),
                page = state.coercedActiveCategoryIndex,
            )
            LibraryToolbar(
                hasActiveFilters = state.hasActiveFilters,
                selectedCount = state.selection.size,
                title = title,
                onClickUnselectAll = viewModel::clearSelection,
                onClickSelectAll = viewModel::selectAll,
                onClickInvertSelection = viewModel::invertSelection,
                onClickFilter = viewModel::showSettingsDialog,
                onClickRefresh = { scope.launch { onClickRefresh(state.activeCategory) } },
                onClickGlobalUpdate = { scope.launch { onClickRefresh(null) } },
                onClickUnifiedSearch = { navigator.push(GlobalSearchRoute(state.searchQuery.orEmpty())) },
                onClickOpenRandomManga = {
                    scope.launch {
                        val randomItem = viewModel.getRandomLibraryItemForCurrentCategory()
                        if (randomItem != null) {
                            navigator.push(MangaRoute(randomItem.libraryManga.manga.id))
                        } else {
                            snackbarHostState.showSnackbar(
                                context.stringResource(MR.strings.information_no_entries_found),
                            )
                        }
                    }
                },
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::search,
                // For scroll overlay when no tab
                scrollBehavior = scrollBehavior.takeIf { !state.showCategoryTabs },
            )
        },
        bottomBar = {
            LibraryBottomActionMenu(
                visible = state.selectionMode,
                onChangeCategoryClicked = viewModel::openChangeCategoryDialog,
                onMarkAsReadClicked = { viewModel.markReadSelection(true) },
                onMarkAsUnreadClicked = { viewModel.markReadSelection(false) },
                onDownloadClicked = viewModel::performDownloadAction
                    .takeIf { state.selectedManga.fastAll { !it.isLocal() } },
                onDeleteClicked = viewModel::openDeleteMangaDialog,
                onMigrateClicked = {
                    val selection = state.selection
                    viewModel.clearSelection()
                    navigator.push(MigrationConfigRoute(selection))
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        when {
            state.isLoading -> {
                LoadingScreen(Modifier.padding(contentPadding))
            }
            state.searchQuery.isNullOrEmpty() && !state.hasActiveFilters && state.isLibraryEmpty -> {
                val handler = LocalUriHandler.current
                EmptyScreen(
                    stringRes = MR.strings.information_empty_library,
                    modifier = Modifier.padding(contentPadding),
                    actions = listOf(
                        EmptyScreenAction(
                            stringRes = MR.strings.getting_started_guide,
                            icon = MaterialSymbols.AutoMirroredRounded.Help,
                            onClick = { handler.openUri(GETTING_STARTED_URL) },
                        ),
                    ),
                )
            }
            else -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useSplitPane = splitPaneEnabled && maxWidth >= 840.dp && !state.selectionMode
                    val showInspector = useSplitPane && inspectedMangaId != null
                    var libraryFraction by rememberSaveable { mutableStateOf(0.56f) }
                    val dividerWidth = 48.dp
                    val panelWidth = (maxWidth - dividerWidth).coerceAtLeast(1.dp)
                    val availableWidthPx = with(LocalDensity.current) { panelWidth.toPx() }
                    val minFraction = (360.dp / panelWidth).coerceIn(0.25f, 0.5f)
                    val maxFraction = (1f - 320.dp / panelWidth).coerceIn(0.5f, 0.75f)
                    val fraction = libraryFraction.coerceIn(minFraction, maxFraction)
                    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    val dividerLabel = stringResource(MR.strings.yomu_tablet_resize_panels)

                    BackHandler(enabled = showInspector) { inspectedMangaId = null }
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(if (showInspector) fraction else 1f)
                                .fillMaxHeight(),
                        ) {
                            LibraryContent(
                                categories = state.displayedCategories,
                                searchQuery = state.searchQuery,
                                selection = state.selection,
                                contentPadding = contentPadding,
                                currentPage = state.coercedActiveCategoryIndex,
                                hasActiveFilters = state.hasActiveFilters,
                                showPageTabs = state.showCategoryTabs || !state.searchQuery.isNullOrEmpty(),
                                onChangeCurrentPage = viewModel::updateActiveCategoryIndex,
                                onClickManga = { mangaId ->
                                    if (useSplitPane) {
                                        inspectedMangaId = mangaId
                                    } else {
                                        navigator.push(MangaRoute(mangaId))
                                    }
                                },
                                onContinueReadingClicked = { it: LibraryManga ->
                                    scope.launch {
                                        val chapter = viewModel.getNextUnreadChapter(it.manga)
                                        if (chapter != null) {
                                            context.startActivity(
                                                ReaderActivity.newIntent(context, chapter.mangaId, chapter.id),
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                context.stringResource(MR.strings.no_next_chapter),
                                            )
                                        }
                                    }
                                    Unit
                                }.takeIf { state.showMangaContinueButton },
                                onToggleSelection = viewModel::toggleSelection,
                                onToggleRangeSelection = { category, manga ->
                                    viewModel.toggleRangeSelection(category, manga)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onRefresh = { onClickRefresh(state.activeCategory) },
                                onGlobalSearchClicked = {
                                    navigator.push(GlobalSearchRoute(viewModel.state.value.searchQuery ?: ""))
                                },
                                getItemCountForCategory = { state.getItemCountForCategory(it) },
                                getDisplayMode = { viewModel.getDisplayMode() },
                                getColumnsForOrientation = { viewModel.getColumnsForOrientation(it) },
                                getItemsForCategory = { state.getItemsForCategory(it) },
                                inspectedMangaId = inspectedMangaId.takeIf { showInspector },
                            )
                        }
                        if (showInspector) {
                            Box(
                                modifier = Modifier
                                    .width(dividerWidth)
                                    .fillMaxHeight()
                                    .yomuFocusHighlight(YomuShapes.pill)
                                    .semantics {
                                        contentDescription = dividerLabel
                                        progressBarRangeInfo = ProgressBarRangeInfo(
                                            fraction,
                                            minFraction..maxFraction,
                                        )
                                        setProgress { value ->
                                            libraryFraction = value.coerceIn(minFraction, maxFraction)
                                            true
                                        }
                                    }
                                    .onPreviewKeyEvent { event ->
                                        val delta = when (event.key) {
                                            Key.DirectionLeft -> -0.025f
                                            Key.DirectionRight -> 0.025f
                                            else -> 0f
                                        }
                                        if (event.type == KeyEventType.KeyDown && delta != 0f) {
                                            val direction = if (rtl) -1f else 1f
                                            val next = (fraction + delta * direction)
                                                .coerceIn(minFraction, maxFraction)
                                            libraryFraction = next
                                            next != fraction
                                        } else {
                                            false
                                        }
                                    }
                                    .focusable()
                                    .draggable(
                                        orientation = Orientation.Horizontal,
                                        reverseDirection = rtl,
                                        state = rememberDraggableState { delta ->
                                            if (availableWidthPx > 0f) {
                                                val current = libraryFraction.coerceIn(minFraction, maxFraction)
                                                libraryFraction = (current + delta / availableWidthPx)
                                                    .coerceIn(minFraction, maxFraction)
                                            }
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                VerticalDivider(color = style.shelfPalette.subtleBorder, thickness = 1.dp)
                                Box(
                                    modifier = Modifier
                                        .size(width = 5.dp, height = 40.dp)
                                        .background(style.shelfPalette.goldAccent, YomuShapes.pill),
                                )
                            }
                            val inspectorPadding = PaddingValues(top = contentPadding.calculateTopPadding())
                            Box(
                                modifier = Modifier
                                    .weight(1f - fraction)
                                    .fillMaxHeight()
                                    .padding(inspectorPadding)
                                    .consumeWindowInsets(inspectorPadding)
                                    .background(MaterialTheme.colorScheme.background),
                            ) {
                                inspectedMangaId?.let { mangaId ->
                                    key(mangaId) {
                                        SplitPaneMangaDetailsHost(
                                            mangaId = mangaId,
                                            onClose = { inspectedMangaId = null },
                                            onExpandFullScreen = { navigator.push(MangaRoute(mangaId)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val onDismissRequest = viewModel::closeDialog
    when (val dialog = state.dialog) {
        is LibraryViewModel.Dialog.SettingsSheet -> run {
            LibrarySettingsDialog(
                onDismissRequest = onDismissRequest,
                viewModel = settingsViewModel,
                category = state.activeCategory,
            )
        }
        is LibraryViewModel.Dialog.ChangeCategory -> {
            ChangeCategoryDialog(
                initialSelection = dialog.initialSelection,
                onDismissRequest = onDismissRequest,
                onEditCategories = {
                    viewModel.clearSelection()
                    navigator.push(CategoryRoute)
                },
                onConfirm = { include, exclude ->
                    viewModel.clearSelection()
                    viewModel.setMangaCategories(dialog.manga, include, exclude)
                },
            )
        }
        is LibraryViewModel.Dialog.DeleteManga -> {
            DeleteLibraryMangaDialog(
                containsLocalManga = dialog.manga.any(Manga::isLocal),
                onDismissRequest = onDismissRequest,
                onConfirm = { deleteManga, deleteChapter ->
                    if (inspectedMangaId != null && dialog.manga.any { it.id == inspectedMangaId }) {
                        inspectedMangaId = null
                    }
                    viewModel.removeMangas(dialog.manga, deleteManga, deleteChapter)
                    viewModel.clearSelection()
                },
            )
        }
        null -> {}
    }

    BackHandler(enabled = state.selectionMode || state.searchQuery != null) {
        when {
            state.selectionMode -> viewModel.clearSelection()
            state.searchQuery != null -> viewModel.search(null)
        }
    }

    LaunchedEffect(state.selectionMode, state.dialog) {
        resultBus.sendResult(
            result = ShowBottomNavEvent(!state.selectionMode),
        )
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            (context as? MainActivity)?.ready = true
        }
    }
    ReportDrawnWhen { !state.isLoading }

    ResultEffect<LibrarySearchEvent> {
        viewModel.search(it.query)
    }

    ResultEffect<Unit>(resultKey = TopLevelRoute.Library.reselectEventKey) {
        viewModel.showSettingsDialog()
    }
}

@Composable
private fun SplitPaneMangaDetailsHost(
    mangaId: Long,
    onClose: () -> Unit,
    onExpandFullScreen: () -> Unit,
) {
    val paneOwner = remember(mangaId) {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(paneOwner) {
        onDispose { paneOwner.viewModelStore.clear() }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides paneOwner) {
        MangaScreen(
            mangaId = mangaId,
            fromSource = false,
            embeddedPane = true,
            onClosePane = onClose,
            onExpandPane = onExpandFullScreen,
        )
    }
}

data class LibrarySearchEvent(val query: String)

