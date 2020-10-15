package org.wycliffeassociates.rcmediadownloader

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        val chapterUrlList = mutableListOf<String>()

        for (chapterNumber in 1..possibleChapterRange) {
            val chapterUrl = templateUrl.replace("{chapter}", chapterNumber.toString())
            chapterUrlList.add(chapterUrl)
        }

        val contentDir = createTempDir()
        val filesToRCMap: ConcurrentHashMap<String, File> = ConcurrentHashMap()

        runBlocking {
            chapterUrlList.forEach { url ->
                launch {
                    val downloadFile = downloadClient.downloadFromUrl(url, contentDir)
                    if (downloadFile != null) {
                        val pathInRC = templatePathInRC(File(url).name, MediaDivision.CHAPTER)
                        filesToRCMap[pathInRC] = downloadFile
                    }
                }
            }
        }

        rc.addFilesToContainer(filesToRCMap)
        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(templateUrl).name,
            MediaDivision.CHAPTER
        )
    }
}
