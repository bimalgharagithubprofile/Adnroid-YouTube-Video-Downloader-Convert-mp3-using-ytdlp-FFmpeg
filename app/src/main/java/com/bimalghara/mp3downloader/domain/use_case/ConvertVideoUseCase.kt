package com.bimalghara.mp3downloader.domain.use_case

import android.content.Context
import android.util.Log
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_URL
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_VIDEO_DURATION
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_VIDEO_TITLE
import com.bimalghara.mp3downloader.domain.model.FileProcessState
import com.bimalghara.mp3downloader.domain.repository.VideoRepositorySource
import com.bimalghara.mp3downloader.utils.ResourceWrapper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Created by BimalGhara
 */

class ConvertVideoUseCase(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val videoRepositorySource: VideoRepositorySource
) {
    private val logTag = javaClass.simpleName


    operator fun invoke(appContext: Context, videoPath: String?, videoTitle: String?, videoDuration: Int?): Flow<ResourceWrapper<FileProcessState>> = callbackFlow {
        send(ResourceWrapper.Loading())

        if(videoPath.isNullOrEmpty()) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_URL)))
        } else if(videoTitle.isNullOrEmpty()) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_VIDEO_TITLE)))
        } else if(videoDuration == null || videoDuration <= 0) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_VIDEO_DURATION)))
        } else {

            val regex = Regex("([^a-zA-Z]|\\s)+")
            val title = regex.replace(videoTitle, "")
            Log.e(logTag, "videoTitle : $videoTitle <> $title")

            try {
                videoRepositorySource.requestConvertVideo(appContext, videoPath, title, videoDuration) {
                    Log.e(logTag, "requestConvertVideo callback : ${it.first}")

                    val sizeArray = it.third.split("|")

                    val data = FileProcessState(
                        progress = it.first,
                        filePath = it.second,
                        currentDuration = sizeArray[0],
                        totalDuration = sizeArray[1]
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