package com.bimalghara.mp3downloader.domain.use_case

import android.content.Context
import android.util.Log
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_AUDIO_PATH
import com.bimalghara.mp3downloader.data.error.ERROR_INVALID_DESTINATION_PATH
import com.bimalghara.mp3downloader.domain.model.FileProcessState
import com.bimalghara.mp3downloader.domain.repository.AudioRepositorySource
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

class SaveAudioUseCase(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val audioRepositorySource: AudioRepositorySource
) {
    private val logTag = javaClass.simpleName


    operator fun invoke(
        appContext: Context,
        audioPath: String?,
        destinationPth: String?
    ): Flow<ResourceWrapper<FileProcessState>> = callbackFlow {
        send(ResourceWrapper.Loading())

        if(audioPath.isNullOrEmpty()) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_AUDIO_PATH)))
        } else if(destinationPth.isNullOrEmpty()) {
            send(ResourceWrapper.Error(CustomException(cause = ERROR_INVALID_DESTINATION_PATH)))
        } else {
            try {
                audioRepositorySource.requestSaveAudio(appContext, audioPath, destinationPth) {
                    Log.e(logTag, "requestSaveAudio callback : $it")

                    val data = FileProcessState(
                        progress = it,
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