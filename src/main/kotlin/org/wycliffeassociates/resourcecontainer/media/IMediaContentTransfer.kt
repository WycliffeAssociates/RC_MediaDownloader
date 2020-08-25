package org.wycliffeassociates.resourcecontainer.media

import java.io.File

interface IMediaContentTransfer {
    fun pullMediaFiles(rc: File, urlParams: MediaUrlParameter): File
}