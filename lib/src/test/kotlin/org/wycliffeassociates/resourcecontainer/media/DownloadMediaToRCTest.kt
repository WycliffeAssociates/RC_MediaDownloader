package org.wycliffeassociates.resourcecontainer.media

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaType
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import java.io.File
import java.util.zip.ZipFile

class DownloadMediaToRCTest {
    @Test
    fun downloadMedia() {
        val rcFilePath = javaClass.classLoader.getResource("titus.zip").file
        val rcFile = File(rcFilePath)
        val urlParameter = MediaUrlParameter(
            projectId = "tit",
            mediaTypes = listOf(MediaType.MP3),
            mediaDivision = MediaDivision.CHAPTER
        )
        val file = RCMediaDownloader.download(rcFile, urlParams = urlParameter, overwrite = false)

        // verify result
        val rc = ResourceContainer.load(file)
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == "tit"
        }
        val media = mediaProject?.media?.firstOrNull() {
            it.identifier == "mp3"
        }
        val chapterUrl = media?.chapterUrl
        if (chapterUrl.isNullOrEmpty()) fail()

        val listEntries = ZipFile(file).entries().toList()
        // check if entries contains the requested download files
        var flag = false
        for (i in 1..200) {
            flag = listEntries.any {
                it.name == "titus/${chapterUrl!!.replace("{chapter}", i.toString())}"
            }
            if (flag) break
        }
        assertTrue(flag)
    }
}