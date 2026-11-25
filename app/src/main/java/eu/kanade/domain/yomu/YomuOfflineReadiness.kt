package eu.kanade.domain.yomu

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.download.DownloadCache
import kotlinx.coroutines.CancellationException
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.chapter.service.getChapterSort
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.local.isLocal

data class YomuOfflineAvailability(
    val nextUnreadDownloaded: Boolean = false,
    val allChaptersDownloaded: Boolean = false,
)

internal fun classifyOfflineChapters(
    chapters: List<Chapter>,
    nextUnread: Chapter?,
    downloadedIds: Set<Long>,
): YomuOfflineAvailability = YomuOfflineAvailability(
    nextUnreadDownloaded = nextUnread != null && nextUnread.id in downloadedIds,
    allChaptersDownloaded = chapters.isNotEmpty() && chapters.all { it.id in downloadedIds },
)

/**
 * Verifies real chapter identities in bounded database batches, rather than running
 * a query for every book or using aggregate download counts as proof of readiness.
 */
@Inject
class GetYomuOfflineAvailability(
    private val chapterRepository: ChapterRepository,
    private val mangaRepository: MangaRepository,
    private val downloadCache: DownloadCache,
) {
    suspend fun await(
        library: List<LibraryManga>,
        downloadCounts: Map<Long, Int>,
        checkFullArchive: Boolean = false,
    ): Map<Long, YomuOfflineAvailability> {
        val result = mutableMapOf<Long, YomuOfflineAvailability>()
        val candidates = library.distinctBy { it.id }.filter { entry ->
            if (entry.manga.isLocal()) {
                result[entry.id] = YomuOfflineAvailability(
                    nextUnreadDownloaded = entry.unreadCount > 0L,
                    allChaptersDownloaded = entry.totalChapters > 0L,
                )
                false
            } else {
                val downloads = downloadCounts[entry.id] ?: 0
                downloads > 0 && entry.totalChapters > 0L &&
                    (entry.unreadCount > 0L || (checkFullArchive && downloads >= entry.totalChapters))
            }
        }

        for (batch in candidates.chunked(200)) {
            try {
                val ids = batch.map { it.id }
                val chaptersByManga = chapterRepository.getChaptersByMangaIds(ids)
                val exclusions = mangaRepository.getExcludedScanlatorsByMangaIds(ids)
                for (entry in batch) {
                    val chapters = chaptersByManga[entry.id].orEmpty()
                    val excluded = exclusions[entry.id].orEmpty().toSet()
                    val nextUnread = chapters.asSequence()
                        .filterNot { it.read || it.scanlator?.let { name -> name in excluded } == true }
                        .minWithOrNull(getChapterSort(entry.manga, sortDescending = false))
                    val checkAll = checkFullArchive && (downloadCounts[entry.id] ?: 0) >= chapters.size
                    val toCheck = if (checkAll) chapters else listOfNotNull(nextUnread)
                    val downloadedIds = downloadCache.getDownloadedChapterIds(
                        toCheck,
                        entry.manga.title,
                        entry.manga.source,
                    )
                    val availability = classifyOfflineChapters(chapters, nextUnread, downloadedIds)
                    result[entry.id] = availability.copy(
                        allChaptersDownloaded = checkAll && availability.allChaptersDownloaded,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logcat(LogPriority.ERROR, error) { "Could not verify Yomu offline availability batch" }
            }
        }
        return result
    }
}
