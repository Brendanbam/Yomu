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
import eu.kanade.domain.yomu.YomuContinueReadingResolver
import eu.kanade.domain.yomu.YomuContinueReadingState
import eu.kanade.tachiyomi.data.download.DownloadCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.history.interactor.GetNextChapters
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.domain.updates.interactor.GetUpdates
import tachiyomi.domain.updates.model.UpdatesWithRelations
import tachiyomi.source.local.isLocal
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class YomuHomeViewModel(
    private val getHistory: GetHistory,
    private val getLibraryManga: GetLibraryManga,
    private val getManga: GetManga,
    private val getUpdates: GetUpdates,
    private val getChapter: GetChapter,
    private val getChaptersByMangaId: GetChaptersByMangaId,
    private val getNextChapters: GetNextChapters,
    private val getEnabledSources: GetEnabledSources,
    private val sourceManager: SourceManager,
    private val downloadCache: DownloadCache,
    private val downloadManager: DownloadManager,
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    private fun <T> Flow<T>.asSectionLoadState(): Flow<SectionLoadState<T>> {
        return map<T, SectionLoadState<T>> { SectionLoadState.Success(it) }
            .onStart { emit(SectionLoadState.Loading) }
            .catch { error ->
                logcat(LogPriority.ERROR, error) { "Failed to load Home section stream" }
                emit(SectionLoadState.Error(error.message))
            }
    }

    private val libraryFlow: Flow<SectionLoadState<List<LibraryManga>>> = retryTrigger.flatMapLatest {
        getLibraryManga.subscribe().asSectionLoadState()
    }

    private val historyFlow: Flow<SectionLoadState<List<HistoryWithRelations>>> = retryTrigger.flatMapLatest {
        getHistory.subscribe("").asSectionLoadState()
    }

    private val updatesFlow: Flow<SectionLoadState<List<UpdatesWithRelations>>> = retryTrigger.flatMapLatest {
        getUpdates.subscribe(
            instant = Clock.System.now().minus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault()),
            unread = null,
            started = null,
            bookmarked = null,
            hideExcludedScanlators = false,
            includedCategories = emptyList(),
            excludedCategories = emptyList(),
        ).asSectionLoadState()
    }

    private val sourcesFlow: Flow<SectionLoadState<List<Source>>> = retryTrigger.flatMapLatest {
        getEnabledSources.subscribe().asSectionLoadState()
    }

    private val downloadSignalFlow = downloadCache.changes
        .debounce(300.milliseconds)
        .onStart { emit(Unit) }

    private val activeDownloadCountFlow = downloadManager.queueState
        .map { it.size }
        .distinctUntilChanged()

    val state: StateFlow<State> = combine(
        combine(libraryFlow, historyFlow, updatesFlow, ::Triple),
        combine(sourcesFlow, downloadSignalFlow, activeDownloadCountFlow, ::Triple),
        uiPreferences.yomuHomeSectionsOrder.changes(),
    ) { (libraryState, historyState, updatesState), (sourcesState, _, activeDownloadsCount), sectionsOrderRaw ->
        val isInitialLoading = libraryState is SectionLoadState.Loading &&
            historyState is SectionLoadState.Loading

        val libraryList = libraryState.dataOrEmpty()
        val historyList = historyState.dataOrEmpty()
        val updatesList = updatesState.dataOrEmpty()
        val sources = sourcesState.dataOrEmpty()

        val libraryById = libraryList.associateBy { it.id }
        val downloadCountsByMangaId = if (libraryList.isNotEmpty()) {
            downloadCache.getDownloadCounts(libraryList.map { it.manga })
        } else {
            emptyMap()
        }

        val now = Clock.System.now()
        val rediscoverCutoff = now - 14.days

        // Resolve Continue Reading across recent history candidates using Mihon's GetNextChapters semantics
        val continueCandidates = historyList.take(8).map { history ->
            buildContinueCandidate(history = history, libraryEntry = libraryById[history.mangaId])
        }
        val continueReading = YomuContinueReadingResolver.selectBest(continueCandidates)

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

        // Prioritize unread updates first while preserving chronological order within unread/read groups
        val recentlyUpdated = updatesList
            .sortedWith(compareBy<UpdatesWithRelations> { it.read })
            .distinctBy { it.mangaId }
            .take(12)
            .map { u ->
                val lib = libraryById[u.mangaId]
                val isChapterDownloaded = if (lib != null && lib.manga.isLocal()) {
                    true
                } else {
                    downloadCache.isChapterDownloaded(
                        chapterName = u.chapterName,
                        chapterScanlator = u.scanlator,
                        chapterUrl = u.chapterUrl,
                        mangaTitle = u.mangaTitle,
                        sourceId = u.sourceId,
                    )
                }
                RecentUpdateItem(
                    update = u,
                    isDownloaded = isChapterDownloaded,
                    unreadCount = lib?.unreadCount ?: if (!u.read) 1L else 0L,
                )
            }

        val currentlyReading = libraryList
            .filter { it.hasStarted && it.unreadCount > 0L }
            .sortedByDescending { it.lastRead?.toEpochMilliseconds() ?: 0L }
            .take(9)
            .map { toShelfBook(it, downloadCountsByMangaId) }

        val currentlyReadingIds = currentlyReading.mapTo(HashSet()) { it.mangaId }
        val rediscover = libraryList
            .filter { lib ->
                lib.id !in currentlyReadingIds &&
                    (lib.lastRead == null || lib.lastRead!! < rediscoverCutoff)
            }
            .sortedBy { it.lastRead?.toEpochMilliseconds() ?: Long.MIN_VALUE }
            .take(9)
            .map { toShelfBook(it, downloadCountsByMangaId) }

        val offlineItems = libraryList.mapNotNull { lib ->
            val dlCount = if (lib.manga.isLocal()) {
                lib.totalChapters.toInt()
            } else {
                downloadCountsByMangaId[lib.manga.id] ?: 0
            }
            if (dlCount > 0 || lib.manga.isLocal()) {
                toShelfBook(lib, downloadCountsByMangaId)
            } else {
                null
            }
        }

        val pinnedSources = sources
            .filter { Pin.Actual in it.pin || it.isUsedLast }
            .distinctBy { it.id }
            .take(6)

        val readingStats = ReadingStats(
            libraryCount = libraryList.size,
            currentlyReadingCount = libraryList.count { it.hasStarted && it.unreadCount > 0L },
            completedSeriesCount = libraryList.count {
                it.totalChapters > 0L && it.unreadCount == 0L &&
                    (
                        it.manga.status.toInt() == SManga.COMPLETED ||
                            it.manga.status.toInt() == SManga.PUBLISHING_FINISHED
                        )
            },
            totalChaptersRead = libraryList.sumOf { it.readCount },
            offlineReadyCount = offlineItems.size,
            totalDownloadedChapters = libraryList.sumOf { lib ->
                if (lib.manga.isLocal()) 0 else (downloadCountsByMangaId[lib.manga.id] ?: 0)
            },
        )

        State(
            isLoading = isInitialLoading,
            libraryError = (libraryState as? SectionLoadState.Error)?.message,
            historyError = (historyState as? SectionLoadState.Error)?.message,
            updatesError = (updatesState as? SectionLoadState.Error)?.message,
            sourcesError = (sourcesState as? SectionLoadState.Error)?.message,
            hasLibraryError = libraryState is SectionLoadState.Error,
            hasHistoryError = historyState is SectionLoadState.Error,
            hasUpdatesError = updatesState is SectionLoadState.Error,
            heroCover = heroCover,
            heroTitle = heroTitle,
            heroMangaId = heroMangaId,
            continueReading = continueReading,
            recentlyUpdated = recentlyUpdated,
            currentlyReading = currentlyReading,
            rediscover = rediscover,
            offlineCollection = offlineItems.take(12),
            pinnedSources = pinnedSources,
            activeDownloadsCount = activeDownloadsCount,
            stats = readingStats,
            sectionsOrder = YomuHomeSection.parseOrder(sectionsOrderRaw),
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = State(),
        )

    private suspend fun buildContinueCandidate(
        history: HistoryWithRelations,
        libraryEntry: LibraryManga?,
    ): YomuContinueReadingResolver.CandidateInput {
        val manga = libraryEntry?.manga ?: runCatching { getManga.await(history.mangaId) }.getOrNull()
        val lastReadChapter = runCatching { getChapter.await(history.chapterId) }.getOrNull()

        // Match Mihon's exact chapter resume order:
        // 1) Next unread chapter from the last read chapter onward (skipping any already-read chapters)
        // 2) If none remain after lastReadChapter, fall back to any remaining unread chapter in the series
        val nextAfterCurrent = runCatching {
            getNextChapters.await(
                mangaId = history.mangaId,
                fromChapterId = history.chapterId,
                onlyUnread = false,
            ).firstOrNull { !it.read }
        }.getOrNull()

        val nextResumable = nextAfterCurrent ?: runCatching {
            getNextChapters.await(mangaId = history.mangaId, onlyUnread = true).firstOrNull()
        }.getOrNull()

        val readChaptersCount: Long
        val totalChaptersCount: Long
        if (libraryEntry != null) {
            readChaptersCount = libraryEntry.readCount
            totalChaptersCount = libraryEntry.totalChapters
        } else {
            val allChapters = runCatching {
                getChaptersByMangaId.await(history.mangaId, applyScanlatorFilter = true)
            }.getOrDefault(emptyList())
            totalChaptersCount = allChapters.size.toLong()
            readChaptersCount = allChapters.count { it.read }.toLong()
        }

        val targetChapter = nextResumable ?: lastReadChapter
        val isLocal = manga?.isLocal() == true
        val isSourceAvailable = if (isLocal) {
            true
        } else {
            val sourceId = manga?.source ?: history.coverData.sourceId
            sourceManager.get(sourceId) != null
        }

        val isTargetDownloaded = when {
            targetChapter == null -> false
            isLocal -> true
            else -> downloadCache.isChapterDownloaded(
                chapterName = targetChapter.name,
                chapterScanlator = targetChapter.scanlator,
                chapterUrl = targetChapter.url,
                mangaTitle = manga?.title ?: history.title,
                sourceId = manga?.source ?: history.coverData.sourceId,
            )
        }

        return YomuContinueReadingResolver.CandidateInput(
            mangaId = history.mangaId,
            title = manga?.title ?: history.title,
            coverData = history.coverData,
            isInLibrary = libraryEntry != null || manga?.favorite == true,
            isSourceAvailable = isSourceAvailable,
            lastReadChapter = lastReadChapter,
            nextResumableChapter = nextResumable,
            readChaptersCount = readChaptersCount,
            totalChaptersCount = totalChaptersCount,
            isNextChapterDownloaded = isTargetDownloaded,
        )
    }

    private fun toShelfBook(
        lib: LibraryManga,
        downloadCountsByMangaId: Map<Long, Int>,
    ): HomeBookItem {
        val dl = if (lib.manga.isLocal()) {
            lib.totalChapters.toInt()
        } else {
            downloadCountsByMangaId[lib.manga.id] ?: 0
        }
        val progress = if (lib.totalChapters > 0L && lib.readCount > 0L) {
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

    fun retry() {
        retryTrigger.update { it + 1 }
    }

    fun updateSectionsOrder(sections: List<YomuHomeSection>) {
        uiPreferences.yomuHomeSectionsOrder.set(YomuHomeSection.serializeOrder(sections))
    }

    fun resetSectionsOrder() {
        uiPreferences.yomuHomeSectionsOrder.set(YomuHomeSection.defaultOrder)
    }

    private sealed interface SectionLoadState<out T> {
        data object Loading : SectionLoadState<Nothing>
        data class Success<T>(val data: T) : SectionLoadState<T>
        data class Error(val message: String?) : SectionLoadState<Nothing>
    }

    private fun <T> SectionLoadState<List<T>>.dataOrEmpty(): List<T> {
        return (this as? SectionLoadState.Success)?.data.orEmpty()
    }

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
    data class ReadingStats(
        val libraryCount: Int = 0,
        val currentlyReadingCount: Int = 0,
        val completedSeriesCount: Int = 0,
        val totalChaptersRead: Long = 0L,
        val offlineReadyCount: Int = 0,
        val totalDownloadedChapters: Int = 0,
    )

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val hasLibraryError: Boolean = false,
        val hasHistoryError: Boolean = false,
        val hasUpdatesError: Boolean = false,
        val libraryError: String? = null,
        val historyError: String? = null,
        val updatesError: String? = null,
        val sourcesError: String? = null,
        val heroCover: MangaCover? = null,
        val heroTitle: String? = null,
        val heroMangaId: Long? = null,
        val continueReading: YomuContinueReadingState? = null,
        val recentlyUpdated: List<RecentUpdateItem> = emptyList(),
        val currentlyReading: List<HomeBookItem> = emptyList(),
        val rediscover: List<HomeBookItem> = emptyList(),
        val offlineCollection: List<HomeBookItem> = emptyList(),
        val pinnedSources: List<Source> = emptyList(),
        val activeDownloadsCount: Int = 0,
        val stats: ReadingStats = ReadingStats(),
        val sectionsOrder: List<YomuHomeSection> = YomuHomeSection.entries,
    ) {
        val libraryCount: Int
            get() = stats.libraryCount

        val totalChaptersRead: Long
            get() = stats.totalChaptersRead

        val isFatalError: Boolean
            get() = !isLoading && hasLibraryError && hasHistoryError

        val areAllSectionsHidden: Boolean
            get() = !isLoading && !isFatalError && sectionsOrder.isEmpty()

        val isEmpty: Boolean
            get() = !isLoading &&
                !hasLibraryError &&
                !hasHistoryError &&
                !areAllSectionsHidden &&
                continueReading == null &&
                recentlyUpdated.isEmpty() &&
                currentlyReading.isEmpty() &&
                rediscover.isEmpty() &&
                offlineCollection.isEmpty() &&
                libraryCount == 0
    }
}
