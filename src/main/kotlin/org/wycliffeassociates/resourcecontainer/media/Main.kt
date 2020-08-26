package org.wycliffeassociates.resourcecontainer.media

import java.io.File

fun main() {
    val rcFile = File("E:/miscs/rc/pentateuch.zip")
    val parameter =  MediaUrlParameter(
        projectId = "gen",
        mediaTypes = listOf(MediaType.WAV)
    )
    MediaDownloader(rcFile).download(parameter)
}