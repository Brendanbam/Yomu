package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
        hasNextReadableChapterDownloaded = isLocal || (unreadCount > 0L && downloadCount > 0),
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
 * Signature Yomu Bookshelf layout (Classic Bookshelf & Collector Edition).
 * Renders rows of physical manga volumes resting flush on realistic dark walnut
 * (or Ebony / Oak / Cherry) wooden shelves with warm overhead lantern lighting.
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
) {
    val style = LocalYomuStyle.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val booksPerRow = remember(columns, collectorEdition, screenWidthDp) {
        when {
            columns > 0 -> columns.coerceIn(1, 10)
            collectorEdition -> when {
                screenWidthDp >= 900 -> 4
                screenWidthDp >= 600 -> 3
                else -> 2
            }
            else -> when {
                screenWidthDp >= 1000 -> 6
                screenWidthDp >= 800 -> 5
                screenWidthDp >= 600 -> 4
                else -> 3
            }
        }
    }

    val rows = remember(items, booksPerRow) { items.chunked(booksPerRow) }
    val rowSpacing = if (collectorEdition) 18.dp else 12.dp
    val sidePad = if (collectorEdition) 20.dp else 14.dp

    FastScrollLazyColumn(
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

        items(
            items = rows.withIndex().toList(),
            key = { (rowIdx, rowItems) -> "shelf_${rowIdx}_${rowItems.firstOrNull()?.id ?: 0L}" },
            contentType = { "yomu_bookshelf_row" },
        ) { (rowIndex, rowItems) ->
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

/**
 * Vertical Book Spine shelf mode: books stand upright by their spines on wooden shelves.
 */
@Composable
internal fun LibraryBookSpineShelf(
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
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val spinesPerRow = remember(columns, screenWidthDp) {
        when {
            columns > 0 -> (columns * 2).coerceIn(4, 16)
            screenWidthDp >= 900 -> 14
            screenWidthDp >= 600 -> 10
            else -> 7
        }
    }
    val rows = remember(items, spinesPerRow) { items.chunked(spinesPerRow) }

    FastScrollLazyColumn(
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

        items(
            items = rows.withIndex().toList(),
            key = { (rowIdx, rowItems) -> "spine_row_${rowIdx}_${rowItems.firstOrNull()?.id ?: 0L}" },
            contentType = { "yomu_spine_row" },
        ) { (rowIndex, rowItems) ->
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
                                unreadCount = libraryItem.badges.unreadCount,
                                downloadCount = libraryItem.badges.downloadCount,
                                readProgress = progress,
                                isSelected = manga.id in selection,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .yomuFocusHighlight(shape)
                    .combinedClickable(
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
                        contentDescription = manga.title,
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
