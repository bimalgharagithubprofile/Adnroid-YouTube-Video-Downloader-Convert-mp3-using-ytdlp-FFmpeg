package com.bimalghara.mp3downloader.data.network.retrofit


import com.bimalghara.mp3downloader.utils.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by BimalGhara
 */

private const val timeoutRead = 30   //In seconds
private const val timeoutConnect = 30   //In seconds

@Singleton
class ApiServiceGenerator @Inject constructor() {
    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    private val retrofit: Retrofit

    init {
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
        val client = okHttpBuilder.build()
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL).client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    fun <S> createApiService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }
}
