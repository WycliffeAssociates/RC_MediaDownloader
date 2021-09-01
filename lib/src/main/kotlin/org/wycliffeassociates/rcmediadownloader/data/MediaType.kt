package org.wycliffeassociates.rcmediadownloader.data

enum class MediaType {
    WAV,
    PNG,
    MP3,
    CUE;

    override fun toString() = this.name.toLowerCase()

    companion object {
        fun get(type: String) = values().firstOrNull { it.name == type.toUpperCase() }
    }
}
