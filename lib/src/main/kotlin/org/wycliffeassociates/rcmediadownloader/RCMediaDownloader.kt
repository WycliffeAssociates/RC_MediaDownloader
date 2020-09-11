package org.wycliffeassociates.rcmediadownloader

import java.io.File
import java.net.MalformedURLException
import java.net.URL
import org.slf4j.LoggerFactory
import org.wycliffeassociates.rcmediadownloader.data.MediaDivision
import org.wycliffeassociates.rcmediadownloader.data.MediaUrlParameter
import org.wycliffeassociates.rcmediadownloader.io.IDownloadClient
import org.wycliffeassociates.resourcecontainer.ResourceContainer

abstract class RCMediaDownloader(
    val urlParams: MediaUrlParameter,
    val downloadClient: IDownloadClient
) {
    val logger = LoggerFactory.getLogger(javaClass)

    abstract fun downloadMedia(url: String, rc: ResourceContainer): String

    fun templatePathInRC(fileName: String, mediaDivision: MediaDivision): String {
        return when (mediaDivision) {
            MediaDivision.CHAPTER -> "$MEDIA_DIR/${urlParams.projectId}/chapters/$fileName"
            else -> "$MEDIA_DIR/${urlParams.projectId}/$fileName"
        }
    }

    private fun execute(rc: ResourceContainer): File {
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == urlParams.projectId
        } ?: return rc.file

        for (mediaType in urlParams.mediaTypes) {
            // filter mediaType to download
            val media = mediaProject.media.firstOrNull {
                it.identifier == mediaType.name.toLowerCase()
            }

            if (media != null) {
                when (urlParams.mediaDivision) {
                    MediaDivision.CHAPTER -> {
                        val url = media.chapterUrl
                        if (validateUrl(url)) {
                            media.chapterUrl = downloadMedia(url, rc)
                        }
                    }
                    else -> {
                        val url = media.url
                        if (validateUrl(url)) {
                            media.url = downloadMedia(url, rc)
                        }
                    }
                }
            }
        }

        rc.writeMedia()
        return rc.file
    }

    private fun validateUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: MalformedURLException) {
            logger.error(
                "${e.message}\nThe following media url is not valid for download: $url"
            )
            false
        }
    }

    companion object {
        const val MEDIA_DIR = "media"

        fun download(
            rcFile: File,
            urlParams: MediaUrlParameter,
            downloadClient: IDownloadClient,
            overwrite: Boolean = false
        ): File {
            val downloader: RCMediaDownloader = when (urlParams.mediaDivision) {
                MediaDivision.CHAPTER -> ChapterMediaDownloader(urlParams, downloadClient)
                else -> BookMediaDownloader(urlParams, downloadClient)
            }

            val rcOutputFile: File = if (overwrite) {
                rcFile
            } else {
                // create a new copy of the original RC file
                val newRCFile = rcFile.parentFile.resolve(rcFile.nameWithoutExtension + "_updated." + rcFile.extension)
                if (newRCFile.exists()) newRCFile.deleteRecursively()
                newRCFile.apply {
                    rcFile.copyRecursively(
                        newRCFile, overwrite = true
                    )
                }
            }

            ResourceContainer.load(rcOutputFile).use { rc ->
                return downloader.execute(rc)
            }
        }
    }
}
