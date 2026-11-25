package eu.kanade.domain.yomu

import eu.kanade.tachiyomi.source.model.SManga
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class YomuSmartShelfClassifierTest {

    private val fixedNow = Instant.fromEpochMilliseconds(1_760_000_000_000L)

    private fun entry(
        id: Long = 1L,
        totalChapters: Long = 10L,
        readCount: Long = 0L,
        downloadCount: Int = 0,
        isLocal: Boolean = false,
        publicationStatus: Int = SManga.ONGOING,
        favoriteAt: Instant? = null,
        latestUpload: Instant? = null,
        chapterFetchedAt: Instant? = null,
        lastRead: Instant? = null,
        hasNextReadableChapterDownloaded: Boolean = false,
        hasFullOfflineArchive: Boolean = false,
    ) = SmartShelfEntry(
        id = id,
        totalChapters = totalChapters,
        readCount = readCount,
        downloadCount = downloadCount,
        isLocal = isLocal,
        publicationStatus = publicationStatus,
        favoriteAt = favoriteAt,
        latestUpload = latestUpload,
        chapterFetchedAt = chapterFetchedAt,
        lastRead = lastRead,
        hasNextReadableChapterDownloaded = hasNextReadableChapterDownloaded,
        hasFullOfflineArchive = hasFullOfflineArchive,
    )

    @Test
    fun `reading the first chapter counts as currently reading`() {
        val started = entry(lastRead = fixedNow)
        YomuSmartShelfClassifier.matches(YomuSmartShelf.CURRENTLY_READING, started, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.CURRENTLY_READING, entry(), fixedNow).shouldBeFalse()
    }

    @Test
    fun `separates completed publication, caught up, and finished reading accurately`() {
        val ongoingCaughtUp = entry(
            id = 1L,
            totalChapters = 50L,
            readCount = 50L,
            publicationStatus = SManga.ONGOING,
        )
        val unknownCaughtUp = entry(
            id = 2L,
            totalChapters = 12L,
            readCount = 12L,
            publicationStatus = SManga.UNKNOWN,
        )
        val completedUnread = entry(
            id = 3L,
            totalChapters = 30L,
            readCount = 5L,
            publicationStatus = SManga.COMPLETED,
        )
        val finishedCompleted = entry(
            id = 4L,
            totalChapters = 30L,
            readCount = 30L,
            publicationStatus = SManga.COMPLETED,
        )
        val finishedPubFinished = entry(
            id = 5L,
            totalChapters = 10L,
            readCount = 10L,
            publicationStatus = SManga.PUBLISHING_FINISHED,
        )

        YomuSmartShelfClassifier.matches(YomuSmartShelf.CAUGHT_UP, ongoingCaughtUp, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FINISHED_READING, ongoingCaughtUp, fixedNow).shouldBeFalse()
        YomuSmartShelfClassifier.matches(
            YomuSmartShelf.COMPLETED_PUBLICATION,
            ongoingCaughtUp,
            fixedNow,
        ).shouldBeFalse()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.CAUGHT_UP, unknownCaughtUp, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(
            YomuSmartShelf.COMPLETED_PUBLICATION,
            unknownCaughtUp,
            fixedNow,
        ).shouldBeFalse()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.COMPLETED_PUBLICATION, completedUnread, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FINISHED_READING, completedUnread, fixedNow).shouldBeFalse()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.CAUGHT_UP, completedUnread, fixedNow).shouldBeFalse()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.FINISHED_READING, finishedCompleted, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(
            YomuSmartShelf.COMPLETED_PUBLICATION,
            finishedCompleted,
            fixedNow,
        ).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.CAUGHT_UP, finishedCompleted, fixedNow).shouldBeFalse()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.FINISHED_READING, finishedPubFinished, fixedNow).shouldBeTrue()
    }

    @Test
    fun `respects deterministic time boundaries for new this week, recently added, and forgotten`() {
        val newUnread = entry(totalChapters = 10L, readCount = 5L, latestUpload = fixedNow - 6.days)
        val oldUnread = entry(totalChapters = 10L, readCount = 5L, latestUpload = fixedNow - 8.days)
        val newAlreadyRead = entry(totalChapters = 10L, readCount = 10L, latestUpload = fixedNow - 2.days)

        YomuSmartShelfClassifier.matches(YomuSmartShelf.NEW_THIS_WEEK, newUnread, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.NEW_THIS_WEEK, oldUnread, fixedNow).shouldBeFalse()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.NEW_THIS_WEEK, newAlreadyRead, fixedNow).shouldBeFalse()

        val recentFav = entry(favoriteAt = fixedNow - 13.days)
        val olderFav = entry(favoriteAt = fixedNow - 15.days)
        YomuSmartShelfClassifier.matches(YomuSmartShelf.RECENTLY_ADDED, recentFav, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.RECENTLY_ADDED, olderFav, fixedNow).shouldBeFalse()

        val forgotten = entry(totalChapters = 10L, readCount = 2L, lastRead = fixedNow - 31.days)
        val notForgottenYet = entry(totalChapters = 10L, readCount = 2L, lastRead = fixedNow - 29.days)
        val oldButFinished = entry(totalChapters = 10L, readCount = 10L, lastRead = fixedNow - 45.days)
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FORGOTTEN, forgotten, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FORGOTTEN, notForgottenYet, fixedNow).shouldBeFalse()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FORGOTTEN, oldButFinished, fixedNow).shouldBeFalse()
    }

    @Test
    fun `separates ready offline from fully downloaded accurately`() {
        val localSeries = entry(id = 1L, totalChapters = 8L, readCount = 2L, isLocal = true)
        val nextDownloaded = entry(
            id = 2L,
            totalChapters = 10L,
            readCount = 2L,
            downloadCount = 1,
            hasNextReadableChapterDownloaded = true,
        )
        val fullArchive = entry(
            id = 3L,
            totalChapters = 10L,
            readCount = 2L,
            downloadCount = 10,
            hasNextReadableChapterDownloaded = true,
            hasFullOfflineArchive = true,
        )
        val noneDownloaded = entry(id = 4L, totalChapters = 10L, readCount = 2L, downloadCount = 0)

        YomuSmartShelfClassifier.matches(YomuSmartShelf.READY_OFFLINE, localSeries, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FULLY_DOWNLOADED, localSeries, fixedNow).shouldBeTrue()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.READY_OFFLINE, nextDownloaded, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FULLY_DOWNLOADED, nextDownloaded, fixedNow).shouldBeFalse()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.READY_OFFLINE, fullArchive, fixedNow).shouldBeTrue()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FULLY_DOWNLOADED, fullArchive, fixedNow).shouldBeTrue()

        YomuSmartShelfClassifier.matches(YomuSmartShelf.READY_OFFLINE, noneDownloaded, fixedNow).shouldBeFalse()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FULLY_DOWNLOADED, noneDownloaded, fixedNow).shouldBeFalse()
    }

    @Test
    fun `downloaded read chapters never imply the next chapter is ready offline`() {
        val misleadingCount = entry(totalChapters = 20L, readCount = 10L, downloadCount = 10)
        val singleReadDownload = entry(totalChapters = 20L, readCount = 10L, downloadCount = 1)
        YomuSmartShelfClassifier.matches(YomuSmartShelf.READY_OFFLINE, misleadingCount, fixedNow).shouldBeFalse()
        YomuSmartShelfClassifier.matches(YomuSmartShelf.READY_OFFLINE, singleReadDownload, fixedNow).shouldBeFalse()
        val staleArchiveCount = entry(totalChapters = 10L, downloadCount = 10)
        YomuSmartShelfClassifier.matches(YomuSmartShelf.FULLY_DOWNLOADED, staleArchiveCount, fixedNow).shouldBeFalse()
    }

    @Test
    fun `single-pass classify builds consistent snapshot counts across all shelves`() {
        val items = listOf(
            entry(id = 1L, totalChapters = 10L, readCount = 8L, hasNextReadableChapterDownloaded = true),
            entry(id = 2L, totalChapters = 10L, readCount = 0L, favoriteAt = fixedNow - 2.days),
            entry(id = 3L, totalChapters = 10L, readCount = 10L, publicationStatus = SManga.COMPLETED),
        )
        val snapshot = YomuSmartShelfClassifier.classify(items, fixedNow) { it }

        snapshot.countFor(YomuSmartShelf.ALL) shouldBe 3
        snapshot.countFor(YomuSmartShelf.CURRENTLY_READING) shouldBe 1
        snapshot.countFor(YomuSmartShelf.ALMOST_CAUGHT_UP) shouldBe 1
        snapshot.countFor(YomuSmartShelf.UNREAD) shouldBe 2
        snapshot.countFor(YomuSmartShelf.RECENTLY_ADDED) shouldBe 1
        snapshot.countFor(YomuSmartShelf.FINISHED_READING) shouldBe 1
        snapshot.countFor(YomuSmartShelf.READY_OFFLINE) shouldBe 1
        snapshot.itemsFor(YomuSmartShelf.FINISHED_READING).map { it.id } shouldBe listOf(3L)
    }
}
