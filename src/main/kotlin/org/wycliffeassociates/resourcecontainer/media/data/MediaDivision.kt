package org.wycliffeassociates.resourcecontainer.media.data

enum class MediaDivision {
    BOOK,
    CHAPTER;

    companion object {
        inline fun get(division: String) = values().firstOrNull { it.name == division.toUpperCase() }
    }
}
