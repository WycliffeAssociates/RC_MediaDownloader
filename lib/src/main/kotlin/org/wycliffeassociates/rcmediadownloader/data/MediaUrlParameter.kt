package org.wycliffeassociates.rcmediadownloader.data

data class MediaUrlParameter(
    val projectId: String,
    val mediaDivision: MediaDivision,
    val mediaTypes: List<MediaType>,
    val chapter: Int? = null
)
