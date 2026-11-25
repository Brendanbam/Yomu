package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eu.kanade.domain.yomu.SmartShelfEntry
import eu.kanade.domain.yomu.SmartShelfSnapshot
import eu.kanade.domain.yomu.YomuSmartShelf
import eu.kanade.domain.yomu.YomuSmartShelfClassifier
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuBookSpineVolume
import eu.kanade.presentation.theme.yomu.YomuBookshelfTier
import eu.kanade.presentation.theme.yomu.YomuPhysicalMangaVolume
import eu.kanade.presentation.theme.yomu.YomuPillChip
import eu.kanade.presentation.theme.yomu.YomuSpacing
import eu.kanade.presentation.theme.yomu.yomuFocusHighlight
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.FastScrollLazyVerticalGrid
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.plus
import tachiyomi.domain.manga.model.MangaCover as MangaCoverModel

internal fun LibraryItem.toSmartShelfEntry(): SmartShelfEntry {
    val lib = libraryManga
    return SmartShelfEntry(
        id = id,
        totalChapters = lib.totalChapters,
        readCount = lib.readCount,
        downloadCount = downloadCount,
        isLocal = isLocal,
        publicationStatus = lib.manga.status.toInt(),
        favoriteAt = lib.manga.favoriteAt,
        latestUpload = lib.latestUpload,
        chapterFetchedAt = lib.chapterFetchedAt,
        lastRead = lib.lastRead,
        hasNextReadableChapterDownloaded = hasNextReadableChapterDownloaded,
        hasFullOfflineArchive = hasFullOfflineArchive,
    )
}

internal fun List<LibraryItem>.classifySmartShelves(): SmartShelfSnapshot<LibraryItem> {
    return YomuSmartShelfClassifier.classify(this) { it.toSmartShelfEntry() }
}

@Composable
internal fun SmartShelvesChipRow(
    selectedShelf: YomuSmartShelf,
    snapshot: SmartShelfSnapshot<LibraryItem>,
    onSelectShelf: (YomuSmartShelf) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = YomuSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = YomuSmartShelf.entries,
            key = { "smart_${it.name}" },
        ) { shelf ->
            val count = snapshot.countFor(shelf)
            if (shelf == YomuSmartShelf.ALL || count > 0 || shelf == selectedShelf) {
                YomuPillChip(
                    label = stringResource(shelf.titleRes),
                    count = count,
                    selected = shelf == selectedShelf,
                    onClick = { onSelectShelf(shelf) },
                )
            }
        }
    }
}

/**
 * Row identities change when the inspector is resized or a device rotates.
 * Save the first visible book's identity, not just its old row index.
 */
@Composable
private fun rememberShelfListState(
    items: List<LibraryItem>,
    booksPerRow: Int,
    hasSearchHeader: Boolean,
): LazyListState {
    val state = rememberLazyListState()
    var anchorId by rememberSaveable { mutableStateOf<Long?>(null) }
    var previousColumns by rememberSaveable { mutableIntStateOf(booksPerRow) }
    val headerRows = if (hasSearchHeader) 1 else 0

    LaunchedEffect(items, booksPerRow, headerRows) {
        if (previousColumns != booksPerRow) {
            val anchor = anchorId
            val index = items.indexOfFirst { it.id == anchor }
            if (index >= 0) {
                state.scrollToItem(index / booksPerRow + headerRows)
            }
            previousColumns = booksPerRow
        }
        snapshotFlow { state.firstVisibleItemIndex }.collect { row ->
            val bookIndex = (row - headerRows) * booksPerRow
            anchorId = items.getOrNull(bookIndex)?.id
        }
    }
    return state
}

/**
 * Signature Yomu Bookshelf layout (Classic Bookshelf & Collector Edition).
 * Uses [BoxWithConstraints] against the actual available layout width so split-screen,
 * foldables, tablets, and split-pane layouts calculate exact column counts safely.
 */
