package eu.kanade.domain.yomu

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.MangaCover

class YomuContinueReadingResolverTest {

    private fun cover(mangaId: Long = 1L) = MangaCover(
        mangaId = mangaId,
        sourceId = 100L,
        isMangaFavorite = true,
        url = null,
        lastModified = null,
    )

    private fun chapter(
        id: Long,
        name: String = "Chapter $id",
        number: Double = id.toDouble(),
        read: Boolean = false,
        lastPageRead: Int = 0,
    ) = Chapter.create().copy(
        id = id,
        mangaId = 1L,
        name = name,
        chapterNumber = number,
        read = read,
        lastPageRead = lastPageRead,
    )

    @Test
    fun `partially read chapter reports exact page position and never fabricates percentage`() {
        val ch = chapter(id = 10L, name = "Ch. 10", read = false, lastPageRead = 14)
        val resolved = YomuContinueReadingResolver.resolve(
            YomuContinueReadingResolver.CandidateInput(
                mangaId = 1L,
                title = "Vagabond",
                coverData = cover(),
                isInLibrary = true,
                isSourceAvailable = true,
                lastReadChapter = ch,
                nextResumableChapter = ch,
                readChaptersCount = 0L,
                totalChaptersCount = 20L,
                isNextChapterDownloaded = true,
                chapterTotalPages = null,
            ),
        )

        resolved.shouldNotBeNull()
        resolved.targetChapterId shouldBe 10L
        resolved.targetChapterLabel shouldBe "Ch. 10"
        resolved.isPartiallyRead shouldBe true
        resolved.currentPageNumber shouldBe 15
        // Never invent a fallback percentage when total page count is unknown
        resolved.chapterCompletionPercent.shouldBeNull()
        // Never fabricate a filled progress bar for zero completed chapters
        resolved.seriesProgressFraction.shouldBeNull()
        resolved.seriesCompletionPercent.shouldBeNull()
        resolved.unreadChaptersCount shouldBe 20L
        resolved.isTargetChapterDownloaded shouldBe true
        resolved.isCaughtUp shouldBe false
    }

    @Test
    fun `partially read chapter computes chapter percent only when total pages are known`() {
        val ch = chapter(id = 10L, read = false, lastPageRead = 9)
        val resolved = YomuContinueReadingResolver.resolve(
            YomuContinueReadingResolver.CandidateInput(
                mangaId = 1L,
                title = "Monster",
                coverData = cover(),
                isInLibrary = true,
                isSourceAvailable = true,
                lastReadChapter = ch,
                nextResumableChapter = ch,
                readChaptersCount = 5L,
                totalChaptersCount = 20L,
                isNextChapterDownloaded = false,
                chapterTotalPages = 20,
            ),
        )

        resolved.shouldNotBeNull()
        resolved.currentPageNumber shouldBe 10
        resolved.chapterCompletionPercent shouldBe 50
        resolved.seriesProgressFraction shouldBe 0.25f
        resolved.seriesCompletionPercent shouldBe 25
    }

    @Test
    fun `completed chapter advances target to next unread chapter`() {
        val lastRead = chapter(id = 5L, name = "Chapter 5", read = true, lastPageRead = 24)
        val nextUnread = chapter(id = 7L, name = "Chapter 7", read = false, lastPageRead = 0)
        val resolved = YomuContinueReadingResolver.resolve(
            YomuContinueReadingResolver.CandidateInput(
                mangaId = 1L,
                title = "Berserk",
                coverData = cover(),
                isInLibrary = true,
                isSourceAvailable = true,
                lastReadChapter = lastRead,
                nextResumableChapter = nextUnread,
                readChaptersCount = 6L,
                totalChaptersCount = 10L,
                isNextChapterDownloaded = true,
            ),
        )

        resolved.shouldNotBeNull()
        resolved.targetChapterId shouldBe 7L
        resolved.targetChapterLabel shouldBe "Chapter 7"
        resolved.isPartiallyRead shouldBe false
        resolved.currentPageNumber.shouldBeNull()
        resolved.isCaughtUp shouldBe false
        resolved.unreadChaptersCount shouldBe 4L
        resolved.seriesCompletionPercent shouldBe 60
    }

