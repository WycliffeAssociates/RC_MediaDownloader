package org.wycliffeassociates.resourcecontainer.media.data

import org.wycliffeassociates.resourcecontainer.media.data.MediaType

data class MediaUrlParameter(
    val projectId: String,
    val mediaTypes: List<MediaType>,
    val isChaptersDownload: Boolean = false
)
