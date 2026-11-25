package eu.kanade.domain.yomu

import androidx.compose.runtime.Immutable
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.MangaCover

/**
 * Pure, truthful representation of a Continue Reading candidate.
 *
 * Explicitly separates:
 * - Current chapter reading position ([lastPageRead], 1-based page number [currentPageNumber])
 * - Optional chapter completion percentage (null when total page count is unknown; never fabricated)
 * - Series-level progress ([readChaptersCount] / [totalChaptersCount], [seriesProgressFraction])
 * - Unread chapter count ([unreadChaptersCount])
 * - Exact target chapter ([targetChapterId]) matching Mihon's [GetNextChapters] resume semantics
 * - Source & offline availability ([isSourceAvailable], [isTargetChapterDownloaded])
 */
@Immutable
data class YomuContinueReadingState(
    val mangaId: Long,
    val targetChapterId: Long,
    val title: String,
    val targetChapterLabel: String,
    val currentPageNumber: Int?,
    val chapterCompletionPercent: Int?,
    val readChaptersCount: Long,
    val totalChaptersCount: Long,
    val unreadChaptersCount: Long,
    val coverData: MangaCover,
    val isInLibrary: Boolean,
    val isPartiallyRead: Boolean,
    val isCaughtUp: Boolean,
    val isSourceAvailable: Boolean,
    val isTargetChapterDownloaded: Boolean,
) {
    /**
     * Series progress strictly derived from real read/total chapter counts.
     * Returns null when total chapters is unknown or zero, or when genuinely 0 chapters are read.
     */
    val seriesProgressFraction: Float?
        get() = if (totalChaptersCount > 0L && readChaptersCount > 0L) {
            (readChaptersCount.toFloat() / totalChaptersCount.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }

    val seriesCompletionPercent: Int?
        get() = seriesProgressFraction?.let { (it * 100f).toInt().coerceIn(0, 100) }
}

/**
 * Pure resolver aligning Home's Continue Reading card with Mihon's [GetNextChapters] semantics.
 */
object YomuContinueReadingResolver {

    data class CandidateInput(
        val mangaId: Long,
        val title: String,
        val coverData: MangaCover,
        val isInLibrary: Boolean,
        val isSourceAvailable: Boolean,
        val lastReadChapter: Chapter?,
        val nextResumableChapter: Chapter?,
        val readChaptersCount: Long,
        val totalChaptersCount: Long,
        val isNextChapterDownloaded: Boolean,
        val chapterTotalPages: Int? = null,
    )

    /**
     * Resolves the exact Continue Reading card state for a candidate.
     *
     * Rules:
     * 1. If [CandidateInput.nextResumableChapter] is non-null (derived via Mihon's `GetNextChapters`),
     *    that exact chapter is both displayed and launched when Resume Reading is pressed.
     * 2. If the target chapter is partially read (`!read && lastPageRead > 0`), reports the real
     *    1-based page position (`lastPageRead + 1`). Only computes [chapterCompletionPercent] if
     *    [CandidateInput.chapterTotalPages] is actually known (> 0); never invents a fallback %.
     * 3. If all available chapters are read (`nextResumableChapter == null`) and [lastReadChapter]
     *    still exists, reports an honest caught-up state referencing the last read chapter (returns
     *    null if even the last chapter was deleted/migrated away).
     */
    internal fun nextUnreadAfterHistory(chapters: List<Chapter>, historyChapterId: Long): Chapter? {
        val start = chapters.indexOfFirst { it.id == historyChapterId }.coerceAtLeast(0)
        return chapters.asSequence().drop(start).firstOrNull { !it.read }
            ?: chapters.firstOrNull { !it.read }
    }

    fun resolve(input: CandidateInput): YomuContinueReadingState? {
        val target = input.nextResumableChapter ?: input.lastReadChapter ?: return null
        val isCaughtUp = input.nextResumableChapter == null && target.read
        val isPartiallyRead = !target.read && target.lastPageRead > 0
        val currentPageNumber = if (isPartiallyRead) target.lastPageRead + 1 else null

        val chapterCompletionPercent = if (
            isPartiallyRead &&
            input.chapterTotalPages != null &&
            input.chapterTotalPages > 0
        ) {
            ((target.lastPageRead + 1).toFloat() / input.chapterTotalPages.toFloat() * 100f)
                .toInt()
                .coerceIn(1, 99)
        } else {
            null
        }

        val unreadCount = (input.totalChaptersCount - input.readChaptersCount).coerceAtLeast(0L)
        val label = formatChapterLabel(target)

        return YomuContinueReadingState(
            mangaId = input.mangaId,
            targetChapterId = target.id,
            title = input.title,
            targetChapterLabel = label,
            currentPageNumber = currentPageNumber,
            chapterCompletionPercent = chapterCompletionPercent,
            readChaptersCount = input.readChaptersCount,
            totalChaptersCount = input.totalChaptersCount,
            unreadChaptersCount = unreadCount,
            coverData = input.coverData,
            isInLibrary = input.isInLibrary,
            isPartiallyRead = isPartiallyRead,
            isCaughtUp = isCaughtUp,
            isSourceAvailable = input.isSourceAvailable,
            isTargetChapterDownloaded = input.isNextChapterDownloaded,
        )
    }

    /**
     * Picks the best Continue Reading state across recent history entries:
     * prefers the most recent entry that still has an unread/resumable chapter;
     * otherwise falls back to the most recent valid caught-up entry.
     */
    fun selectBest(candidates: List<CandidateInput>): YomuContinueReadingState? {
        var firstCaughtUp: YomuContinueReadingState? = null
        var firstUnavailable: YomuContinueReadingState? = null
        for (candidate in candidates) {
            val resolved = resolve(candidate) ?: continue
            if (!resolved.isCaughtUp) {
                if (resolved.isSourceAvailable || resolved.isTargetChapterDownloaded) return resolved
                if (firstUnavailable == null) firstUnavailable = resolved
            } else if (firstCaughtUp == null) {
                firstCaughtUp = resolved
            }
        }
        return firstUnavailable ?: firstCaughtUp
    }

    internal fun formatChapterLabel(chapter: Chapter): String {
        val trimmed = chapter.name.trim()
        if (trimmed.isNotEmpty()) return trimmed
        return if (chapter.isRecognizedNumber) {
            val num = chapter.chapterNumber
            val formatted = if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
            "Ch. $formatted"
        } else {
            "Chapter"
        }
    }
}
