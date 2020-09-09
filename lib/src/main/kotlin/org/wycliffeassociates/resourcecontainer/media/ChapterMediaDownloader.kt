package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.IDownloadClient
import java.io.File
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream


class ChapterMediaDownloader(
    rcFile: File,
    overwrite: Boolean,
    urlParams: MediaUrlParameter,
    downloadClient: IDownloadClient
) : RCMediaDownloader(rcFile, overwrite, urlParams, downloadClient) {

    override fun downloadMedia(url: String): String {
        val contentDir = createTempDir()
        val chapterUrlList = mutableListOf<String>()
        val possibleChapterRange = 200

        for (chapterNumber in 1..possibleChapterRange) {
            val chapterUrl = url.replace("{chapter}", chapterNumber.toString())
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
        )

        rc.addFilesToContainer(filesToRCMap.filter { it.value.name.isNotEmpty() })
        contentDir.deleteRecursively() // delete temp dir after downloaded

        return templatePathInRC(
            File(url).name,
            MediaDivision.CHAPTER
        )
    }
}
