package org.wycliffeassociates.resourcecontainer.media

import java.io.File
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter

abstract class RCMediaDownloader(
    rcFile: File,
    overwrite: Boolean,
    val urlParams: MediaUrlParameter
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
                    MediaDivision.CHAPTER -> media.chapterUrl = downloadMedia(media.chapterUrl)
                    else -> media.url = downloadMedia(media.url)
                }
            }
        }

        rc.writeMedia()
        return rcOutputFile
    }

    abstract fun downloadMedia(url: String): String

    fun templatePathInRC(fileName: String, mediaDivision: MediaDivision): String {
        return when (mediaDivision) {
            MediaDivision.CHAPTER -> "$MEDIA_DIR/${urlParams.projectId}/chapters/$fileName"
            else -> "$MEDIA_DIR/${urlParams.projectId}/$fileName"
        }
    }

    companion object {
        const val MEDIA_DIR = "media"

        fun download(
            rcFile: File,
            urlParams: MediaUrlParameter,
            overwrite: Boolean = false
        ): File {
            val downloader: RCMediaDownloader = when (urlParams.mediaDivision) {
                MediaDivision.CHAPTER -> ChapterMediaDownloader(rcFile, overwrite, urlParams)
                else -> BookMediaDownloader(rcFile, overwrite, urlParams)
            }

            return downloader.execute()
        }
    }
}
