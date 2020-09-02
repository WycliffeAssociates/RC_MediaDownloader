package org.wycliffeassociates.resourcecontainer.media

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class RetroScratch

fun main() {
    val retrofitService = Retrofit.Builder()
            .baseUrl("https://fetcher-content.bibletranslationtools.org/")
            .build()
    val downloader: FileDownloadClient = retrofitService.create(FileDownloadClient::class.java)

    val call = downloader.downloadFile("en/ulb/tit/1/CONTENTS/mp3/hi/chapter/en_nt_ulb_tit_c01.mp3")

    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                println("success")

                val body = response.body()
                if (body == null) {
                    println("body is null")
                } else {
                    writeResponseBodyToDisk(body)
                }
            }
        }
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            TODO("Not yet implemented")
        }
    })
}

fun writeResponseBodyToDisk(body: ResponseBody) {
    val outputFile = File("/home/dj/Desktop/200MB.zip")

    BufferedInputStream(body.byteStream()).use { inputStream ->
        val bytes = inputStream.readBytes()
        FileOutputStream(outputFile).buffered().use { outputStream ->
            outputStream.write(bytes)
        }
    }

    println("finished")
}