    @Test
    fun `all chapters read resolves honestly as caught up`() {
        val last = chapter(id = 10L, name = "Chapter 10", read = true, lastPageRead = 30)
        val resolved = YomuContinueReadingResolver.resolve(
            YomuContinueReadingResolver.CandidateInput(
                mangaId = 1L,
                title = "Pluto",
                coverData = cover(),
                isInLibrary = true,
                isSourceAvailable = true,
                lastReadChapter = last,
                nextResumableChapter = null,
                readChaptersCount = 10L,
                totalChaptersCount = 10L,
                isNextChapterDownloaded = false,
            ),
        )

        resolved.shouldNotBeNull()
        resolved.isCaughtUp shouldBe true
        resolved.targetChapterId shouldBe 10L
        resolved.unreadChaptersCount shouldBe 0L
        resolved.seriesProgressFraction shouldBe 1f
    }

    @Test
    fun `deleted or migrated chapter with no remaining chapters returns null`() {
        val resolved = YomuContinueReadingResolver.resolve(
            YomuContinueReadingResolver.CandidateInput(
                mangaId = 1L,
                title = "Deleted Series",
                coverData = cover(),
                isInLibrary = false,
                isSourceAvailable = false,
                lastReadChapter = null,
                nextResumableChapter = null,
                readChaptersCount = 0L,
                totalChaptersCount = 0L,
                isNextChapterDownloaded = false,
            ),
        )
        resolved.shouldBeNull()
    }

    @Test
    fun `selectBest prefers resumable series over caught up and deleted entries`() {
        val deleted = YomuContinueReadingResolver.CandidateInput(
            mangaId = 1L,
            title = "Deleted",
            coverData = cover(1L),
            isInLibrary = false,
            isSourceAvailable = false,
            lastReadChapter = null,
            nextResumableChapter = null,
            readChaptersCount = 0L,
            totalChaptersCount = 0L,
            isNextChapterDownloaded = false,
        )
        val caughtUp = YomuContinueReadingResolver.CandidateInput(
            mangaId = 2L,
            title = "Caught Up Series",
            coverData = cover(2L),
            isInLibrary = true,
            isSourceAvailable = true,
            lastReadChapter = chapter(20L, read = true),
            nextResumableChapter = null,
            readChaptersCount = 20L,
            totalChaptersCount = 20L,
            isNextChapterDownloaded = false,
        )
        val resumable = YomuContinueReadingResolver.CandidateInput(
            mangaId = 3L,
            title = "Active Series",
            coverData = cover(3L),
            isInLibrary = false,
            isSourceAvailable = false,
            lastReadChapter = chapter(30L, read = false, lastPageRead = 4),
            nextResumableChapter = chapter(30L, read = false, lastPageRead = 4),
            readChaptersCount = 3L,
            totalChaptersCount = 12L,
            isNextChapterDownloaded = true,
        )

        val selected = YomuContinueReadingResolver.selectBest(listOf(deleted, caughtUp, resumable))
        selected.shouldNotBeNull()
        selected.mangaId shouldBe 3L
        selected.isInLibrary shouldBe false
        selected.isSourceAvailable shouldBe false
        selected.isTargetChapterDownloaded shouldBe true
        selected.currentPageNumber shouldBe 5
    }

    @Test
    fun `selectBest falls back to caught up entry when no resumable series exist`() {
        val caughtUp = YomuContinueReadingResolver.CandidateInput(
            mangaId = 2L,
            title = "Caught Up Series",
            coverData = cover(2L),
            isInLibrary = true,
            isSourceAvailable = true,
            lastReadChapter = chapter(20L, read = true),
            nextResumableChapter = null,
            readChaptersCount = 20L,
            totalChaptersCount = 20L,
            isNextChapterDownloaded = false,
        )
        val selected = YomuContinueReadingResolver.selectBest(listOf(caughtUp))
        selected.shouldNotBeNull()
        selected.mangaId shouldBe 2L
        selected.isCaughtUp shouldBe true
    }

    @Test
    fun `selectBest skips unavailable sources when another title can be read`() {
        val unavailable = YomuContinueReadingResolver.CandidateInput(
            mangaId = 1L,
            title = "Unavailable",
            coverData = cover(),
            isInLibrary = true,
            isSourceAvailable = false,
            lastReadChapter = chapter(5L, read = false),
            nextResumableChapter = chapter(5L, read = false),
            readChaptersCount = 4L,
            totalChaptersCount = 10L,
            isNextChapterDownloaded = false,
        )
        val offline = unavailable.copy(
            mangaId = 2L,
            title = "Offline",
            coverData = cover(2L),
            isNextChapterDownloaded = true,
        )

        YomuContinueReadingResolver.selectBest(listOf(unavailable, offline))?.mangaId shouldBe 2L
        YomuContinueReadingResolver.selectBest(listOf(unavailable))?.mangaId shouldBe 1L
    }
}
