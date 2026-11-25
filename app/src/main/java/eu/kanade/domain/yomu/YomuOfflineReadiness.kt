package eu.kanade.domain.yomu

import eu.kanade.tachiyomi.data.download.DownloadCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.util.chapter.getNextUnread
import kotlinx.coroutines.CancellationException
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.source.local.isLocal

/**
 * Uses the same next-unread selection as the library's reading shortcut, without
 * enabling the downloaded-only filter (which could skip an undownloaded chapter).
 * Aggregate download counts are only a cheap prefilter, never proof of readiness.
 */
internal suspend fun LibraryManga.isNextUnreadChapterDownloaded(
    downloadCount: Int,
    getChaptersByMangaId: GetChaptersByMangaId,
    downloadManager: DownloadManager,
    downloadCache: DownloadCache,
): Boolean {
    if (unreadCount <= 0L) return false
    if (manga.isLocal()) return true
    if (downloadCount <= 0) return false

    return try {
        val nextChapter = getChaptersByMangaId.await(manga.id, applyScanlatorFilter = true)
            .getNextUnread(manga, downloadedOnly = false, downloadManager = downloadManager)
            ?: return false
        downloadCache.isChapterDownloaded(
            chapterName = nextChapter.name,
            chapterScanlator = nextChapter.scanlator,
            chapterUrl = nextChapter.url,
            mangaTitle = manga.title,
            sourceId = manga.source,
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        logcat(LogPriority.ERROR, error) { "Could not verify Yomu offline readiness" }
        false
    }
}
