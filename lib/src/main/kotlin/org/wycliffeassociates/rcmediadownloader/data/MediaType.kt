package org.wycliffeassociates.rcmediadownloader.data

enum class MediaType {
    WAV,
    PNG,
    MP3;

    companion object {
        fun get(type: String) = values().firstOrNull { it.name == type.toUpperCase() }
    }
}
