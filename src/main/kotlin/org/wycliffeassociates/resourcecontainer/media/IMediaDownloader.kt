package org.wycliffeassociates.resourcecontainer.media

import java.io.File

interface IMediaDownloader {
    fun download(rcFile: File, urlParams: MediaUrlParameter): File
}