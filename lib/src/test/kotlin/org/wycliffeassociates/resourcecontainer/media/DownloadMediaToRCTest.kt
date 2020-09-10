package org.wycliffeassociates.resourcecontainer.media

import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.slf4j.LoggerFactory
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaType
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.DownloadClient
import org.wycliffeassociates.resourcecontainer.media.io.IDownloadClient
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile

class DownloadMediaToRCTest {
    val logger = LoggerFactory.getLogger(javaClass)
    val titusRCFileName = "titus-test.zip"

    @Test
    fun testChaptersDownload() {
        val projectId = "tit"
        val mediaType = MediaType.MP3
        val mediaDivision = MediaDivision.CHAPTER
        val rcFile = getTestFile(titusRCFileName)
        val urlParameter = MediaUrlParameter(projectId, mediaDivision, listOf(mediaType))
        val tempDir = createTempDir("testRC")
        val mockDownloadClient = mock(IDownloadClient::class.java)
        `when`(mockDownloadClient.downloadFromUrl(anyString(), this.any(File::class.java)))
            .thenAnswer {
                val downloadUrl = it.getArgument(0, String::class.java)
                defaultMediaFile(downloadUrl, tempDir)
            }

        val file = RCMediaDownloader.download(rcFile!!, urlParameter, mockDownloadClient, false)

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
        tempDir.deleteRecursively()
        file.deleteRecursively()

        assertFalse(isMissingChapter)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any(type)

    @Throws(FileNotFoundException::class)
    private fun getTestFile(name: String): File? {
        val rcFilePath = javaClass.classLoader.getResource(name)
        if (rcFilePath == null) {
            throw(FileNotFoundException("Test resource not found: $name"))
        }
        return File(rcFilePath.file)
    }

    private fun defaultMediaFile(url: String, tempDir: File): File? {
        val defaultFileNames = arrayOf(
            "en_nt_ulb_tit_c01.mp3",
            "en_nt_ulb_tit_c02.mp3",
            "en_nt_ulb_tit_c03.mp3"
        )

        return if (File(url).name in defaultFileNames) {
            tempDir.resolve(File(url).name).apply { createNewFile() }
        } else {
            null
        }
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
