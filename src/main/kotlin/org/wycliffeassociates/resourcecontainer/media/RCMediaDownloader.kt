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
    val rcOutputFile: File = if (overwrite) {
        rcFile
    } else {
        // create a new copy next to the original RC file
        rcFile.copyTo(
            rcFile.parentFile.resolve(rcFile.nameWithoutExtension + "_updated." + rcFile.extension),
            overwrite = true
        )
    }
    val rc = ResourceContainer.load(rcOutputFile)

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

    abstract fun execute(): File

    abstract fun downloadMedia(url: String): String

    fun templatePathInRC(fileName: String, mediaDivision: MediaDivision): String {
        return when (mediaDivision) {
            MediaDivision.CHAPTER -> "$MEDIA_DIR/${urlParams.projectId}/chapters/$fileName"
            else -> "$MEDIA_DIR/${urlParams.projectId}/$fileName"
        }
    }
}
