package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.File

class MediaDownloader() {
    companion object {
        fun download(rcFile: File, urlParams: MediaUrlParameter): File {
            val rc = ResourceContainer.load(rcFile)
            val project = urlParams.projectId
            val chapter = urlParams.chapter
            val mediaType = urlParams.mediaTypes[0] // test with 1st type

            val mediaProjects = rc.media?.projects?.filter { it.identifier == project }
            val outputDir = File("./media/")

            // get download url
            mediaProjects?.forEach {
                downloadMedia(it.media)
            }

            return rcFile
        }
        private fun downloadMedia(media: List<Media>) {
            
        }
    }
}