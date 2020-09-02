package org.wycliffeassociates.resourcecontainer.media

import java.io.File
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.DownloadClient

class RCMediaDownloader private constructor(
    rcFile: File,
    overwrite: Boolean,
    private val urlParams: MediaUrlParameter
) {
    private val rcOutputFile: File = if (overwrite) {
        rcFile
    } else {
        // create a new copy next to the original RC file
        rcFile.copyTo(
            rcFile.parentFile.resolve(rcFile.nameWithoutExtension + "_updated." + rcFile.extension),
            overwrite = true
        )
    }
    private val rc = ResourceContainer.load(rcOutputFile)

    companion object {
        private const val MEDIA_DIR = "media"

        fun download(
            rcFile: File,
            urlParams: MediaUrlParameter,
            overwrite: Boolean = false
        ): File {
            val downloader = RCMediaDownloader(rcFile, overwrite, urlParams)
            return downloader.execute()
        }
    }

    private fun execute(): File {
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == urlParams.projectId
        } ?: return rcOutputFile

        for (mediaType in urlParams.mediaTypes) {
            // filter mediaType to download
            val media = mediaProject.media.firstOrNull {
                it.identifier == mediaType.name.toLowerCase()
            }

            if (media != null) {
                if (urlParams.isChaptersDownload) {
                    media.chapterUrl = downloadChaptersMedia(media.chapterUrl)
                } else {
                    media.url = downloadProjectMedia(media.url)
                }
            }
        }

        rc.writeMedia()
        return rcOutputFile
    }

    private fun downloadProjectMedia(url: String): String {
        val contentDir = createTempDir().apply { deleteOnExit() }
        val downloadedFile = DownloadClient.downloadFromUrl(url, contentDir)

        if (downloadedFile != null) {
            val pathInRC = "$MEDIA_DIR/${urlParams.projectId}/${downloadedFile.name}"
            rc.addFileToContainer(downloadedFile, pathInRC)
        }

        return templatePathInRC(
            File(url).name,
            isChapter = false
        )
    }

    private fun downloadChaptersMedia(url: String): String {
        val contentDir = createTempDir().apply { deleteOnExit() }
        val filesToRCMap = mutableMapOf<String, File>()
        val chapterUrlList = mutableListOf<String>()
        val possibleChapterRange = 200

        for (chapterNumber in 1..possibleChapterRange) {
            val chapterUrl = url.replace("{chapter}", chapterNumber.toString())
            chapterUrlList.add(chapterUrl)
        }

        chapterUrlList.parallelStream().forEach { downloadUrl ->
            val downloadedFile = DownloadClient.downloadFromUrl(downloadUrl, contentDir)

            if (downloadedFile != null) {
                val pathInRC = "$MEDIA_DIR/${urlParams.projectId}/chapters/${downloadedFile.name}"
                filesToRCMap[pathInRC] = downloadedFile // save File to map for later multiple addition
            }
        }
        rc.addFilesToContainer(filesToRCMap)

        return templatePathInRC(
            File(url).name,
            isChapter = true
        )
    }

    private fun templatePathInRC(
        fileName: String,
        isChapter: Boolean
    ): String {
        return if (isChapter) {
            "$MEDIA_DIR/${urlParams.projectId}/chapters/$fileName"
        } else {
            "$MEDIA_DIR/${urlParams.projectId}/$fileName"
        }
    }
}
