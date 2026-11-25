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
import eu.kanade.domain.yomu.GetYomuOfflineAvailability
import eu.kanade.domain.yomu.YomuContinueReadingResolver
import eu.kanade.domain.yomu.YomuContinueReadingState
import eu.kanade.tachiyomi.data.download.DownloadCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.CancellationException
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
import tachiyomi.domain.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.chapter.service.getChapterSort
import tachiyomi.domain.history.interactor.GetHistory
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
    private val getChaptersByMangaId: GetChaptersByMangaId,
    private val getYomuOfflineAvailability: GetYomuOfflineAvailability,
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
        getLibraryManga.subscribeStrict().asSectionLoadState()
    }

    private val historyFlow: Flow<SectionLoadState<List<HistoryWithRelations>>> = retryTrigger.flatMapLatest {
        getHistory.subscribe("").asSectionLoadState()
    }

    private val updatesFlow: Flow<SectionLoadState<List<UpdatesWithRelations>>> = retryTrigger.flatMapLatest {
        getUpdates.subscribe(
            instant = Clock.System.now().minus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault()),
            unread = true,
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

    private val offlineSnapshotFlow = combine(libraryFlow, downloadSignalFlow) { libraryState, _ ->
        val library = libraryState.dataOrEmpty()
        val counts = downloadCache.getDownloadCounts(library.map { it.manga })
        val availability = getYomuOfflineAvailability.await(library, counts)
        Triple(libraryState, counts, availability)
    }

    val state: StateFlow<State> = combine(
        combine(offlineSnapshotFlow, historyFlow, updatesFlow, ::Triple),
        combine(sourcesFlow, activeDownloadCountFlow, ::Pair),
        uiPreferences.yomuHomeSectionsOrder.changes(),
    ) { (snapshot, historyState, updatesState), (sourcesState, activeDownloadsCount), sectionsOrderRaw ->
        val (libraryState, downloadCountsByMangaId, offlineAvailability) = snapshot
        val isInitialLoading = libraryState is SectionLoadState.Loading ||
            historyState is SectionLoadState.Loading

        val libraryList = libraryState.dataOrEmpty()
        val historyList = historyState.dataOrEmpty()
        val updatesList = updatesState.dataOrEmpty()
        val sources = sourcesState.dataOrEmpty()

        val libraryById = libraryList.associateBy { it.id }

        val now = Clock.System.now()
        val rediscoverCutoff = now - 14.days

        // Resolve Continue Reading across recent history candidates using Mihon's GetNextChapters semantics
        var continueLookupError: Exception? = null
        val continueCandidates = historyList.distinctBy { it.mangaId }.take(8).mapNotNull { history ->
            try {
                buildContinueCandidate(history = history, libraryEntry = libraryById[history.mangaId])
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logcat(LogPriority.ERROR, error) { "Failed to resolve Continue Reading" }
                continueLookupError = error
                null
            }
        }
        val continueReading = YomuContinueReadingResolver.selectBest(continueCandidates)

        val featuredLib = libraryList.maxByOrNull {
            it.lastRead?.toEpochMilliseconds() ?: it.manga.favoriteAt?.toEpochMilliseconds() ?: 0L
        }

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

        // The new-arrivals shelf represents unread releases, not previously read chapters.
        val recentlyUpdated = updatesList
            .filterNot { it.read }
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

        val inProgress = libraryList.filter { (it.hasStarted || it.lastRead != null) && it.unreadCount > 0L }
        val currentlyReading = inProgress
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

        val offlineItems = libraryList.distinctBy { it.id }
            .filter { offlineAvailability[it.id]?.nextUnreadDownloaded == true }
            .map { toShelfBook(it, downloadCountsByMangaId) }

        val pinnedSources = sources
            .filter { Pin.Actual in it.pin || it.isUsedLast }
            .distinctBy { it.id }
            .take(6)

        val readingStats = ReadingStats(
            libraryCount = libraryList.size,
            currentlyReadingCount = inProgress.size,
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
            historyError = (historyState as? SectionLoadState.Error)?.message ?: continueLookupError?.message,
            updatesError = (updatesState as? SectionLoadState.Error)?.message,
            sourcesError = (sourcesState as? SectionLoadState.Error)?.message,
            hasLibraryError = libraryState is SectionLoadState.Error,
            hasHistoryError = historyState is SectionLoadState.Error ||
                (continueLookupError != null && continueReading == null),
            hasUpdatesError = updatesState is SectionLoadState.Error,
            hasSourcesError = sourcesState is SectionLoadState.Error,
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
        val manga = libraryEntry?.manga ?: getManga.awaitStrict(history.mangaId)
        // One strict, scanlator-filtered snapshot: lookup errors must not become caught-up cards.
        val chapters = getChaptersByMangaId.awaitStrict(history.mangaId, applyScanlatorFilter = true)
            .sortedWith(getChapterSort(manga, sortDescending = false))
        val lastReadChapter = chapters.firstOrNull { it.id == history.chapterId }
        val nextResumable = YomuContinueReadingResolver.nextUnreadAfterHistory(chapters, history.chapterId)
        val totalChaptersCount = chapters.size.toLong()
        val readChaptersCount = chapters.count { it.read }.toLong()

        val targetChapter = nextResumable ?: lastReadChapter
        val isLocal = manga.isLocal()
        val isSourceAvailable = if (isLocal) {
            true
        } else {
            sourceManager.get(manga.source) != null
        }

        val isTargetDownloaded = when {
            targetChapter == null -> false
            isLocal -> true
            else -> downloadCache.isChapterDownloaded(
                chapterName = targetChapter.name,
                chapterScanlator = targetChapter.scanlator,
                chapterUrl = targetChapter.url,
                mangaTitle = manga.title,
                sourceId = manga.source,
            )
        }

        return YomuContinueReadingResolver.CandidateInput(
            mangaId = history.mangaId,
            title = manga.title,
            coverData = history.coverData,
            isInLibrary = manga.favorite,
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

    fun pickRandomManga(): Long? {
        val home = state.value
        val candidates = home.rediscover.ifEmpty { home.currentlyReading }.ifEmpty { home.offlineCollection }
        val picked = candidates.filterNot { it.mangaId == previousRandomManga }.ifEmpty { candidates }.randomOrNull()
        val id = picked?.mangaId ?: home.heroMangaId
        previousRandomManga = id
        return id
    }

    private var previousRandomManga: Long? = null

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
        val hasSourcesError: Boolean = false,
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
                !hasUpdatesError &&
                !hasSourcesError &&
                !areAllSectionsHidden &&
                continueReading == null &&
                recentlyUpdated.isEmpty() &&
                currentlyReading.isEmpty() &&
                rediscover.isEmpty() &&
                offlineCollection.isEmpty() &&
                libraryCount == 0
    }
}
