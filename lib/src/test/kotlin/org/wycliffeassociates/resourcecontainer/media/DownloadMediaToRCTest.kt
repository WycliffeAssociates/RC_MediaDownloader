package org.wycliffeassociates.resourcecontainer.media

import org.junit.Assert.*
import org.junit.Test
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaType
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import java.io.File
import java.util.zip.ZipFile

class DownloadMediaToRCTest {
    @Test
    fun testFilesDownloaded() {
        val rcFilePath = javaClass.classLoader.getResource("titus.zip").file
        val rcFile = File(rcFilePath)
        val urlParameter = MediaUrlParameter(
            projectId = "tit",
            mediaTypes = listOf(MediaType.MP3),
            mediaDivision = MediaDivision.CHAPTER
        )
        val file = RCMediaDownloader.download(rcFile, urlParams = urlParameter, overwrite = false)
        val rc = ResourceContainer.load(file)

        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == "tit"
        }
        val media = mediaProject?.media?.firstOrNull {
            it.identifier == "mp3"
        }
        val chapterUrl = media?.chapterUrl

        if (chapterUrl.isNullOrEmpty()) fail()

        // check if entries contains the requested download files
        val rcZip = ZipFile(file)
        val listEntries = rcZip.entries().toList()

        var isMissingChapter = false
        for (chapterNumber in 1..3) {
            isMissingChapter = !listEntries.any {
                val pathInRC = chapterUrl!!.replace("{chapter}", chapterNumber.toString())
                it.name == "titus/$pathInRC"
            }

            if(isMissingChapter) break
        }

        rcZip.close()
        file.deleteRecursively()

        assertFalse(isMissingChapter)
    }
}