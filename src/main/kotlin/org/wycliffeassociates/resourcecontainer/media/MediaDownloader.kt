package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class MediaDownloader private constructor(
        rcFile: File,
        private val urlParams: MediaUrlParameter
) {
    private val mediaDir = "media"
    private val rc = ResourceContainer.load(rcFile)

    companion object {
        fun download(rcFile: File, urlParams: MediaUrlParameter): File {
            val downloader = MediaDownloader(rcFile, urlParams)
            return downloader.download()
        }
    }

    private fun download(): File {
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == urlParams.projectId
        }

        if (mediaProject != null) {
            val updatedMedia =
                    downloadProjectMedia(mediaProject.identifier, mediaProject.media)
            mediaProject.media = updatedMedia
        }
        rc.writeMedia()
        return File("")
    }

    private fun downloadProjectMedia(
            projectId: String,
            mediaList: List<Media>,
            mediaTypes: List<MediaType> = listOf()
    ): List<Media> {
        val contentDir = createTempDir().apply { deleteOnExit() }

        for (media in mediaList) {
            val url = media.url.replace("{latest}", "12") // replace url template variables

            val downloadedFile = downloadFromStream(url, contentDir)
            if (downloadedFile != null) {
                val pathInRC = "$mediaDir/$projectId/${downloadedFile.name}"
                val templatePath = templatePathInRC(
                        downloadedFile.nameWithoutExtension,
                        isChapter = media.chapterUrl.isNotEmpty()
                )
                rc.addFileToContainer(downloadedFile, pathInRC)
                media.url = pathInRC
            }
        }

        return mediaList
    }

    private fun downloadFromStream(url: String, outputDir: File): File? {
        val fileName = File(url).name
        val outputFile = outputDir.resolve(fileName)

        try {
            BufferedInputStream(URL(url).openStream()).use { inputStream ->
                val bytesReceived = inputStream.readBytes()

                FileOutputStream(outputFile).buffered().use { outputStream ->
                    outputStream.write(bytesReceived)
                }
                return outputFile
            }
        } catch (e: IOException) {
            println(e.message)
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun templatePathInRC(
            fileNameNoExt: String,
            isChapter: Boolean,
            isProject: Boolean = true
    ): String {
        return when {
            isChapter -> {
                val rx = Regex("_c[0-9]{1,3}")
                val chapterSlug = rx.find(fileNameNoExt)?.value
                        ?: throw IllegalArgumentException("Corrupted file name")
                val templatedFileName = fileNameNoExt.replace(chapterSlug, "_c{chapter}")
                "$mediaDir/{project}/chapters/$templatedFileName.{mediaType}"
            }
            isProject -> "$mediaDir/{project}/$fileNameNoExt.{mediaType}"
            else -> "$mediaDir/$fileNameNoExt.{mediaType}"
        }
    }
}
