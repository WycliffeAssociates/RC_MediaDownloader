package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.URL


class RCMediaDownloader private constructor(
    rcFile: File,
    overwrite: Boolean,
    private val urlParams: MediaUrlParameter
) {
    private val rcOutputFile: File = if (overwrite) {
        rcFile
    } else {
        rcFile.copyTo(
            rcFile.parentFile
                .resolve(rcFile.nameWithoutExtension + "_updated." + rcFile.extension)
        )
    }
    private val rc = ResourceContainer.load(rcOutputFile)

    companion object {
        private const val MEDIA_DIR = "media"

        fun download(
            rcFile: File,
            urlParams: MediaUrlParameter,
            overwrite: Boolean = false
        ): File {
            val downloader = RCMediaDownloader(rcFile, overwrite, urlParams)
            return downloader.execute()
        }
    }

    private fun execute(): File {
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == urlParams.projectId
        }

        if (mediaProject != null) {
            for (mediaType in urlParams.mediaTypes) {
                // filter mediaType to download
                val media = mediaProject.media.firstOrNull {
                    it.identifier == mediaType.name.toLowerCase()
                }

                if (media != null) {
                    if (urlParams.isChaptersDownload) {
                        media.chapterUrl = downloadChaptersMedia(media.chapterUrl)
                    } else {
                        media.url = downloadProjectMedia(media.url)
                    }
                }
            }
        }

        rc.writeMedia()
        return rcOutputFile
    }

    // download a project
    private fun downloadProjectMedia(url: String): String {
        val contentDir = createTempDir().apply { deleteOnExit() }

        val downloadedFile = downloadFromStream(url, contentDir)
        if (downloadedFile != null) {
            val pathInRC = "$MEDIA_DIR/${urlParams.projectId}/${downloadedFile.name}"
            rc.addFileToContainer(downloadedFile, pathInRC)
        }

        return templatePathInRC(
            File(url).name,
            isChapter = false
        )
    }

    // download all chapters
    private fun downloadChaptersMedia(url: String): String {
        val contentDir = createTempDir().apply { deleteOnExit() }
        val filesToRCMap = mutableMapOf<String, File>()

        for (chapterNumber in 1..200) {
            val downloadUrl = url.replace("{chapter}", chapterNumber.toString())

            val downloadedFile = downloadFromStream(downloadUrl, contentDir)
            if (downloadedFile != null) {
                // add file to container
                val pathInRC = "$MEDIA_DIR/${urlParams.projectId}/chapters/${downloadedFile.name}"
                filesToRCMap[pathInRC] = downloadedFile
            }
        }
        rc.addFilesToContainer(filesToRCMap)

        return templatePathInRC(
            File(url).name,
            isChapter = true
        )
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

    private fun templatePathInRC(
        fileName: String,
        isChapter: Boolean
    ): String {
        return if (isChapter) {
            "$MEDIA_DIR/${urlParams.projectId}/chapters/$fileName"
        } else {
            "$MEDIA_DIR/${urlParams.projectId}/$fileName"
        }
    }
}