@Composable
internal fun LibraryWoodenBookshelf(
    items: List<LibraryItem>,
    columns: Int,
    collectorEdition: Boolean,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
    inspectedMangaId: Long? = null,
) {
    val style = LocalYomuStyle.current
    val rowSpacing = if (collectorEdition) 18.dp else 12.dp
    val sidePad = if (collectorEdition) 20.dp else 14.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidthDp = maxWidth.value.toInt().coerceAtLeast(1)
        val minBookWidthDp = if (collectorEdition) 118 else 84
        val maxSafeColumns = (
            (availableWidthDp - sidePad.value.toInt() * 2 + rowSpacing.value.toInt()) /
                (minBookWidthDp + rowSpacing.value.toInt())
            )
            .coerceIn(1, 10)

        val booksPerRow = remember(columns, collectorEdition, availableWidthDp, maxSafeColumns) {
            when {
                columns > 0 -> columns.coerceIn(1, maxSafeColumns)
                collectorEdition -> when {
                    availableWidthDp >= 900 -> 4
                    availableWidthDp >= 600 -> 3
                    else -> 2
                }
                else -> when {
                    availableWidthDp >= 1000 -> 6
                    availableWidthDp >= 780 -> 5
                    availableWidthDp >= 560 -> 4
                    else -> 3
                }
            }.coerceAtMost(maxSafeColumns)
        }

        val rows = remember(items, booksPerRow) { items.chunked(booksPerRow) }
        val listState = rememberShelfListState(items, booksPerRow, !searchQuery.isNullOrEmpty())

        FastScrollLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(style.shelfPalette.alcoveBackBottom),
            contentPadding = contentPadding,
        ) {
            if (!searchQuery.isNullOrEmpty()) {
                item(key = "bookshelf_global_search") {
                    GlobalSearchItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        searchQuery = searchQuery,
                        onClick = onGlobalSearchClicked,
                    )
                }
            }

            itemsIndexed(
                items = rows,
                key = { _, books -> "shelf_${books.first().id}" },
                contentType = { _, _ -> "yomu_bookshelf_row" },
            ) { rowIndex, rowItems ->
                YomuBookshelfTier(
                    rowIndex = rowIndex,
                    horizontalPadding = sidePad,
                    itemSpacing = rowSpacing,
                    topAlcovePadding = if (collectorEdition) 18.dp else 12.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        for (slot in 0 until booksPerRow) {
                            val libraryItem = rowItems.getOrNull(slot)
                            if (libraryItem != null) {
                                val manga = libraryItem.libraryManga.manga
                                val libManga = libraryItem.libraryManga
                                val progress = if (libManga.totalChapters > 0 && libManga.readCount > 0) {
                                    (libManga.readCount.toFloat() / libManga.totalChapters.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    null
                                }
                                YomuPhysicalMangaVolume(
                                    coverData = MangaCoverModel(
                                        mangaId = manga.id,
                                        sourceId = manga.source,
                                        isMangaFavorite = manga.favorite,
                                        url = manga.thumbnailUrl,
                                        lastModified = manga.coverLastModified,
                                    ),
                                    title = manga.title,
                                    unreadCount = libraryItem.badges.unreadCount,
                                    downloadCount = libraryItem.badges.downloadCount,
                                    readProgress = progress,
                                    hasBookmarks = libManga.hasBookmarks,
                                    isSelected = manga.id in selection,
                                    isInspected = manga.id == inspectedMangaId,
                                    showTitle = style.showShelfTitles,
                                    showProgress = style.showShelfProgress,
                                    collectorMode = collectorEdition,
                                    onClick = { onClick(libManga) },
                                    onLongClick = { onLongClick(libManga) },
                                    onClickContinueReading = if (onClickContinueReading != null &&
                                        libraryItem.unreadCount > 0
                                    ) {
                                        { onClickContinueReading(libManga) }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vertical Book Spine shelf mode: books stand upright by their spines on wooden shelves.
 * Uses [BoxWithConstraints] so spine density stays readable and touch-friendly at every width.
 */
@Composable
internal fun LibraryBookSpineShelf(
    items: List<LibraryItem>,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    columns: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
    inspectedMangaId: Long? = null,
) {
    val style = LocalYomuStyle.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidthDp = maxWidth.value.toInt().coerceAtLeast(1)
        val minimumSpineWidth = (52 * LocalDensity.current.fontScale.coerceAtLeast(1f)).toInt()
        val maxSafeSpines = ((availableWidthDp - 24 + 4) / minimumSpineWidth).coerceIn(1, 16)
        val spinesPerRow = remember(columns, availableWidthDp, maxSafeSpines) {
            when {
                columns > 0 -> (columns.coerceAtMost(8) * 2).coerceIn(1, maxSafeSpines)
                availableWidthDp >= 900 -> 14
                availableWidthDp >= 600 -> 10
                else -> 7
            }.coerceAtMost(maxSafeSpines)
        }
        val rows = remember(items, spinesPerRow) { items.chunked(spinesPerRow) }
        val listState = rememberShelfListState(items, spinesPerRow, !searchQuery.isNullOrEmpty())

        FastScrollLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(style.shelfPalette.alcoveBackBottom),
            contentPadding = contentPadding,
        ) {
            if (!searchQuery.isNullOrEmpty()) {
                item(key = "spine_global_search") {
                    GlobalSearchItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        searchQuery = searchQuery,
                        onClick = onGlobalSearchClicked,
                    )
                }
            }

            itemsIndexed(
                items = rows,
                key = { _, books -> "spine_row_${books.first().id}" },
                contentType = { _, _ -> "yomu_spine_row" },
            ) { rowIndex, rowItems ->
                YomuBookshelfTier(
                    rowIndex = rowIndex,
                    horizontalPadding = 12.dp,
                    topAlcovePadding = 14.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        for (slot in 0 until spinesPerRow) {
                            val libraryItem = rowItems.getOrNull(slot)
                            if (libraryItem != null) {
                                val manga = libraryItem.libraryManga.manga
                                val libManga = libraryItem.libraryManga
                                val progress = if (libManga.totalChapters > 0 && libManga.readCount > 0) {
                                    (libManga.readCount.toFloat() / libManga.totalChapters.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    null
                                }
                                YomuBookSpineVolume(
                                    mangaId = manga.id,
                                    title = manga.title,
                                    onReadNext = onClickContinueReading?.takeIf { libManga.unreadCount > 0 }
                                        ?.let { action -> { action(libManga) } },
                                    unreadCount = libraryItem.badges.unreadCount,
                                    downloadCount = libraryItem.badges.downloadCount,
                                    readProgress = progress,
                                    isSelected = manga.id in selection,
                                    isInspected = manga.id == inspectedMangaId,
                                    onClick = { onClick(libManga) },
                                    onLongClick = { onLongClick(libManga) },
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Minimal Mode: clean, distraction-free Yomu presentation with zero wood textures.
 */
@Composable
internal fun LibraryMinimalGrid(
    items: List<LibraryItem>,
    columns: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    val style = LocalYomuStyle.current
    val shape = RoundedCornerShape(10.dp)

    FastScrollLazyVerticalGrid(
        columns = if (columns == 0) GridCells.Adaptive(118.dp) else GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        globalSearchItem(searchQuery, onGlobalSearchClicked)

        items(
            items = items,
            key = { "minimal_${it.id}" },
            contentType = { "yomu_minimal_item" },
        ) { libraryItem ->
            val manga = libraryItem.libraryManga.manga
            val isSelected = manga.id in selection
            val unreadLabel = stringResource(tachiyomi.i18n.MR.strings.yomu_home_unread_remaining, libraryItem.badges.unreadCount)
            val downloadedLabel = stringResource(tachiyomi.i18n.MR.strings.yomu_downloaded_count, libraryItem.badges.downloadCount)
            val accessibilityLabel = buildList {
                add(manga.title)
                if (libraryItem.badges.unreadCount > 0) add(unreadLabel)
                if (libraryItem.badges.downloadCount > 0) add(downloadedLabel)
            }.joinToString(", ")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .yomuFocusHighlight(shape)
                    .semantics {
                        contentDescription = accessibilityLabel
                        selected = isSelected
                    }
                    .combinedClickable(
                        role = Role.Button,
                        onClick = { onClick(libraryItem.libraryManga) },
                        onLongClick = { onLongClick(libraryItem.libraryManga) },
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(MangaCover.Book.ratio)
                        .clip(shape)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                style.shelfPalette.subtleBorder
                            },
                            shape = shape,
                        ),
                ) {
                    AsyncImage(
                        model = MangaCoverModel(
                            mangaId = manga.id,
                            sourceId = manga.source,
                            isMangaFavorite = manga.favorite,
                            url = manga.thumbnailUrl,
                            lastModified = manga.coverLastModified,
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (libraryItem.badges.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = libraryItem.badges.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                if (style.showShelfTitles) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
