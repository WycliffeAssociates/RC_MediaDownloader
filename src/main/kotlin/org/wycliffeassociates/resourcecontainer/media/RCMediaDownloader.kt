package org.wycliffeassociates.resourcecontainer.media

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import okhttp3.ResponseBody
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import retrofit2.Retrofit

class RCMediaDownloader private constructor(
    rcFile: File,
    overwrite: Boolean,
    private val urlParams: MediaUrlParameter
) {
    private val rcOutputFile: File = if (overwrite) {
        rcFile
    } else {
        // create a new copy next to the original RC file
        rcFile.copyTo(
            rcFile.parentFile.resolve(rcFile.nameWithoutExtension + "_updated." + rcFile.extension),
            overwrite = true
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

        val downloadedFile = downloadWithClient(url, contentDir)
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
        val chapterUrlList = mutableListOf<String>()

        for (chapterNumber in 1..200) {
            val chapterUrl = url.replace("{chapter}", chapterNumber.toString())
            chapterUrlList.add(chapterUrl)
        }

        chapterUrlList.parallelStream().forEach { downloadUrl ->
            val downloadedFile = downloadWithClient(downloadUrl, contentDir)
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

    private fun downloadWithClient(url: String, outputDir: File): File? {
        val urlFile = File(url)
        val outputFile = outputDir.resolve(urlFile.name)

        val retrofitService = Retrofit.Builder()
            .baseUrl(urlFile.parentFile.invariantSeparatorsPath + "/")
            .build()
        val downloader: FileDownloadClient = retrofitService.create(FileDownloadClient::class.java)

        val call = downloader.downloadFile(urlFile.name)
        val response = call.execute()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                println("content not found")
            } else {
                writeTempDownload(body, outputFile)
            }
        }

        return if (outputFile.isFile) outputFile else null
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

    private fun writeTempDownload(body: ResponseBody, outputFile: File): File {
        BufferedInputStream(body.byteStream()).use { inputStream ->
            val bytes = inputStream.readBytes()

            FileOutputStream(outputFile).buffered().use { outputStream ->
                outputStream.write(bytes)
            }
        }
        return outputFile
    }
}
