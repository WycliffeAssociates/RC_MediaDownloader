package org.wycliffeassociates.resourcecontainer.media.cli
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.wycliffeassociates.resourcecontainer.media.RCMediaDownloader
import org.wycliffeassociates.resourcecontainer.media.data.MediaDivision
import org.wycliffeassociates.resourcecontainer.media.data.MediaType
import org.wycliffeassociates.resourcecontainer.media.data.MediaUrlParameter
import org.wycliffeassociates.resourcecontainer.media.io.DownloadClient
import java.io.File

class CIApplication : CliktCommand() {
    private val rcPath by option(
        "-rc", "--resoucecontainer",
        help = "Path to resource container"
    ).default("")

    private val projectId by option(
        "-pid", "--projectid",
        help = "Project identifier"
    ).default("")

    private val mediaDivision by option(
        "-md", "--mediadivision",
        help = "Media division. Example: book"
    ).default("")

    private val overwrite: Boolean by option(
        "-o", "--overwrite",
        help = "Overwrite the original resource container."
    ).flag()

    private val mediaTypes by option(
        "-mt", "--mediatypes",
        help = "List of media types to download, separated by commas ','. Example: wav,png"
    ).default("")

    override fun run() {
        execute()
    }

    private fun execute() {
        val rcFile = File(rcPath)
        val division = MediaDivision.get(mediaDivision)
        val mediaTypeList = mediaTypes.split(',').mapNotNull {
            MediaType.get(it)
        }

        return when {
            // validate args...
            !rcFile.exists() -> System.err.println("Resource Container not found at ${rcFile.absolutePath}")
            projectId.isEmpty() -> System.err.println("Invalid projectId")
            division == null -> System.err.println("Invalid media division: $mediaDivision")
            !mediaTypeList.any() -> System.err.println("Invalid media type(s)")
            else -> {
                val urlParameter = MediaUrlParameter(projectId, division, mediaTypeList)
                val resultFile = RCMediaDownloader.download(rcFile, urlParameter, DownloadClient(), overwrite)
                println("Process completed! Check your file at $resultFile.")
            }
        }
    }
}
