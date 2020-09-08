package org.wycliffeassociates.resourcecontainer.media

import java.io.File
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.DownloadClient

class ChapterMediaDownloader(
    rcFile: File,
    overwrite: Boolean,
    urlParams: MediaUrlParameter
) : RCMediaDownloader(rcFile, overwrite, urlParams) {

    override fun downloadMedia(url: String): String {
        val contentDir = createTempDir()
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
        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(url).name,
            MediaDivision.CHAPTER
        )
    }
}
