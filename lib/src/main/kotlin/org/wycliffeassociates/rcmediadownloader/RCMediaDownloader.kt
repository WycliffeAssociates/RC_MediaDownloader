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

    private fun execute(rc: ResourceContainer, projectExclusive: Boolean): File {
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
                        if (true) {
                            var startTime = System.currentTimeMillis()

                            media.chapterUrl = downloadMedia(url, rc)

                            var endTime = System.currentTimeMillis()
                            println("Download time: " + (endTime - startTime) + "ms")
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

        if (projectExclusive) {
            rc.media?.projects = listOf(mediaProject)
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

        /**
         * @param rcFile the resource container to be used for download.
         * @param urlParams defines the information about the requested content.
         * @param singleProject limits the media manifest to contain a maximum of 1 project.
         * @param overwrite whether to create new resource container or overwrite the rcFile.
         */
        fun download(
            rcFile: File,
            urlParams: MediaUrlParameter,
            downloadClient: IDownloadClient,
            singleProject: Boolean = true,
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
                val newRCFile = rcFile.parentFile.resolve(
                    rcFile.nameWithoutExtension + "_updated." + rcFile.extension
                )
                if (newRCFile.exists()) newRCFile.deleteRecursively()
                newRCFile.apply {
                    rcFile.copyRecursively(
                        newRCFile, overwrite = true
                    )
                }
            }

            ResourceContainer.load(rcOutputFile).use { rc ->
                return downloader.execute(rc, projectExclusive = singleProject)
            }
        }
    }
}
