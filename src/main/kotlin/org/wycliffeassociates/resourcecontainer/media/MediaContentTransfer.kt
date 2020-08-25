package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File

class MediaContentTransfer() : IMediaContentTransfer {
    override fun pullMediaFiles(rcFile: File, urlParams: MediaUrlParameter): File {
        val rc = ResourceContainer.load(rcFile)
        val project = urlParams.projectId
        val chapter = urlParams.chapter
        val mediaType = urlParams.mediaType
        val mediaProjects = rc.media?.projects?.filter { it.identifier == project }
        val outputDir = File("./media/")
        

        return rcFile
    }
}