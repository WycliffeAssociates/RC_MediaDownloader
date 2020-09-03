package org.wycliffeassociates.resourcecontainer.media.data

enum class MediaType {
    WAV,
    PNG;

    companion object {
        inline fun get(type: String) = MediaType.values().firstOrNull { it.name == type.toUpperCase() }
    }
}
