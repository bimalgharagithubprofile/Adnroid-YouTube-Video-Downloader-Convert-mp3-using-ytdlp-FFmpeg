package com.bimalghara.mp3downloader.domain.repository

import com.bimalghara.mp3downloader.data.error.ErrorDetails

/**
 * Created by BimalGhara
 */

interface ErrorDetailsSource {
    suspend fun getErrorDetails(cause: String): ErrorDetails
}