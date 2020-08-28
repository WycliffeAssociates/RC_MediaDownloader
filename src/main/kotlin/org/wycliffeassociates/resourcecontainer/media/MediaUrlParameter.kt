package org.wycliffeassociates.resourcecontainer.media

data class MediaUrlParameter(
    val projectId: String,
    val mediaTypes: List<MediaType>,
    val isChaptersDownload: Boolean = false
)
