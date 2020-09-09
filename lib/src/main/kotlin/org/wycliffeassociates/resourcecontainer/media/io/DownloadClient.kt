package org.wycliffeassociates.resourcecontainer.media.io

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

class DownloadClient: IDownloadClient {
    override fun downloadFromUrl(url: String, outputDir: File): File? {
        val urlFile = File(url)
        val outputFile = outputDir.resolve(urlFile.name)

        val retrofitService = Retrofit.Builder()
            .baseUrl(urlFile.parentFile.invariantSeparatorsPath + "/")
            .build()
        val client: RetrofitDownloadClient = retrofitService.create(
            RetrofitDownloadClient::class.java
        )

        val call = client.downloadFile(urlFile.name)
        val response = call.execute()

        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                val logger = LoggerFactory.getLogger(javaClass)
                logger.error("No response body found. ${response.message()}")
            } else {
                writeTempDownload(body, outputFile)
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

private interface RetrofitDownloadClient { // implemented by retrofit service
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>
}

