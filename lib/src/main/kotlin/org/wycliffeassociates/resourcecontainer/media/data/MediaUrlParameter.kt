package org.wycliffeassociates.resourcecontainer.media.data

data class MediaUrlParameter(
    val projectId: String,
    val mediaDivision: MediaDivision,
    val mediaTypes: List<MediaType>
)
