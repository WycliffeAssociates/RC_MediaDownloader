package org.wycliffeassociates.rcmediadownloader

import java.io.File
import org.wycliffeassociates.rcmediadownloader.data.MediaDivision
import org.wycliffeassociates.rcmediadownloader.data.MediaUrlParameter
import org.wycliffeassociates.rcmediadownloader.io.IDownloadClient
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class BookMediaDownloader(
    urlParams: MediaUrlParameter,
    downloadClient: IDownloadClient
) : RCMediaDownloader(urlParams, downloadClient) {

    override fun downloadMedia(url: String, rc: ResourceContainer): String {
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
