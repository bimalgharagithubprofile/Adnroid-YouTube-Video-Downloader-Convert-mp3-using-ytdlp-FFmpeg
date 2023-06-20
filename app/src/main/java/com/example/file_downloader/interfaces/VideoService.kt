package com.example.file_downloader.interfaces

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface VideoService {

    @Streaming
    @GET
    fun downloadVideo(@Url url: String): Call<ResponseBody>
}
