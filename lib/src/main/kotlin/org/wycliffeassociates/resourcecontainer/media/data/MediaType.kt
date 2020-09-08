package org.wycliffeassociates.resourcecontainer.media.data

enum class MediaType {
    WAV,
    PNG;

    companion object {
        fun get(type: String) = values().firstOrNull { it.name == type.toUpperCase() }
    }
}
