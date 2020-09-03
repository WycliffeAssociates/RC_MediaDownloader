package org.wycliffeassociates.resourcecontainer.media

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaType
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import java.io.File

fun main() {
    val args: Array<String> = arrayOf(
        "-rc",
        "/path/to/rc/file",
        "-pi",
        "tit",
        "-md",
        "chapter",
        "-mt",
        "[wav,mp3]",
        "-o"
    )
    val params = parseParams(args)

    val rcPath = params["rc"] ?: ""
    val projectId = params["pi"] ?: ""
    val mediaDivision = params["md"] ?: ""
    val mediaTypes = params["mt"] ?: ""
    val overwrite = params["o"] != null

    val rcFile = File(rcPath)
    val division = MediaDivision.get(mediaDivision)!!
    val mediaTypeList = mediaTypes.trim('[', ']').split(',').map {
        MediaType.get(it)!!
    }
    // validate args...

    val urlParameter = MediaUrlParameter(projectId, division, mediaTypeList)
    RCMediaDownloader.download(rcFile, urlParameter, overwrite)
}

fun parseParams(args: Array<String>): Map<String, String> {
    val params: MutableMap<String, String> = mutableMapOf()
    var param: String? = null
    for (a in args) {
        when {
            a[0] == '-' -> {
                if (a.length < 2) {
                    System.err.println("Error at argument $a")
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
