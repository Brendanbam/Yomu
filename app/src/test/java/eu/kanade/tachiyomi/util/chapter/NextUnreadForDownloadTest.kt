package eu.kanade.tachiyomi.util.chapter

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter

class NextUnreadForDownloadTest {
    private fun chapter(id: Long, number: Double, scanlator: String = "A", read: Boolean = false) =
        Chapter.create().copy(id = id, chapterNumber = number, scanlator = scanlator, read = read)

    @Test
    fun `starts after the open chapter skips read chapters and limits the queue`() {
        val current = chapter(2, 2.0)
        val chapters = listOf(chapter(1, 1.0), current, chapter(3, 3.0, read = true), chapter(4, 4.0), chapter(5, 5.0))
        chapters.nextUnreadForDownload(current, 1, skipDuplicates = false).map { it.id } shouldBe listOf(4L)
    }

    @Test
    fun `keeps the active release and prefers the same scanlator for later chapters`() {
        val current = chapter(2, 1.0)
        val chapters = listOf(chapter(1, 1.0, "B"), current, chapter(3, 2.0, "B"), chapter(4, 2.0), chapter(5, 3.0))
        chapters.nextUnreadForDownload(current, 2, skipDuplicates = true).map { it.id } shouldBe listOf(4L, 5L)
    }

    @Test
    fun `leaves the queue alone when the active chapter is absent`() {
        val current = chapter(2, 2.0)
        listOf(chapter(1, 1.0)).nextUnreadForDownload(current, 10, skipDuplicates = true) shouldBe emptyList()
    }
}
