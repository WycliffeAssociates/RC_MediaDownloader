package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class MediaDownloader private constructor(private val rcFile: File) {
    val outputDir = "./media/"
    private val rc = ResourceContainer.load(rcFile)

    companion object {
        fun download(rcFile: File, urlParams: MediaUrlParameter): File {
            val downloader = MediaDownloader(rcFile)
            return downloader.download(urlParams)
        }
    }

    private fun download(urlParams: MediaUrlParameter): File {
        val project = urlParams.projectId
//        val chapter = urlParams.chapter

        val mediaProject = rc.media?.projects?.firstOrNull { it.identifier == project }

        if (mediaProject != null) {
            val updatedMedia = downloadProjectMedia(mediaProject.identifier, mediaProject.media)
            mediaProject.media = updatedMedia
        }
        rc.writeMedia()
        return rcFile
    }

    private fun downloadProjectMedia(projectId: String, mediaList: List<Media>): List<Media> {
        val contentDir = createTempDir()
        contentDir.deleteOnExit()

        for (media in mediaList) {
            val url = media.url.replace("{latest}", "12") // replace url template variables

            val downloadedFile = downloadFromStream(url, contentDir)
            if (downloadedFile != null) {
                val pathInRC = "media/$projectId/${downloadedFile.name}"
                rc.addFileToContainer(downloadedFile, pathInRC)
                media.url = pathInRC
            }
            // update url
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
}

fun main() {

    val rcFile = File("/home/dj/Downloads/en_ulb.zip")
    val parameter = MediaUrlParameter(
        projectId = "gen",
        mediaTypes = listOf(MediaType.WAV)
    )

    val download = MediaDownloader.download(rcFile, parameter)

}
