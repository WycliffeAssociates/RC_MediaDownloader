package org.wycliffeassociates.rcmediadownloader

import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.slf4j.LoggerFactory
import org.wycliffeassociates.rcmediadownloader.data.MediaDivision
import org.wycliffeassociates.rcmediadownloader.data.MediaType
import org.wycliffeassociates.rcmediadownloader.data.MediaUrlParameter
import org.wycliffeassociates.rcmediadownloader.io.IDownloadClient
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class DownloadMediaToRCTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val rcFileName = "titus-test.zip"
    private val projectId = "tit"
    private val mediaDivision = MediaDivision.CHAPTER
    private val mediaType = MediaType.MP3

    @Test
    fun testDownloadSingleChapter() {
        val chapterNumber = 1
        val tempDir = createTempDir("testRC")
        val mockDownloadClient = mock(IDownloadClient::class.java)
        `when`(mockDownloadClient.downloadFromUrl(anyString(), this.any(File::class.java)))
            .thenReturn(
                tempDir.resolve("en_nt_ulb_tit_c01.mp3").apply { createNewFile() }
            )

        val file = RCMediaDownloader.download(
            getTestFile(rcFileName),
            MediaUrlParameter(projectId, mediaDivision, listOf(mediaType), chapterNumber),
            mockDownloadClient,
            overwrite = false
        )
        verify(mockDownloadClient).downloadFromUrl(anyString(), this.any(File::class.java))

        val chapterUrl = getMediaUrl(file, projectId, mediaDivision, mediaType)
        if (chapterUrl.isNullOrEmpty()) {
            return fail("Chapter url not found")
        }

//         check if entries contain the requested download files
        var isExisting = false
        val pathInMediaManifest = chapterUrl.replace("{chapter}", chapterNumber.toString())
        ZipFile(file).use { rcZip ->
            val listEntries = rcZip.entries().toList()
            isExisting = listEntries.any { entry ->
                entry.name == "titus/$pathInMediaManifest"
            }
        }

        tempDir.deleteRecursively()
        file.deleteRecursively()

        assertTrue(isExisting)
    }

    @Test
    fun testDownloadAllChapters() {
        val tempDir = createTempDir("testRC")
        val mockDownloadClient = mock(IDownloadClient::class.java)

        `when`(mockDownloadClient.downloadFromUrl(anyString(), this.any(File::class.java)))
            .thenAnswer {
                val downloadUrl = it.getArgument(0, String::class.java)
                defaultMediaFile(downloadUrl, tempDir)
            }

        val file = RCMediaDownloader.download(
            getTestFile(rcFileName),
            MediaUrlParameter(projectId, mediaDivision, listOf(mediaType)),
            mockDownloadClient,
            overwrite = false
        )

        verify(mockDownloadClient, times(200))
            .downloadFromUrl(anyString(), this.any(File::class.java))

        val chapterUrl = getMediaUrl(file, projectId, mediaDivision, mediaType)
        if (chapterUrl.isNullOrEmpty()) {
            return fail("Chapter url not found")
        }

//         check if entries contain the requested download files
        var isMissingChapter = false
        ZipFile(file).use { rcZip ->
            val listEntries = rcZip.entries().toList()

            for (chapterNumber in 1..3) {
                isMissingChapter = !listEntries.any { entry ->
                    val pathInMediaManifest = chapterUrl.replace("{chapter}", chapterNumber.toString())
                    entry.name == "titus/$pathInMediaManifest"
                }

                if (isMissingChapter) break
            }
        }

        tempDir.deleteRecursively()
        file.deleteRecursively()

        assertFalse(isMissingChapter)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any(type)

    @Throws(FileNotFoundException::class)
    private fun getTestFile(name: String): File {
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

    private fun getMediaUrl(
        rcFile: File,
        projectId: String,
        mediaDivision: MediaDivision,
        mediaType: MediaType
    ): String? {
        ResourceContainer.load(rcFile).use { rc ->
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
}
