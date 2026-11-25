package eu.kanade.tachiyomi.ui.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import eu.kanade.domain.source.interactor.GetEnabledSources
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.YomuHomeSection
import eu.kanade.tachiyomi.data.download.DownloadCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.history.interactor.GetNextChapters
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.domain.updates.interactor.GetUpdates
import tachiyomi.domain.updates.model.UpdatesWithRelations
import tachiyomi.source.local.isLocal
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class YomuHomeViewModel(
    private val getHistory: GetHistory,
    private val getLibraryManga: GetLibraryManga,
    private val getUpdates: GetUpdates,
    private val getChapter: GetChapter,
    private val getNextChapters: GetNextChapters,
    private val getEnabledSources: GetEnabledSources,
    private val downloadCache: DownloadCache,
    private val downloadManager: DownloadManager,
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    private val libraryFlow = getLibraryManga.subscribe()
        .catch {
            logcat(LogPriority.ERROR, it)
            emit(emptyList())
        }

    private val historyFlow = getHistory.subscribe("")
        .catch {
            logcat(LogPriority.ERROR, it)
            emit(emptyList())
        }

    private val updatesFlow = getUpdates.subscribe(
        instant = Clock.System.now().minus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault()),
        unread = null,
        started = null,
        bookmarked = null,
        hideExcludedScanlators = false,
        includedCategories = emptyList(),
        excludedCategories = emptyList(),
    ).catch {
        logcat(LogPriority.ERROR, it)
        emit(emptyList())
    }

    private val sourcesFlow = getEnabledSources.subscribe()
        .catch {
            logcat(LogPriority.ERROR, it)
            emit(emptyList())
        }

    val state: StateFlow<State> = combine(
        combine(libraryFlow, historyFlow, updatesFlow, ::Triple),
        combine(sourcesFlow, downloadCache.changes, downloadManager.queueState, ::Triple),
        uiPreferences.yomuHomeSectionsOrder.changes(),
    ) { (libraryList, historyList, updatesList), (sources, _, downloadQueue), sectionsOrderRaw ->
        val libraryById = libraryList.associateBy { it.id }
        val now = Clock.System.now()
        val rediscoverCutoff = now - 14.days

        // Continue Reading from real most-recent reading history entry
        val latestHistory = historyList.firstOrNull()
        val continueReading = latestHistory?.let { h ->
            val chapter = runCatching { getChapter.await(h.chapterId) }.getOrNull()
            val lib = libraryById[h.mangaId]
            val progressFraction = when {
                lib != null && lib.totalChapters > 0 ->
                    (lib.readCount.toFloat() / lib.totalChapters.toFloat()).coerceIn(0f, 1f)
                chapter != null && chapter.read -> 1f
                chapter != null && chapter.lastPageRead > 0 -> 0.5f
                else -> 0f
            }
            ContinueReadingItem(
                mangaId = h.mangaId,
                chapterId = h.chapterId,
                title = h.title,
                chapterLabel = chapter?.name?.takeIf { it.isNotBlank() }
                    ?: if (h.chapterNumber >= 0) "Ch. ${formatNumber(h.chapterNumber)}" else "Latest chapter",
                lastPageRead = chapter?.lastPageRead ?: 0,
                progressFraction = progressFraction,
                coverData = h.coverData,
                isChapterRead = chapter?.read == true,
            )
        }

        val featuredLib = libraryList
            .sortedByDescending {
                it.lastRead?.toEpochMilliseconds() ?: it.manga.favoriteAt?.toEpochMilliseconds() ?: 0L
            }
            .firstOrNull()

        val heroCover = continueReading?.coverData
            ?: featuredLib?.let { lib ->
                MangaCover(
                    mangaId = lib.manga.id,
                    sourceId = lib.manga.source,
                    isMangaFavorite = lib.manga.favorite,
                    url = lib.manga.thumbnailUrl,
                    lastModified = lib.manga.coverLastModified,
                )
            }

        val heroTitle = continueReading?.title ?: featuredLib?.manga?.title

        val heroMangaId = continueReading?.mangaId ?: featuredLib?.manga?.id

        // Deduplicated recently updated series (preserving latest chapter per series)
        val recentlyUpdated = updatesList
            .distinctBy { it.mangaId }
            .take(12)
            .map { u ->
                val isDownloaded = downloadCache.getDownloadCount(
                    libraryById[u.mangaId]?.manga ?: return@map RecentUpdateItem(
                        update = u,
                        isDownloaded = false,
                        unreadCount = libraryById[u.mangaId]?.unreadCount ?: if (!u.read) 1L else 0L,
                    ),
                ) > 0
                RecentUpdateItem(
                    update = u,
                    isDownloaded = isDownloaded,
                    unreadCount = libraryById[u.mangaId]?.unreadCount ?: if (!u.read) 1L else 0L,
                )
            }

        // Currently reading shelf (started with unread chapters, sorted by last read)
        val currentlyReading = libraryList
            .filter { it.hasStarted && it.unreadCount > 0 }
            .sortedByDescending { it.lastRead?.toEpochMilliseconds() ?: 0L }
            .take(9)
            .map { toShelfBook(it) }

        // Rediscover shelf (in library, not read recently or untouched)
        val currentlyReadingIds = currentlyReading.mapTo(HashSet()) { it.mangaId }
        val rediscover = libraryList
            .filter { lib ->
                lib.id !in currentlyReadingIds &&
                    (lib.lastRead == null || lib.lastRead!! < rediscoverCutoff)
            }
            .sortedBy { it.lastRead?.toEpochMilliseconds() ?: Long.MIN_VALUE }
            .take(9)
            .map { toShelfBook(it) }

        // Offline Collection (downloaded chapters or local manga)
        val offlineCollection = libraryList
            .mapNotNull { lib ->
                val dlCount = if (lib.manga.isLocal()) {
                    lib.totalChapters.toInt()
                } else {
                    downloadCache.getDownloadCount(
                        lib.manga,
                    )
                }
                if (dlCount > 0 || lib.manga.isLocal()) {
                    toShelfBook(lib, dlCount)
                } else {
                    null
                }
            }
            .take(12)

        val pinnedSources = sources
            .filter { Pin.Actual in it.pin || it.isUsedLast }
            .distinctBy { it.id }
            .take(6)

        State(
            isLoading = false,
            heroCover = heroCover,
            heroTitle = heroTitle,
            heroMangaId = heroMangaId,
            continueReading = continueReading,
            recentlyUpdated = recentlyUpdated,
            currentlyReading = currentlyReading,
            rediscover = rediscover,
            offlineCollection = offlineCollection,
            pinnedSources = pinnedSources,
            activeDownloadsCount = downloadQueue.size,
            libraryCount = libraryList.size,
            totalChaptersRead = libraryList.sumOf { it.readCount },
            sectionsOrder = YomuHomeSection.parseOrder(sectionsOrderRaw),
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = State(),
        )

    private fun toShelfBook(lib: LibraryManga, precomputedDownloads: Int? = null): HomeBookItem {
        val dl = precomputedDownloads
            ?: if (lib.manga.isLocal()) lib.totalChapters.toInt() else downloadCache.getDownloadCount(lib.manga)
        val progress = if (lib.totalChapters > 0) {
            (lib.readCount.toFloat() / lib.totalChapters.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }
        return HomeBookItem(
            mangaId = lib.manga.id,
            title = lib.manga.title,
            coverData = MangaCover(
                mangaId = lib.manga.id,
                sourceId = lib.manga.source,
                isMangaFavorite = lib.manga.favorite,
                url = lib.manga.thumbnailUrl,
                lastModified = lib.manga.coverLastModified,
            ),
            unreadCount = lib.unreadCount,
            downloadCount = dl,
            readProgress = progress,
            hasBookmarks = lib.hasBookmarks,
        )
    }

    suspend fun getResumeChapterId(mangaId: Long, fallbackChapterId: Long): Long {
        val next = getNextChapters.await(mangaId, fallbackChapterId, onlyUnread = false).firstOrNull()
        return next?.id ?: fallbackChapterId
    }

    fun updateSectionsOrder(sections: List<YomuHomeSection>) {
        val serialized = if (sections.isEmpty()) {
            YomuHomeSection.defaultOrder
        } else {
            sections.joinToString(",") { it.id }
        }
        uiPreferences.yomuHomeSectionsOrder.set(serialized)
    }

    private fun formatNumber(num: Double): String {
        return if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
    }

    @Immutable
    data class ContinueReadingItem(
        val mangaId: Long,
        val chapterId: Long,
        val title: String,
        val chapterLabel: String,
        val lastPageRead: Int,
        val progressFraction: Float,
        val coverData: MangaCover,
        val isChapterRead: Boolean,
    )

    @Immutable
    data class RecentUpdateItem(
        val update: UpdatesWithRelations,
        val isDownloaded: Boolean,
        val unreadCount: Long,
    )

    @Immutable
    data class HomeBookItem(
        val mangaId: Long,
        val title: String,
        val coverData: MangaCover,
        val unreadCount: Long,
        val downloadCount: Int,
        val readProgress: Float?,
        val hasBookmarks: Boolean,
    )

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val heroCover: MangaCover? = null,
        val heroTitle: String? = null,
        val heroMangaId: Long? = null,
        val continueReading: ContinueReadingItem? = null,
        val recentlyUpdated: List<RecentUpdateItem> = emptyList(),
        val currentlyReading: List<HomeBookItem> = emptyList(),
        val rediscover: List<HomeBookItem> = emptyList(),
        val offlineCollection: List<HomeBookItem> = emptyList(),
        val pinnedSources: List<Source> = emptyList(),
        val activeDownloadsCount: Int = 0,
        val libraryCount: Int = 0,
        val totalChaptersRead: Long = 0,
        val sectionsOrder: List<YomuHomeSection> = YomuHomeSection.entries,
    ) {
        val isEmpty: Boolean
            get() = !isLoading &&
                continueReading == null &&
                recentlyUpdated.isEmpty() &&
                currentlyReading.isEmpty() &&
                rediscover.isEmpty() &&
                offlineCollection.isEmpty() &&
                libraryCount == 0
    }
}
