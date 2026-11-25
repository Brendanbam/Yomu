package eu.kanade.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.domain.ui.model.YomuHomeSection
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuBookshelfTier
import eu.kanade.presentation.theme.yomu.YomuColors
import eu.kanade.presentation.theme.yomu.YomuPhysicalMangaVolume
import eu.kanade.presentation.theme.yomu.YomuPrimaryButton
import eu.kanade.presentation.theme.yomu.YomuSectionHeader
import eu.kanade.presentation.theme.yomu.YomuSerifFontFamily
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.presentation.theme.yomu.YomuShortcutBar
import eu.kanade.presentation.theme.yomu.YomuSpacing
import eu.kanade.presentation.theme.yomu.YomuSurfaceCard
import eu.kanade.presentation.theme.yomu.yomuFocusHighlight
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.home.YomuHomeViewModel
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.ArrowDownward
import mihon.icons.materialsymbols.rounded.ArrowUpward
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Schedule
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.Storage
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import mihon.presentation.core.util.isTabletUi
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen
import kotlin.math.roundToInt

@Composable
fun YomuHomeScreen(
    state: YomuHomeViewModel.State,
    onClickSettings: () -> Unit,
    onClickManga: (Long) -> Unit,
    onClickResume: (mangaId: Long, chapterId: Long) -> Unit,
    onClickHistory: () -> Unit,
    onClickUpdates: () -> Unit,
    onClickLibrary: () -> Unit,
    onClickBrowse: () -> Unit,
    onClickDownloads: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickRestoreBackup: () -> Unit,
    onClickSource: (Long) -> Unit,
    onUpdateSectionsOrder: (List<YomuHomeSection>) -> Unit,
) {
    val style = LocalYomuStyle.current
    val isTablet = isTabletUi()
    var showCustomizeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_yomu),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            text = stringResource(MR.strings.app_name),
                            fontFamily = YomuSerifFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCustomizeSheet = true }) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.FilterList,
                            contentDescription = stringResource(MR.strings.yomu_home_customize),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onClickSettings) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Settings,
                            contentDescription = stringResource(MR.strings.label_settings),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { contentPadding ->
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
            state.isEmpty -> YomuHomeEmptySanctuary(
                modifier = Modifier.padding(contentPadding),
                onClickBrowse = onClickBrowse,
                onClickRestoreBackup = onClickRestoreBackup,
                onClickLibrary = onClickLibrary,
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding() + 4.dp,
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.sectionsOrder.forEach { section ->
                        when (section) {
                            YomuHomeSection.HERO_BANNER -> {
                                if (state.heroCover != null) {
                                    item(key = "yomu_home_hero") {
                                        YomuCinematicHeroBanner(
                                            cover = state.heroCover,
                                            title = state.heroTitle,
                                            libraryCount = state.libraryCount,
                                            chaptersRead = state.totalChaptersRead,
                                            isTablet = isTablet,
                                            onClick = {
                                                state.heroMangaId?.let(onClickManga) ?: onClickLibrary()
                                            },
                                        )
                                    }
                                }
                            }
                            YomuHomeSection.CONTINUE_READING -> {
                                val item = state.continueReading
                                if (item != null) {
                                    item(key = "yomu_home_continue") {
                                        Column {
                                            YomuSectionHeader(
                                                title = stringResource(MR.strings.yomu_home_continue_reading),
                                                actionLabel = stringResource(MR.strings.yomu_home_view_all),
                                                onActionClick = onClickHistory,
                                            )
                                            ContinueReadingCard(
                                                item = item,
                                                onClickCard = { onClickManga(item.mangaId) },
                                                onClickResume = { onClickResume(item.mangaId, item.chapterId) },
                                            )
                                        }
                                    }
                                }
                            }
                            YomuHomeSection.RECENTLY_UPDATED -> {
                                if (state.recentlyUpdated.isNotEmpty()) {
                                    item(key = "yomu_home_updated") {
                                        Column {
                                            YomuSectionHeader(
                                                title = stringResource(MR.strings.yomu_home_recently_updated),
                                                actionLabel = stringResource(MR.strings.yomu_home_view_all),
                                                onActionClick = onClickUpdates,
                                            )
                                            LazyRow(
                                                contentPadding = PaddingValues(horizontal = YomuSpacing.lg),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                items(
                                                    items = state.recentlyUpdated,
                                                    key = { "recent_${it.update.mangaId}_${it.update.chapterId}" },
                                                ) { recent ->
                                                    RecentlyUpdatedBookCard(
                                                        item = recent,
                                                        onClick = { onClickManga(recent.update.mangaId) },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            YomuHomeSection.CURRENTLY_READING -> {
                                if (state.currentlyReading.isNotEmpty()) {
                                    item(key = "yomu_home_currently_reading") {
                                        val booksPerRow = if (isTablet) 5 else 3
                                        Column {
                                            YomuSectionHeader(
                                                title = stringResource(MR.strings.yomu_home_currently_reading),
                                                actionLabel = stringResource(MR.strings.yomu_home_view_all),
                                                onActionClick = onClickLibrary,
                                                accentDot = true,
                                            )
                                            HomeWoodenShelfStrip(
                                                books = state.currentlyReading.take(booksPerRow),
                                                rowIndex = 0,
                                                booksPerRow = booksPerRow,
                                                onClickManga = onClickManga,
                                            )
                                        }
                                    }
                                }
                            }
                            YomuHomeSection.REDISCOVER -> {
                                if (state.rediscover.isNotEmpty()) {
                                    item(key = "yomu_home_rediscover") {
                                        val booksPerRow = if (isTablet) 5 else 3
                                        Column {
                                            YomuSectionHeader(
                                                title = stringResource(MR.strings.yomu_home_rediscover),
                                                actionLabel = stringResource(MR.strings.yomu_home_view_all),
                                                onActionClick = onClickLibrary,
                                                accentDot = true,
                                            )
                                            HomeWoodenShelfStrip(
                                                books = state.rediscover.take(booksPerRow),
                                                rowIndex = 1,
                                                booksPerRow = booksPerRow,
                                                onClickManga = onClickManga,
                                            )
                                        }
                                    }
                                }
                            }
                            YomuHomeSection.OFFLINE_COLLECTION -> {
                                if (state.offlineCollection.isNotEmpty()) {
                                    item(key = "yomu_home_offline") {
                                        val booksPerRow = if (isTablet) 5 else 3
                                        Column {
                                            YomuSectionHeader(
                                                title = stringResource(MR.strings.yomu_home_offline_collection),
                                                actionLabel = stringResource(MR.strings.label_download_queue),
                                                onActionClick = onClickDownloads,
                                            )
                                            HomeWoodenShelfStrip(
                                                books = state.offlineCollection.take(booksPerRow),
                                                rowIndex = 2,
                                                booksPerRow = booksPerRow,
                                                onClickManga = onClickManga,
                                            )
                                        }
                                    }
                                }
                            }
                            YomuHomeSection.QUICK_ACCESS -> {
                                item(key = "yomu_home_quick_access") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = YomuSpacing.lg),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        // Signature History shortcut bar matching Primary Home reference
                                        YomuShortcutBar(
                                            title = stringResource(MR.strings.history),
                                            icon = MaterialSymbols.Rounded.Schedule,
                                            onClick = onClickHistory,
                                        )

                                        // Secondary quick access tiles (Downloads, Shelf Manager, Statistics)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            QuickAccessTile(
                                                title = stringResource(MR.strings.label_download_queue),
                                                icon = MaterialSymbols.Rounded.Download,
                                                badge = state.activeDownloadsCount.takeIf { it > 0 }?.toString(),
                                                tint = style.shelfPalette.sageAccent,
                                                onClick = onClickDownloads,
                                                modifier = Modifier.weight(1f),
                                            )
                                            QuickAccessTile(
                                                title = stringResource(MR.strings.yomu_shelf_manager),
                                                icon = MaterialSymbols.AutoMirroredRounded.Label,
                                                tint = style.shelfPalette.goldAccent,
                                                onClick = onClickCategories,
                                                modifier = Modifier.weight(1f),
                                            )
                                            QuickAccessTile(
                                                title = stringResource(MR.strings.label_stats),
                                                icon = MaterialSymbols.Rounded.QueryStats,
                                                tint = MaterialTheme.colorScheme.primary,
                                                onClick = onClickStats,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }

                                        // Pinned Sources quick strip when available
                                        if (state.pinnedSources.isNotEmpty()) {
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(top = 4.dp),
                                            ) {
                                                items(
                                                    items = state.pinnedSources,
                                                    key = { "home_src_${it.id}" },
                                                ) { source ->
                                                    Surface(
                                                        onClick = { onClickSource(source.id) },
                                                        shape = YomuShapes.pill,
                                                        color = style.shelfPalette.cardSurface,
                                                        border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                                                        modifier = Modifier.yomuFocusHighlight(YomuShapes.pill),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(
                                                                horizontal = 14.dp,
                                                                vertical = 8.dp,
                                                            ),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = MaterialSymbols.Rounded.Explore,
                                                                contentDescription = null,
                                                                tint = style.shelfPalette.goldAccent,
                                                                modifier = Modifier.size(15.dp),
                                                            )
                                                            Text(
                                                                text = source.name,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
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
                    }
                }
            }
        }
    }

    if (showCustomizeSheet) {
        HomeCustomizeBottomSheet(
            activeOrder = state.sectionsOrder,
            onDismiss = { showCustomizeSheet = false },
            onSave = {
                onUpdateSectionsOrder(it)
                showCustomizeSheet = false
            },
        )
    }
}

@Composable
private fun YomuCinematicHeroBanner(
    cover: tachiyomi.domain.manga.model.MangaCover,
    title: String?,
    libraryCount: Int,
    chaptersRead: Long,
    isTablet: Boolean,
    onClick: () -> Unit,
) {
    val style = LocalYomuStyle.current
    val bannerHeight = if (isTablet) 196.dp else 148.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = YomuSpacing.lg)
            .height(bannerHeight)
            .clip(YomuShapes.heroBanner)
            .border(1.dp, style.shelfPalette.subtleBorder, YomuShapes.heroBanner)
            .yomuFocusHighlight(YomuShapes.heroBanner)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        AsyncImage(
            model = cover,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        )
        // Warm nocturnal lantern vignette + espresso fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color(0xD8100D0B),
                        0.55f to Color(0x70100D0B),
                        1f to Color(0x40100D0B),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to style.shelfPalette.lanternGlow.copy(alpha = 0.08f * style.lightingIntensity),
                        0.6f to Color.Transparent,
                        1f to Color(0xE6100D0B),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = stringResource(MR.strings.yomu_tagline).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = style.shelfPalette.goldAccent,
            )
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(Color.Black, Offset(0f, 1.5f), 5f),
                    ),
                    color = YomuColors.WarmIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "$libraryCount volumes on shelf  ·  $chaptersRead chapters read",
                style = MaterialTheme.typography.bodySmall,
                color = YomuColors.SecondaryText,
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(
    item: YomuHomeViewModel.ContinueReadingItem,
    onClickCard: () -> Unit,
    onClickResume: () -> Unit,
) {
    val style = LocalYomuStyle.current
    val animatedProgress by animateFloatAsState(
        targetValue = item.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (style.reducedMotion) 0 else 450),
        label = "continueProgress",
    )
    val percent = (item.progressFraction * 100).roundToInt()

    YomuSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = YomuSpacing.lg),
        onClick = onClickCard,
        shape = YomuShapes.card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Physical volume cover thumbnail
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .aspectRatio(MangaCover.Book.ratio)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(8.dp)),
            ) {
                AsyncImage(
                    model = item.coverData,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val posDetail = buildString {
                    append(item.chapterLabel)
                    if (item.lastPageRead > 0 && !item.isChapterRead) {
                        append(" · Page ${item.lastPageRead + 1}")
                    }
                    if (percent > 0) {
                        append(" · $percent%")
                    }
                }
                Text(
                    text = posDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Amethyst progress track (matches 01_Home.png)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(YomuShapes.pill)
                        .background(style.shelfPalette.elevatedSurface),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress.coerceAtLeast(0.06f))
                            .clip(YomuShapes.pill)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Prominent Amethyst Play/Resume circular control (matches 01_Home.png)
            Surface(
                onClick = onClickResume,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.8.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(48.dp)
                    .yomuFocusHighlight(CircleShape),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                        contentDescription = stringResource(MR.strings.yomu_home_resume_reading),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyUpdatedBookCard(
    item: YomuHomeViewModel.RecentUpdateItem,
    onClick: () -> Unit,
) {
    val style = LocalYomuStyle.current
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .width(114.dp)
            .aspectRatio(MangaCover.Book.ratio)
            .clip(shape)
            .border(1.dp, style.shelfPalette.subtleBorder, shape)
            .yomuFocusHighlight(shape)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        AsyncImage(
            model = item.update.coverData,
            contentDescription = item.update.mangaTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.52f)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to Color(0xEE100D0B),
                    ),
                ),
        )
        // Amethyst unread badge in top-right (matches 01_Home.png)
        if (!item.update.read || item.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(1.5.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
        ) {
            Text(
                text = item.update.mangaTitle,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = YomuColors.WarmIvory,
                    shadow = Shadow(Color.Black, Offset(0f, 1f), 4f),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.update.chapterName,
                style = MaterialTheme.typography.labelSmall,
                color = YomuColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item.isDownloaded) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Check,
                contentDescription = null,
                tint = style.shelfPalette.sageAccent,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(15.dp),
            )
        }
    }
}

@Composable
private fun HomeWoodenShelfStrip(
    books: List<YomuHomeViewModel.HomeBookItem>,
    rowIndex: Int,
    booksPerRow: Int,
    onClickManga: (Long) -> Unit,
) {
    val style = LocalYomuStyle.current
    YomuBookshelfTier(
        rowIndex = rowIndex,
        horizontalPadding = 16.dp,
        topAlcovePadding = 14.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            for (slot in 0 until booksPerRow) {
                val book = books.getOrNull(slot)
                if (book != null) {
                    YomuPhysicalMangaVolume(
                        coverData = book.coverData,
                        title = book.title,
                        unreadCount = book.unreadCount,
                        downloadCount = book.downloadCount,
                        readProgress = book.readProgress,
                        hasBookmarks = book.hasBookmarks,
                        isSelected = false,
                        showTitle = style.showShelfTitles,
                        showProgress = style.showShelfProgress,
                        onClick = { onClickManga(book.mangaId) },
                        onLongClick = { onClickManga(book.mangaId) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    YomuSurfaceCard(
        modifier = modifier,
        onClick = onClick,
        shape = YomuShapes.card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(22.dp),
                )
                if (!badge.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun YomuHomeEmptySanctuary(
    onClickBrowse: () -> Unit,
    onClickRestoreBackup: () -> Unit,
    onClickLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_yomu),
            contentDescription = null,
            modifier = Modifier.size(84.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(MR.strings.yomu_home_empty_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = YomuSerifFontFamily,
                fontWeight = FontWeight.SemiBold,
            ),
            color = style.shelfPalette.goldAccent,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(MR.strings.yomu_home_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        YomuPrimaryButton(
            text = stringResource(MR.strings.yomu_home_action_browse),
            icon = MaterialSymbols.Rounded.Explore,
            onClick = onClickBrowse,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onClickRestoreBackup,
                shape = YomuShapes.button,
                border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(MR.strings.pref_restore_backup),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = onClickLibrary,
                shape = YomuShapes.button,
                border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(MR.strings.label_library),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeCustomizeBottomSheet(
    activeOrder: List<YomuHomeSection>,
    onDismiss: () -> Unit,
    onSave: (List<YomuHomeSection>) -> Unit,
) {
    var orderedAll by remember(activeOrder) {
        val missing = YomuHomeSection.entries.filterNot { it in activeOrder }
        mutableStateOf(activeOrder + missing)
    }
    var enabledSet by remember(activeOrder) {
        mutableStateOf(activeOrder.toSet())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(MR.strings.yomu_home_customize),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            orderedAll.forEachIndexed { idx, section ->
                val isChecked = section in enabledSet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(YomuShapes.chapterRow)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            enabledSet = if (checked) enabledSet + section else enabledSet - section
                        },
                    )
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        enabled = idx > 0,
                        onClick = {
                            val list = orderedAll.toMutableList()
                            val prev = list[idx - 1]
                            list[idx - 1] = section
                            list[idx] = prev
                            orderedAll = list
                        },
                    ) {
                        Icon(MaterialSymbols.Rounded.ArrowUpward, contentDescription = null)
                    }
                    IconButton(
                        enabled = idx < orderedAll.lastIndex,
                        onClick = {
                            val list = orderedAll.toMutableList()
                            val next = list[idx + 1]
                            list[idx + 1] = section
                            list[idx] = next
                            orderedAll = list
                        },
                    ) {
                        Icon(MaterialSymbols.Rounded.ArrowDownward, contentDescription = null)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            YomuPrimaryButton(
                text = stringResource(MR.strings.action_save),
                onClick = { onSave(orderedAll.filter { it in enabledSet }) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
