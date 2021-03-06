package org.wycliffeassociates.rcmediadownloader

import java.io.File
import java.util.stream.Collectors
import org.wycliffeassociates.rcmediadownloader.data.MediaDivision
import org.wycliffeassociates.rcmediadownloader.data.MediaUrlParameter
import org.wycliffeassociates.rcmediadownloader.io.IDownloadClient
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ChapterMediaDownloader(
    urlParams: MediaUrlParameter,
    downloadClient: IDownloadClient
) : RCMediaDownloader(urlParams, downloadClient) {

    override fun downloadMedia(url: String, rc: ResourceContainer): String {
        return if (urlParams.chapter != null) {
            downloadSingleChapter(urlParams.chapter, url, rc)
        } else {
            downloadAllChapters(url, rc)
        }
    }

    private fun downloadSingleChapter(chapterNumber: Int, templateUrl: String, rc: ResourceContainer): String {
        val contentDir = createTempDir()
        val formattedUrl = templateUrl.replace("{chapter}", chapterNumber.toString())

        val downloadedFile = downloadClient.downloadFromUrl(formattedUrl, contentDir)
        if (downloadedFile != null) {
            val pathInRC = templatePathInRC(downloadedFile.name, MediaDivision.CHAPTER)
            rc.addFileToContainer(downloadedFile, pathInRC)
        }

        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(templateUrl).name,
            MediaDivision.CHAPTER
        )
    }

    private fun downloadAllChapters(templateUrl: String, rc: ResourceContainer): String {
        val possibleChapterRange = 200
        val contentDir = createTempDir()
        val chapterUrlList = mutableListOf<String>()

        for (chapterNumber in 1..possibleChapterRange) {
            val chapterUrl = templateUrl.replace("{chapter}", chapterNumber.toString())
            chapterUrlList.add(chapterUrl)
        }

        val filesToRCMap = chapterUrlList.parallelStream().collect(
            Collectors.toConcurrentMap(
                { downloadUrl: String ->
                    "$MEDIA_DIR/${urlParams.projectId}/chapters/${File(downloadUrl).name}"
                },
                { downloadUrl: String ->
                    downloadClient.downloadFromUrl(downloadUrl, contentDir) ?: File("")
                },
                { f1: File, f2: File -> if (f1.isFile) f1 else f2 }
            )
        ).filter { it.value.name.isNotEmpty() }

        rc.addFilesToContainer(filesToRCMap)
        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(templateUrl).name,
            MediaDivision.CHAPTER
        )
    }
}
