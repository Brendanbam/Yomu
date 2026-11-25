package eu.kanade.tachiyomi.util.chapter

import tachiyomi.domain.chapter.model.Chapter

internal fun List<Chapter>.nextUnreadForDownload(current: Chapter, count: Int, skipDuplicates: Boolean): List<Chapter> {
    val ordered = if (skipDuplicates) removeDuplicates(current) else this
    return ordered.asSequence()
        .dropWhile { it.id != current.id }
        .drop(1)
        .filterNot { it.read }
        .take(count.coerceIn(0, 10))
        .toList()
}

/**
 * Returns a copy of the list with duplicate chapters removed
 */
fun List<Chapter>.removeDuplicates(currentChapter: Chapter): List<Chapter> {
    return groupBy { it.chapterNumber }
        .map { (_, chapters) ->
            chapters.find { it.id == currentChapter.id }
                ?: chapters.find { it.scanlator == currentChapter.scanlator }
                ?: chapters.first()
        }
}
