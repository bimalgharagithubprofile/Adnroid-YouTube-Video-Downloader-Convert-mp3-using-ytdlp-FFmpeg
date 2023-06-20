package com.bimalghara.mp3downloader.data.error.mapper

import com.bimalghara.mp3downloader.data.error.ErrorDetails

interface ErrorMapperSource {
    //fun getErrorString(errorId: Int): String

    fun getErrorByCode(errorCode: String): ErrorDetails

    val errorsMap: Map<String, String>
}
