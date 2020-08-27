package org.wycliffeassociates.resourcecontainer.media

import java.io.File

fun main() {
    val rcFile = File("S:/Misc/en_ulb_pentateuch")
    val parameter =  MediaUrlParameter(
        projectId = "gen",
        mediaTypes = listOf(MediaType.WAV)
    )
    MediaDownloader(rcFile).download(parameter)