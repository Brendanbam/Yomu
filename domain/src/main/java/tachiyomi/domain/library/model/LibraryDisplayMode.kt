package tachiyomi.domain.library.model

sealed interface LibraryDisplayMode {

    data object ClassicBookshelf : LibraryDisplayMode
    data object CollectorEdition : LibraryDisplayMode
    data object BookSpine : LibraryDisplayMode
    data object Minimal : LibraryDisplayMode
    data object CompactGrid : LibraryDisplayMode
    data object ComfortableGrid : LibraryDisplayMode
    data object List : LibraryDisplayMode
    data object CoverOnlyGrid : LibraryDisplayMode

    object Serializer {
        fun deserialize(serialized: String): LibraryDisplayMode {
            return LibraryDisplayMode.deserialize(serialized)
        }

        fun serialize(value: LibraryDisplayMode): String {
            return value.serialize()
        }
    }

    companion object {
        val values by lazy {
            setOf(
                ClassicBookshelf,
                CollectorEdition,
                BookSpine,
                CompactGrid,
                Minimal,
                ComfortableGrid,
                List,
                CoverOnlyGrid,
            )
        }
        val default: LibraryDisplayMode = ClassicBookshelf

        fun deserialize(serialized: String): LibraryDisplayMode {
            return when (serialized) {
                "CLASSIC_BOOKSHELF" -> ClassicBookshelf
                "COLLECTOR_EDITION" -> CollectorEdition
                "BOOK_SPINE" -> BookSpine
                "MINIMAL" -> Minimal
                "COMFORTABLE_GRID" -> ComfortableGrid
                "COMPACT_GRID" -> CompactGrid
                "COVER_ONLY_GRID" -> CoverOnlyGrid
                "LIST" -> List
                else -> default
            }
        }
    }

    fun serialize(): String {
        return when (this) {
            ClassicBookshelf -> "CLASSIC_BOOKSHELF"
            CollectorEdition -> "COLLECTOR_EDITION"
            BookSpine -> "BOOK_SPINE"
            Minimal -> "MINIMAL"
            ComfortableGrid -> "COMFORTABLE_GRID"
            CompactGrid -> "COMPACT_GRID"
            CoverOnlyGrid -> "COVER_ONLY_GRID"
            List -> "LIST"
        }
    }
}
