package org.wycliffeassociates.resourcecontainer.media

import java.io.File
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.IDownloadClient

class BookMediaDownloader(
    rcFile: File,
    overwrite: Boolean,
    urlParams: MediaUrlParameter,
    downloadClient: IDownloadClient
) : RCMediaDownloader(rcFile, overwrite, urlParams, downloadClient) {

    override fun downloadMedia(url: String): String {
        val contentDir = createTempDir()
        val downloadedFile = downloadClient.downloadFromUrl(url, contentDir)

        if (downloadedFile != null) {
            val pathInRC = "$MEDIA_DIR/${urlParams.projectId}/${downloadedFile.name}"
            rc.addFileToContainer(downloadedFile, pathInRC)
        }
        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(url).name,
            MediaDivision.BOOK
        )
    }
}
