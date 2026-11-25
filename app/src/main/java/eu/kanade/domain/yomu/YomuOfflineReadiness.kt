package eu.kanade.domain.yomu

import eu.kanade.tachiyomi.data.download.DownloadCache
import kotlinx.coroutines.CancellationException
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.service.getChapterSort
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.source.local.isLocal

internal data class YomuOfflineAvailability(
    val nextUnreadDownloaded: Boolean = false,
    val allChaptersDownloaded: Boolean = false,
)

/**
 * Verifies chapter availability using the canonical reading order and download cache.
 * Aggregate download counts are only a cheap prefilter, never proof of readiness.
 */
internal suspend fun LibraryManga.getOfflineAvailability(
    downloadCount: Int,
    getChaptersByMangaId: GetChaptersByMangaId,
    downloadCache: DownloadCache,
    checkFullArchive: Boolean = false,
): YomuOfflineAvailability {
    if (manga.isLocal()) {
        return YomuOfflineAvailability(unreadCount > 0L, totalChapters > 0L)
    }
    if (downloadCount <= 0 || totalChapters <= 0L) return YomuOfflineAvailability()

    fun isDownloaded(chapter: Chapter): Boolean = downloadCache.isChapterDownloaded(
        chapterName = chapter.name,
        chapterScanlator = chapter.scanlator,
        chapterUrl = chapter.url,
        mangaTitle = manga.title,
        sourceId = manga.source,
    )

    return try {
        val nextChapter = if (unreadCount > 0L) {
            getChaptersByMangaId.await(manga.id, applyScanlatorFilter = true)
                .asSequence()
                .filterNot { it.read }
                .minWithOrNull(getChapterSort(manga, sortDescending = false))
        } else {
            null
        }
        val nextDownloaded = nextChapter?.let(::isDownloaded) == true
        val fullArchive = if (checkFullArchive && downloadCount >= totalChapters) {
            val chapters = getChaptersByMangaId.await(manga.id, applyScanlatorFilter = false)
            chapters.isNotEmpty() && chapters.all(::isDownloaded)
        } else {
            false
        }
        YomuOfflineAvailability(nextDownloaded, fullArchive)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        logcat(LogPriority.ERROR, error) { "Could not verify Yomu offline readiness" }
        YomuOfflineAvailability()
    }
}
