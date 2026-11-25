package eu.kanade.domain.yomu

import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.source.model.SManga
import tachiyomi.i18n.MR
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

enum class YomuSmartShelf(val titleRes: StringResource) {
    ALL(MR.strings.yomu_smart_all),
    CURRENTLY_READING(MR.strings.yomu_smart_currently_reading),
    UNREAD(MR.strings.yomu_smart_unread),
    NEW_THIS_WEEK(MR.strings.yomu_smart_new_this_week),
    RECENTLY_ADDED(MR.strings.yomu_smart_recently_added),
    ALMOST_CAUGHT_UP(MR.strings.yomu_smart_almost_caught_up),
    CAUGHT_UP(MR.strings.yomu_smart_caught_up),
    FINISHED_READING(MR.strings.yomu_smart_finished_reading),
    COMPLETED_PUBLICATION(MR.strings.yomu_smart_completed_publication),
    FORGOTTEN(MR.strings.yomu_smart_forgotten),
    READY_OFFLINE(MR.strings.yomu_smart_ready_offline),
    FULLY_DOWNLOADED(MR.strings.yomu_smart_fully_downloaded),
}

/**
 * Pure metadata projection used by [YomuSmartShelfClassifier] so classification
 * can be unit-tested deterministically without Android or database dependencies.
 */
data class SmartShelfEntry(
    val id: Long,
    val totalChapters: Long,
    val readCount: Long,
    val downloadCount: Int,
    val isLocal: Boolean,
    val publicationStatus: Int,
    val favoriteAt: Instant?,
    val latestUpload: Instant?,
    val chapterFetchedAt: Instant?,
    val lastRead: Instant?,
    val hasNextReadableChapterDownloaded: Boolean = false,
) {
    val unreadCount: Long
        get() = (totalChapters - readCount).coerceAtLeast(0L)

    val hasStarted: Boolean
        get() = readCount > 0L

    val isPublicationCompleted: Boolean
        get() = publicationStatus == SManga.COMPLETED ||
            publicationStatus == SManga.PUBLISHING_FINISHED
}

data class SmartShelfSnapshot<T>(
    val itemsByShelf: Map<YomuSmartShelf, List<T>>,
    val counts: Map<YomuSmartShelf, Int>,
) {
    fun itemsFor(shelf: YomuSmartShelf): List<T> = itemsByShelf[shelf].orEmpty()
    fun countFor(shelf: YomuSmartShelf): Int = counts[shelf] ?: 0
}

/**
 * Single-pass deterministic Smart Shelf classifier.
 *
 * Separates publication completion ([YomuSmartShelf.COMPLETED_PUBLICATION]),
 * ongoing catch-up ([YomuSmartShelf.CAUGHT_UP]), fully finished completed works
 * ([YomuSmartShelf.FINISHED_READING]), practical offline readiness
 * ([YomuSmartShelf.READY_OFFLINE]), and complete offline archive
 * ([YomuSmartShelf.FULLY_DOWNLOADED]).
 */
object YomuSmartShelfClassifier {

    val NEW_THIS_WEEK_WINDOW = 7.days
    val RECENTLY_ADDED_WINDOW = 14.days
    val FORGOTTEN_WINDOW = 30.days

    fun matches(
        shelf: YomuSmartShelf,
        entry: SmartShelfEntry,
        now: Instant = Clock.System.now(),
    ): Boolean {
        val unread = entry.unreadCount
        val weekAgo = now - NEW_THIS_WEEK_WINDOW
        val twoWeeksAgo = now - RECENTLY_ADDED_WINDOW
        val thirtyDaysAgo = now - FORGOTTEN_WINDOW

        return when (shelf) {
            YomuSmartShelf.ALL -> true
            YomuSmartShelf.CURRENTLY_READING -> entry.hasStarted && unread > 0L
            YomuSmartShelf.UNREAD -> unread > 0L
            YomuSmartShelf.NEW_THIS_WEEK -> {
                val latest = entry.latestUpload ?: entry.chapterFetchedAt
                latest != null && latest >= weekAgo && unread > 0L
            }
            YomuSmartShelf.RECENTLY_ADDED -> {
                val fav = entry.favoriteAt
                fav != null && fav >= twoWeeksAgo
            }
            YomuSmartShelf.ALMOST_CAUGHT_UP -> entry.hasStarted && unread in 1L..3L
            YomuSmartShelf.CAUGHT_UP -> {
                entry.totalChapters > 0L && unread == 0L && !entry.isPublicationCompleted
            }
            YomuSmartShelf.FINISHED_READING -> {
                entry.totalChapters > 0L && unread == 0L && entry.isPublicationCompleted
            }
            YomuSmartShelf.COMPLETED_PUBLICATION -> entry.isPublicationCompleted
            YomuSmartShelf.FORGOTTEN -> {
                val last = entry.lastRead
                last != null && last < thirtyDaysAgo && unread > 0L
            }
            YomuSmartShelf.READY_OFFLINE -> {
                entry.isLocal ||
                    entry.hasNextReadableChapterDownloaded ||
                    (unread > 0L && entry.downloadCount >= unread)
            }
            YomuSmartShelf.FULLY_DOWNLOADED -> {
                entry.isLocal ||
                    (entry.totalChapters > 0L && entry.downloadCount >= entry.totalChapters)
            }
        }
    }

    fun <T> classify(
        items: List<T>,
        now: Instant = Clock.System.now(),
        toEntry: (T) -> SmartShelfEntry,
    ): SmartShelfSnapshot<T> {
        val buckets = YomuSmartShelf.entries.associateWith { mutableListOf<T>() }
        for (item in items) {
            val entry = toEntry(item)
            for (shelf in YomuSmartShelf.entries) {
                if (matches(shelf, entry, now)) {
                    buckets.getValue(shelf).add(item)
                }
            }
        }
        val counts = buckets.mapValues { it.value.size }
        return SmartShelfSnapshot(itemsByShelf = buckets, counts = counts)
    }
}
