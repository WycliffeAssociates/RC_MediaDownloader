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
    fun testChaptersDownload() {
        val projectId = "tit"
        val mediaType = MediaType.MP3
        val mediaDivision = MediaDivision.CHAPTER
        val rcFilePath = javaClass.classLoader.getResource("titus-test.zip").file
        val rcFile = File(rcFilePath)

        val urlParameter = MediaUrlParameter(projectId, mediaDivision, listOf(mediaType))
        val file = RCMediaDownloader.download(rcFile, urlParams = urlParameter, overwrite = false)

        val chapterUrl = getUrl(file, projectId, mediaDivision, mediaType)
        if (chapterUrl.isNullOrEmpty()) fail("Chapter url not found")

        // check if entries contain the requested download files
        val rcZip = ZipFile(file)
        val listEntries = rcZip.entries().toList()

        var isMissingChapter = false
        for (chapterNumber in 1..3) {
            isMissingChapter = !listEntries.any { entry ->
                val pathInMediaManifest = chapterUrl!!.replace("{chapter}", chapterNumber.toString())
                entry.name == "titus/$pathInMediaManifest"
            }

            if (isMissingChapter) break
        }

        rcZip.close()
        file.deleteRecursively()

        assertFalse(isMissingChapter)
    }

    private fun getUrl(
        rcFile: File,
        projectId: String,
        mediaDivision: MediaDivision,
        mediaType: MediaType
    ): String? {
        val rc = ResourceContainer.load(rcFile)
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == projectId
        }
        val media = mediaProject?.media?.firstOrNull {
            it.identifier == mediaType.name.toLowerCase()
        }

        return when (mediaDivision) {
            MediaDivision.CHAPTER -> media?.chapterUrl
            else -> media?.url
        }
    }
}
