package org.wycliffeassociates.resourcecontainer.media

import java.io.File
import org.wycliffeassociates.resourcecontainer.media.data.MediaContent
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.DownloadClient

class BookMediaDownloader(
    rcFile: File,
    overwrite: Boolean,
    urlParams: MediaUrlParameter
) : RCMediaDownloader(rcFile, overwrite, urlParams) {

    override fun execute(): File {
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == urlParams.projectId
        } ?: return rcOutputFile

        for (mediaType in urlParams.mediaTypes) {
            // filter mediaType to download
            val media = mediaProject.media.firstOrNull {
                it.identifier == mediaType.name.toLowerCase()
            }

            if (media != null) {
                media.url = downloadMedia(media.url)
            }
        }

        rc.writeMedia()
        return rcOutputFile
    }

    override fun downloadMedia(url: String): String {
        val contentDir = createTempDir().apply { deleteOnExit() }
        val downloadedFile = DownloadClient.downloadFromUrl(url, contentDir)

        if (downloadedFile != null) {
            val pathInRC = "${RCMediaDownloader.MEDIA_DIR}/${urlParams.projectId}/${downloadedFile.name}"
            rc.addFileToContainer(downloadedFile, pathInRC)
        }
        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(url).name,
            MediaContent.BOOK
        )
    }
}
