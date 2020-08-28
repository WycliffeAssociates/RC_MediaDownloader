package org.wycliffeassociates.resourcecontainer.media

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileDownloadClient {
    @Streaming
    @GET
    fun downloadFileStream(@Url url: String): Call<ResponseBody>
}