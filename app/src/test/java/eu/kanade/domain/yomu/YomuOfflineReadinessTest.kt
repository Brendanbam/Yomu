package eu.kanade.domain.yomu

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter

class YomuOfflineReadinessTest {
    private val chapters = (1L..20L).map {
        Chapter.create().copy(id = it, read = it <= 10L)
    }
    private val next = chapters.first { !it.read }

    @Test
    fun `read downloads are not evidence of offline readiness`() {
        classifyOfflineChapters(chapters, next, (1L..10L).toSet()) shouldBe YomuOfflineAvailability()
    }

    @Test
    fun `next unread and complete archive are distinct states`() {
        classifyOfflineChapters(chapters, next, setOf(11L)) shouldBe YomuOfflineAvailability(true, false)
        classifyOfflineChapters(chapters, next, (1L..20L).toSet()) shouldBe YomuOfflineAvailability(true, true)
        classifyOfflineChapters(chapters, null, (1L..20L).toSet()) shouldBe YomuOfflineAvailability(false, true)
    }

    @Test
    fun `empty or unrelated cached chapters cannot qualify as an archive`() {
        classifyOfflineChapters(emptyList(), null, emptySet()) shouldBe YomuOfflineAvailability()
        classifyOfflineChapters(chapters, next, setOf(999L)) shouldBe YomuOfflineAvailability()
    }
}
