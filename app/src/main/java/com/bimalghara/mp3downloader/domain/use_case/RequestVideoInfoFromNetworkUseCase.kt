package com.bimalghara.mp3downloader.domain.use_case

import android.content.Context
import android.text.Editable
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.error.ERROR_EMPTY_FIELDS
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_DIRECTORY
import com.bimalghara.mp3downloader.domain.model.VideoDetails
import com.bimalghara.mp3downloader.domain.repository.VideoRepositorySource
import com.bimalghara.mp3downloader.utils.ResourceWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Created by BimalGhara
 */

class RequestVideoInfoFromNetworkUseCase(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val videoRepositorySource: VideoRepositorySource
) {

    operator fun invoke(appContext: Context, url: Editable?, selectedDirectory: String?): Flow<ResourceWrapper<VideoDetails>> = flow {
        emit(ResourceWrapper.Loading())

        if(url.isNullOrEmpty() || !url.toString().startsWith("https://")){
            emit(ResourceWrapper.Error(CustomException(cause = ERROR_EMPTY_FIELDS)))
        } else if(selectedDirectory.isNullOrEmpty()){
            emit(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_DIRECTORY)))
        } else {
            try {
                val videoDetails = videoRepositorySource.requestVideoInfoFromNetwork(appContext, url.toString())
                emit(ResourceWrapper.Success(data = videoDetails))
            } catch (e: CustomException) {
                emit(ResourceWrapper.Error(e))
            }
        }
    }.flowOn(dispatcherProviderSource.io)


}