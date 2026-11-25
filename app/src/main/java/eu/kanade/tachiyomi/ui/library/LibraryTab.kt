package eu.kanade.tachiyomi.ui.library

import androidx.activity.compose.BackHandler
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import eu.kanade.presentation.theme.yomu.YomuSerifFontFamily
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.tachiyomi.R
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
import mihon.icons.materialsymbols.automirroredrounded.OpenInNew
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

                    if (!useSplitPane) {
                        LibraryContent(
                            categories = state.displayedCategories,
                            searchQuery = state.searchQuery,
                            selection = state.selection,
                            contentPadding = contentPadding,
                            currentPage = state.coercedActiveCategoryIndex,
                            hasActiveFilters = state.hasActiveFilters,
                            showPageTabs = state.showCategoryTabs || !state.searchQuery.isNullOrEmpty(),
                            onChangeCurrentPage = viewModel::updateActiveCategoryIndex,
                            onClickManga = { navigator.push(MangaRoute(it)) },
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
                        )
                    } else {
                        BackHandler(enabled = inspectedMangaId != null) {
                            inspectedMangaId = null
                        }
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(0.56f)
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
                                        if (inspectedMangaId == mangaId) {
                                            navigator.push(MangaRoute(mangaId))
                                        } else {
                                            inspectedMangaId = mangaId
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
                                )
                            }

                            VerticalDivider(
                                thickness = 1.dp,
                                color = style.shelfPalette.subtleBorder,
                            )

                            Box(
                                modifier = Modifier
                                    .weight(0.44f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.background),
                            ) {
                                val selectedId = inspectedMangaId
                                if (selectedId != null) {
                                    SplitPaneMangaDetailsHost(
                                        mangaId = selectedId,
                                        onClose = { inspectedMangaId = null },
                                        onExpandFullScreen = { navigator.push(MangaRoute(selectedId)) },
                                    )
                                } else {
                                    SplitPaneEmptyInspectorPrompt(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(contentPadding),
                                    )
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
    val style = LocalYomuStyle.current
    val paneOwner = remember(mangaId) {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(paneOwner) {
        onDispose { paneOwner.viewModelStore.clear() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides paneOwner) {
            MangaScreen(
                mangaId = mangaId,
                fromSource = false,
                embeddedPane = true,
                onClosePane = onClose,
            )
        }
        Surface(
            shape = YomuShapes.pill,
            color = style.shelfPalette.cardSurface.copy(alpha = 0.92f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            IconButton(onClick = onExpandFullScreen) {
                Icon(
                    imageVector = MaterialSymbols.AutoMirroredRounded.OpenInNew,
                    contentDescription = stringResource(MR.strings.yomu_details_open_full),
                    tint = style.shelfPalette.goldAccent,
                )
            }
        }
    }
}

@Composable
private fun SplitPaneEmptyInspectorPrompt(
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_yomu),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(MR.strings.app_name),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = YomuSerifFontFamily,
                fontWeight = FontWeight.SemiBold,
            ),
            color = style.shelfPalette.goldAccent,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(MR.strings.yomu_details_select_book_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

data class LibrarySearchEvent(val query: String)
