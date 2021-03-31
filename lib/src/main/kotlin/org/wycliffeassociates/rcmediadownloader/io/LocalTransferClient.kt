package org.wycliffeassociates.rcmediadownloader.io

import java.io.File

class LocalTransferClient : IDownloadClient {
    override fun downloadFromUrl(url: String, outputDir: File): File? {
        val sourceFile = File(System.getenv("CONTENT_ROOT")).resolve(url)
        val outputFile = outputDir.resolve(File(url).name)

        return if (sourceFile.exists()) {
            sourceFile.copyTo(outputFile)
            sourceFile
        } else null
    }
}
