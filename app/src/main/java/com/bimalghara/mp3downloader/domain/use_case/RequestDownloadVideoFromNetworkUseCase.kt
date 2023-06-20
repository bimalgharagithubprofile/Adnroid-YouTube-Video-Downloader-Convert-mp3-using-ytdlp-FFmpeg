package com.bimalghara.mp3downloader.domain.use_case

import android.content.Context
import android.util.Log
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_EXTENSION
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_URL
import com.bimalghara.mp3downloader.domain.model.FileProcessState
import com.bimalghara.mp3downloader.domain.repository.VideoRepositorySource
import com.bimalghara.mp3downloader.utils.ResourceWrapper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by BimalGhara
 */

class RequestDownloadVideoFromNetworkUseCase(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val videoRepositorySource: VideoRepositorySource
) {
    private val logTag = javaClass.simpleName


    operator fun invoke(appContext: Context, url: String?, ext: String?): Flow<ResourceWrapper<FileProcessState>> = callbackFlow {
        send(ResourceWrapper.Loading())

        if(url.isNullOrEmpty() || !url.toString().startsWith("https://")) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_URL)))
        } else if(ext.isNullOrEmpty()) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_EXTENSION)))
        } else {
            try {
                videoRepositorySource.requestDownloadVideoFromNetwork(appContext, url, ext) {
                    Log.e(logTag, "requestDownloadVideoFromNetwork callback : ${it.first}")

                    val sizeArray = it.third.split("|")

                    val data = FileProcessState(
                        progress = it.first,
                        filePath = it.second,
                        currentSize = sizeArray[0].toLong(),
                        totalSize = sizeArray[1].toLong()
                    )
                    launch { send(ResourceWrapper.Success(data = data)) }
                }
            } catch (e: CustomException) {
                send(ResourceWrapper.Error(e))
            }
        }

        awaitClose { }

    }.distinctUntilChanged().flowOn(dispatcherProviderSource.io)




}