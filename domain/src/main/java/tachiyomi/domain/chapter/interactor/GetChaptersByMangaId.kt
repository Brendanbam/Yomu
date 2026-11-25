package tachiyomi.domain.chapter.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository

@Inject
class GetChaptersByMangaId(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun awaitStrict(mangaId: Long, applyScanlatorFilter: Boolean = false): List<Chapter> =
        chapterRepository.getChapterByMangaId(mangaId, applyScanlatorFilter)

    suspend fun await(mangaId: Long, applyScanlatorFilter: Boolean = false): List<Chapter> {
        return try {
            awaitStrict(mangaId, applyScanlatorFilter)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }
}
