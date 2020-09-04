package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaType
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import java.io.File

fun main(args: Array<String>) {
    val params = parseParams(args)

    val rcPath = params["rc"] ?: ""
    val projectId = params["pi"] ?: ""
    val mediaDivision = params["md"] ?: ""
    val mediaTypes = params["mt"] ?: ""
    val overwrite = params["o"] != null

    val rcFile = File(rcPath)
    val division = MediaDivision.get(mediaDivision)
    val mediaTypeList = mediaTypes.trim('[', ']').split(',').map {
        MediaType.get(it)
    }.filterNotNull()

    // validate args...
    when {
        !rcFile.exists() -> System.err.println("Resource Container file not found at ${rcFile.absolutePath}")
        projectId.isEmpty() -> System.err.println("Invalid projectId")
        division == null -> System.err.println("Invalid media division: $mediaDivision")
        !mediaTypeList.any() -> System.err.println("Invalid media type(s)")
        else -> {
            val urlParameter = MediaUrlParameter(projectId, division, mediaTypeList)
            val resultFile = RCMediaDownloader.download(rcFile, urlParameter, overwrite)
            println("Process completed! Check your file at $resultFile.")
        }
    }
}

fun parseParams(args: Array<String>): Map<String, String> {
    val params: MutableMap<String, String> = mutableMapOf()
    var param: String? = null
    for (a in args) {
        when {
            a[0] == '-' -> {
                if (a.length < 2) {
                    System.err.println("Invalid syntax argument: $a")
                    return mapOf()
                }
                param = a.substring(1)
                params[param] = ""
            }
            params[param] == "" -> params[param!!] = a
            else -> {
                System.err.println("Illegal parameter usage")
                return mapOf()
            }
        }
    }
    return params
}
