package com.bimalghara.mp3downloader.domain.use_case

import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.ErrorDetails
import com.bimalghara.mp3downloader.domain.repository.ErrorDetailsSource
import kotlinx.coroutines.withContext

/**
 * Created by BimalGhara
 */

class GetErrorDetailsUseCase(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val errorDetailsSource: ErrorDetailsSource
    ) {

    suspend operator fun invoke(cause: String): ErrorDetails = withContext(dispatcherProviderSource.io){
        errorDetailsSource.getErrorDetails(cause)
    }
}