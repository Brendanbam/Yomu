package eu.kanade.presentation.manga.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import eu.kanade.presentation.components.DownloadDropdownMenu
import eu.kanade.presentation.manga.DownloadAction
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuColors
import eu.kanade.presentation.theme.yomu.YomuPrimaryButton
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.presentation.theme.yomu.YomuSpacing
import eu.kanade.presentation.theme.yomu.YomuSurfaceCard
import eu.kanade.presentation.theme.yomu.yomuFocusHighlight
import eu.kanade.tachiyomi.source.model.SManga
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Bookmark
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.EditNote
import mihon.icons.materialsymbols.rounded.HourglassEmpty
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.materialsymbols.rounded.Schedule
import mihon.icons.materialsymbols.rounded.Sync
import mihon.icons.materialsymbols.roundedfilled.Bookmark
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

enum class YomuMangaDetailTab {
    CHAPTERS,
    TRACKING,
    DETAILS,
}

/**
 * Immersive cinematic Featured Book header matching 01_Primary_Design/03_Series_Details.png:
 * full-bleed cover artwork fading smoothly into espresso background, bold title, author,
 * metadata pill chips, expandable description, prominent Amethyst Resume/Start CTA +
 * bookmark & download square action buttons.
 */
@Composable
fun YomuMangaHeroHeader(
    manga: Manga,
    sourceName: String,
    chapterCount: Int,
    readCount: Int,
    hasReadChapters: Boolean,
    hasUnreadChapters: Boolean,
    appBarPadding: Dp,
    onCoverClick: () -> Unit,
    onContinueReading: () -> Unit,
    onAddToLibraryClicked: () -> Unit,
    onEditCategoryClicked: (() -> Unit)?,
    onDownloadActionClicked: ((DownloadAction) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    val backgroundColor = MaterialTheme.colorScheme.background
    var descriptionExpanded by rememberSaveable { mutableStateOf(false) }

    val statusText = when (manga.status.toInt()) {
        SManga.ONGOING -> stringResource(MR.strings.ongoing)
        SManga.COMPLETED -> stringResource(MR.strings.completed)
        SManga.LICENSED -> stringResource(MR.strings.licensed)
        SManga.PUBLISHING_FINISHED -> stringResource(MR.strings.publishing_finished)
        SManga.CANCELLED -> stringResource(MR.strings.cancelled)
        SManga.ON_HIATUS -> stringResource(MR.strings.on_hiatus)
        else -> stringResource(MR.strings.unknown)
    }

    val authorArtist = remember(manga.author, manga.artist) {
        val a = manga.author?.trim().orEmpty()
        val ar = manga.artist?.trim().orEmpty()
        when {
            a.isNotEmpty() && ar.isNotEmpty() && a != ar -> "$a · $ar"
            a.isNotEmpty() -> a
            ar.isNotEmpty() -> ar
            else -> null
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Full-bleed edge-to-edge cinematic cover artwork backdrop
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
        )

        // Smooth espresso & radial vignette transition into page body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        0f to backgroundColor.copy(alpha = 0.25f),
                        0.38f to backgroundColor.copy(alpha = 0.52f),
                        0.76f to backgroundColor.copy(alpha = 0.94f),
                        1f to backgroundColor,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.horizontalGradient(
                        0f to backgroundColor.copy(alpha = 0.85f),
                        0.58f to backgroundColor.copy(alpha = 0.25f),
                        1f to Color.Transparent,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = appBarPadding + 68.dp,
                    start = YomuSpacing.lg,
                    end = YomuSpacing.lg,
                    bottom = 12.dp,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp,
                            shadow = if (style.shelfPalette.isLight) {
                                Shadow.None
                            } else {
                                Shadow(Color.Black.copy(alpha = 0.65f), Offset(0f, 2f), 6f)
                            },
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!authorArtist.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "By $authorArtist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Physical miniature cover preview (tappable to open Cover Dialog!)
                Box(
                    modifier = Modifier
                        .width(84.dp)
                        .aspectRatio(MangaCover.Book.ratio)
                        .shadow(10.dp, YomuShapes.bookCover)
                        .clip(YomuShapes.bookCover)
                        .border(1.2.dp, style.shelfPalette.goldAccent.copy(alpha = 0.55f), YomuShapes.bookCover)
                        .clickable(role = Role.Button, onClick = onCoverClick),
                ) {
                    AsyncImage(
                        model = manga,
                        contentDescription = manga.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata pill chips (Source, Status, Chapters) matching 03_Series_Details.png
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetadataPill(
                    icon = MaterialSymbols.Rounded.Book,
                    text = sourceName,
                )
                MetadataPill(
                    icon = MaterialSymbols.Rounded.Schedule,
                    text = statusText,
                )
                MetadataPill(
                    icon = MaterialSymbols.Rounded.CollectionsBookmark,
                    text = if (readCount > 0 && chapterCount > 0) {
                        "$readCount/$chapterCount Chapters"
                    } else {
                        "$chapterCount Chapters"
                    },
                )
            }

            // Expandable synopsis
            val desc = manga.description?.trim()
            if (!desc.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                    maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { descriptionExpanded = !descriptionExpanded },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action Row: wide Amethyst Resume/Start button + Favorite + Download
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YomuPrimaryButton(
                    text = stringResource(
                        if (hasReadChapters) {
                            MR.strings.yomu_home_resume_reading
                        } else {
                            MR.strings.yomu_home_start_reading
                        },
                    ),
                    icon = MaterialSymbols.RoundedFilled.PlayArrow,
                    onClick = onContinueReading,
                    enabled = hasUnreadChapters || chapterCount > 0,
                    modifier = Modifier.weight(1f),
                )

                // Library / Favorite square button (long-press edits categories!)
                Surface(
                    shape = YomuShapes.button,
                    color = if (manga.favorite) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        style.shelfPalette.cardSurface
                    },
                    contentColor = if (manga.favorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (manga.favorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            style.shelfPalette.subtleBorder
                        },
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .yomuFocusHighlight(YomuShapes.button)
                        .combinedClickable(
                            role = Role.Button,
                            onClick = onAddToLibraryClicked,
                            onLongClick = onEditCategoryClicked,
                        ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (manga.favorite) {
                                MaterialSymbols.RoundedFilled.Bookmark
                            } else {
                                MaterialSymbols.Rounded.Bookmark
                            },
                            contentDescription = stringResource(
                                if (manga.favorite) MR.strings.in_library else MR.strings.add_to_library,
                            ),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Explicit Download Options menu (never silently queues hardcoded chapter count)
                if (onDownloadActionClicked != null) {
                    var downloadMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { downloadMenuExpanded = true },
                            shape = YomuShapes.button,
                            color = style.shelfPalette.cardSurface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                            modifier = Modifier
                                .size(48.dp)
                                .yomuFocusHighlight(YomuShapes.button),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Download,
                                    contentDescription = stringResource(MR.strings.yomu_details_download_options),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                        DownloadDropdownMenu(
                            expanded = downloadMenuExpanded,
                            onDismissRequest = { downloadMenuExpanded = false },
                            onDownloadClicked = onDownloadActionClicked,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(
    icon: ImageVector,
    text: String,
) {
    val style = LocalYomuStyle.current
    Surface(
        shape = YomuShapes.pill,
        color = style.shelfPalette.cardSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = style.shelfPalette.goldAccent,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Clean 3-tab strip matching 03_Series_Details.png: Chapters | Tracking | Details
 */
@Composable
fun YomuMangaTabRow(
    selectedTab: YomuMangaDetailTab,
    trackingCount: Int,
    onSelectTab: (YomuMangaDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = YomuSpacing.lg),
        ) {
            val tabs = listOf(
                YomuMangaDetailTab.CHAPTERS to stringResource(MR.strings.yomu_tab_chapters),
                YomuMangaDetailTab.TRACKING to if (trackingCount > 0) {
                    "${stringResource(MR.strings.yomu_tab_tracking)} ($trackingCount)"
                } else {
                    stringResource(MR.strings.yomu_tab_tracking)
                },
                YomuMangaDetailTab.DETAILS to stringResource(MR.strings.yomu_tab_details),
            )
            tabs.forEach { (tab, label) ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Tab) { onSelectTab(tab) }
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(2.5.dp)
                            .clip(YomuShapes.pill)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            ),
                    )
                }
            }
        }
        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.7f))
    }
}

/**
 * Refined Tracking tab card wired directly to Mihon's TrackInfoDialog & tracking services.
 */
@Composable
fun YomuMangaTrackingCard(
    trackingCount: Int,
    hasLoggedInTrackers: Boolean,
    readCount: Int,
    totalChapters: Int,
    onTrackingClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(YomuSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        YomuSurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTrackingClicked,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Sync,
                        contentDescription = null,
                        tint = style.shelfPalette.goldAccent,
                        modifier = Modifier.size(22.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (trackingCount > 0) {
                                pluralStringResource(MR.plurals.num_trackers, count = trackingCount, trackingCount)
                            } else {
                                stringResource(MR.strings.manga_tracking_tab)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (hasLoggedInTrackers) {
                                "Sync chapter progress ($readCount / $totalChapters read), status, and scores"
                            } else {
                                "Sign in to AniList, MyAnimeList, Kitsu, Shikimori, or other trackers"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                YomuPrimaryButton(
                    text = if (hasLoggedInTrackers) "Manage Trackers" else "Configure Trackers",
                    icon = MaterialSymbols.Rounded.Sync,
                    onClick = onTrackingClicked,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Refined Details tab displaying genres, notes, source website, and update interval controls.
 */
@Composable
fun YomuMangaDetailsPanel(
    manga: Manga,
    sourceName: String,
    onTagSearch: (String) -> Unit,
    onCopyTagToClipboard: (String) -> Unit,
    onEditNotesClicked: () -> Unit,
    onEditIntervalClicked: (() -> Unit)?,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(YomuSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (!manga.description.isNullOrBlank()) {
            YomuSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleSmall,
                        color = style.shelfPalette.goldAccent,
                    )
                    Text(
                        text = manga.description!!.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val genres = manga.genre.orEmpty()
        if (genres.isNotEmpty()) {
            YomuSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Genres & Tags",
                        style = MaterialTheme.typography.titleSmall,
                        color = style.shelfPalette.goldAccent,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        genres.forEach { tag ->
                            Surface(
                                shape = YomuShapes.pill,
                                color = style.shelfPalette.elevatedSurface,
                                border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                                modifier = Modifier.combinedClickable(
                                    onClick = { onTagSearch(tag) },
                                    onLongClick = { onCopyTagToClipboard(tag) },
                                ),
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        YomuSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Volume Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = style.shelfPalette.goldAccent,
                )
                DetailMetadataRow("Source", sourceName)
                if (!manga.author.isNullOrBlank()) DetailMetadataRow("Author", manga.author!!)
                if (!manga.artist.isNullOrBlank()) DetailMetadataRow("Artist", manga.artist!!)
            }
        }

        // Quick actions for Notes, Update Interval, and Source WebView
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YomuSurfaceCard(
                modifier = Modifier.weight(1f),
                onClick = onEditNotesClicked,
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.EditNote,
                        contentDescription = null,
                        tint = style.shelfPalette.goldAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(MR.strings.action_edit_notes),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onEditIntervalClicked != null) {
                YomuSurfaceCard(
                    modifier = Modifier.weight(1f),
                    onClick = onEditIntervalClicked,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.HourglassEmpty,
                            contentDescription = null,
                            tint = style.shelfPalette.goldAccent,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (onWebViewClicked != null) {
                Surface(
                    shape = YomuShapes.card,
                    color = style.shelfPalette.cardSurface,
                    border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = onWebViewClicked,
                            onLongClick = onWebViewLongClicked,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Public,
                            contentDescription = null,
                            tint = style.shelfPalette.goldAccent,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(MR.strings.action_web_view),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
