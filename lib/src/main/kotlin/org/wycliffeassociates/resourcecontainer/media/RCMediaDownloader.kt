package org.wycliffeassociates.resourcecontainer.media

import java.io.File
import java.net.MalformedURLException
import java.net.URL
import org.slf4j.LoggerFactory
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.IDownloadClient

abstract class RCMediaDownloader(
    rcFile: File,
    overwrite: Boolean,
    val urlParams: MediaUrlParameter,
    val downloadClient: IDownloadClient
) {
    private val rcOutputFile: File = if (overwrite) {
        rcFile
    } else {
        // create a new copy of the original RC file
        val destFile = rcFile.parentFile.resolve(rcFile.nameWithoutExtension + "_updated." + rcFile.extension)
        destFile.apply {
            rcFile.copyRecursively(
                destFile, overwrite = true
            )
        }
    }
    val rc = ResourceContainer.load(rcOutputFile)
    val logger = LoggerFactory.getLogger(javaClass)

    abstract fun downloadMedia(url: String): String

    fun templatePathInRC(fileName: String, mediaDivision: MediaDivision): String {
        return when (mediaDivision) {
            MediaDivision.CHAPTER -> "$MEDIA_DIR/${urlParams.projectId}/chapters/$fileName"
            else -> "$MEDIA_DIR/${urlParams.projectId}/$fileName"
        }
    }

    private fun execute(): File {
        val mediaProject = rc.media?.projects?.firstOrNull {
            it.identifier == urlParams.projectId
        } ?: return rcOutputFile

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
                            media.chapterUrl = downloadMedia(url)
                        }
                    }
                    else -> {
                        val url = media.url
                        if (validateUrl(url)) {
                            media.url = downloadMedia(url)
                        }
                    }
                }
            }
        }

        rc.writeMedia()
        return rcOutputFile
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
                MediaDivision.CHAPTER -> ChapterMediaDownloader(rcFile, overwrite, urlParams, downloadClient)
                else -> BookMediaDownloader(rcFile, overwrite, urlParams, downloadClient)
            }

            return downloader.execute()
        }
    }
}
