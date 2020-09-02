package org.wycliffeassociates.resourcecontainer.media

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FileDownloadClient {
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>
}
