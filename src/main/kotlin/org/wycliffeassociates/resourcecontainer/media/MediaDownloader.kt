package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class MediaDownloader() {
    companion object {
        const val outputDir = "./media/"

        fun download(rcFile: File, urlParams: MediaUrlParameter): File {
            val rc = ResourceContainer.load(rcFile)
            val project = urlParams.projectId
            val chapter = urlParams.chapter
            val mediaType = urlParams.mediaTypes[0] // test with 1st type

            val mediaProject = rc.media?.projects?.firstOrNull { it.identifier == project }

            if (mediaProject != null){
                downloadMedia(mediaProject.media)
            }

            return rcFile
        }

        private fun downloadMedia(mediaList: List<Media>) {
            val contentDir = File("S:/Misc/download") // path in RC
            for (media in mediaList) {
                val url = media.url.replace("{latest}", "12")
                val fileName = File(url).name
                val outputFile = contentDir.resolve(fileName)

                // download file to RC
                downloadFromStream(url, outputFile)


                // update url

            }
        }

        private fun downloadFromStream(url: String, outputFile: File): File? {
            try {
                BufferedInputStream(URL(url).openStream()).use { inputStream ->
                    val bytes = inputStream.readBytes()

                    FileOutputStream(outputFile).use { outputStream ->
                        outputStream.write(bytes)
                    }
                    println(outputFile)
                    return outputFile
                }
            } catch (e: IOException) {
                println(e.message)
                return null
            }
        }
    }
}