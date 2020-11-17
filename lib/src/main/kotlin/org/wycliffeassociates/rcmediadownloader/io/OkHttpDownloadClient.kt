package org.wycliffeassociates.rcmediadownloader.io

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory

class OkHttpDownloadClient : IDownloadClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun downloadFromUrl(url: String, outputDir: File): File? {
        val client = OkHttpClient()
        val urlFile = File(url)
        val outputFile = outputDir.resolve(urlFile.name)

        val request: Request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                if (response.body == null) {
                    logger.error("No response body found. ${response.message}")
                } else {
                    writeTempDownload(response.body!!, outputFile)
                }
            }
        }

        return if (outputFile.isFile) outputFile else null
    }

    private fun writeTempDownload(body: ResponseBody, outputFile: File): File {
        BufferedInputStream(body.byteStream()).use { inputStream ->
            val bytes = inputStream.readBytes()

            FileOutputStream(outputFile).buffered().use { outputStream ->
                outputStream.write(bytes)
            }
        }
        return outputFile
    }
}
