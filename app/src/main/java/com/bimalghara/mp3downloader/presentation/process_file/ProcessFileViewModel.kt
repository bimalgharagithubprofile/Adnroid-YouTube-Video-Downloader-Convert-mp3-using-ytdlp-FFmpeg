package com.bimalghara.mp3downloader.presentation.process_file

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.error.ERROR_DEFAULT
import com.bimalghara.mp3downloader.data.error.ERROR_NO_INTERNET_CONNECTION
import com.bimalghara.mp3downloader.domain.model.VideoDetails
import com.bimalghara.mp3downloader.domain.use_case.GetErrorDetailsUseCase
import com.bimalghara.mp3downloader.domain.use_case.RequestDownloadVideoFromNetworkUseCase
import com.bimalghara.mp3downloader.presentation.base.BaseViewModel
import com.bimalghara.mp3downloader.domain.model.ActionStateData
import com.bimalghara.mp3downloader.domain.use_case.ConvertVideoUseCase
import com.bimalghara.mp3downloader.domain.use_case.SaveAudioUseCase
import com.bimalghara.mp3downloader.utils.NetworkConnectivitySource
import com.bimalghara.mp3downloader.utils.ResourceWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Created by BimalGhara
 */

@HiltViewModel
class ProcessFileViewModel @Inject constructor(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val networkConnectivitySource: NetworkConnectivitySource,
    errorDetailsUseCase: GetErrorDetailsUseCase,
    private val requestDownloadVideoFromNetworkUseCase: RequestDownloadVideoFromNetworkUseCase,
    private val convertVideoUseCase: ConvertVideoUseCase,
    private val saveAudioUseCase: SaveAudioUseCase
) : BaseViewModel(dispatcherProviderSource, errorDetailsUseCase) {
    private val logTag = javaClass.simpleName

    private val _videoDetailsLiveData = MutableLiveData<VideoDetails>()
    val videoDetailsLiveData: LiveData<VideoDetails> get() = _videoDetailsLiveData

    private var _videoDownloadJob: Job? = null
    private val _videoDownloadLiveData = MutableLiveData<ResourceWrapper<ActionStateData>>()
    val videoDownloadLiveData: LiveData<ResourceWrapper<ActionStateData>> get() = _videoDownloadLiveData

    private var _videoConvertJob: Job? = null
    private val _convertVideoLiveData = MutableLiveData<ResourceWrapper<ActionStateData>>()
    val convertVideoLiveData: LiveData<ResourceWrapper<ActionStateData>> get() = _convertVideoLiveData

    private var _saveAudioJob: Job? = null
    private val _saveAudioLiveData = MutableLiveData<ResourceWrapper<ActionStateData>>()
    val saveAudioLiveData: LiveData<ResourceWrapper<ActionStateData>> get() = _saveAudioLiveData



    fun setVideoInfo(data: VideoDetails) {
        _videoDetailsLiveData.postValue(data)
    }

    private suspend fun getNetworkStatus(): NetworkConnectivitySource.Status {
        val result = networkConnectivitySource.getStatus(dispatcherProviderSource.io)
        Log.i(logTag, "network status: $result")
        return result
    }



    fun downloadVideo(appContext: Context, url: String?, ext: String?) = viewModelScope.launch(dispatcherProviderSource.io) {
        val networkStatus = async { getNetworkStatus() }.await()

        if (networkStatus != NetworkConnectivitySource.Status.Available) {
            showError(CustomException(cause = ERROR_NO_INTERNET_CONNECTION))//just to notify user about no-internet
        } else {
            requestDownloadVideoFromCloud(appContext, url, ext)
        }
    }


    fun convertVideo(appContext: Context, videoPath: String?) = viewModelScope.launch(dispatcherProviderSource.io) {
        _videoConvertJob?.cancel()//to prevent creating duplicate flow, fun is called multiple times
        _videoConvertJob = convertVideoUseCase(appContext, videoPath, videoDetailsLiveData.value?.title, videoDetailsLiveData.value?.duration).onEach {
            when (it) {
                is ResourceWrapper.Loading -> { _convertVideoLiveData.value = ResourceWrapper.Loading() }
                is ResourceWrapper.Success -> {

                    val actionStateData = ActionStateData(
                        progress = it.data?.progress ?: 0,
                        audioPath = it.data?.filePath,
                        currentDuration = it.data?.currentDuration ?: "0",
                        totalDuration = it.data?.totalDuration ?: "0",
                    )
                    _convertVideoLiveData.value = ResourceWrapper.Success(data = actionStateData)

                }
                is ResourceWrapper.Error -> {
                    _convertVideoLiveData.value = ResourceWrapper.Error(it.error ?: CustomException(ERROR_DEFAULT))
                    showError(it.error)
                }
            }
        }.launchIn(viewModelScope)
    }



    fun saveAudio(appContext: Context, audioPath: String?) = viewModelScope.launch(dispatcherProviderSource.io) {
        _saveAudioJob?.cancel()//to prevent creating duplicate flow, fun is called multiple times
        _saveAudioJob = saveAudioUseCase(appContext, audioPath, videoDetailsLiveData.value?.selectedPath).onEach {
            when (it) {
                is ResourceWrapper.Loading -> { _saveAudioLiveData.value = ResourceWrapper.Loading() }
                is ResourceWrapper.Success -> {

                    val actionStateData = ActionStateData(
                        progress = it.data?.progress ?: 0,
                    )
                    _saveAudioLiveData.value = ResourceWrapper.Success(data = actionStateData)

                }
                is ResourceWrapper.Error -> {
                    _saveAudioLiveData.value = ResourceWrapper.Error(it.error ?: CustomException(ERROR_DEFAULT))
                    showError(it.error)
                }
            }
        }.launchIn(viewModelScope)
    }




    /*
    * download video info from network
    */
    private fun requestDownloadVideoFromCloud(appContext: Context, url: String?, ext: String?) {
        _videoDownloadJob?.cancel()//to prevent creating duplicate flow, fun is called multiple times
        _videoDownloadJob = requestDownloadVideoFromNetworkUseCase(appContext ,url, ext).onEach {
            when (it) {
                is ResourceWrapper.Loading -> { _videoDownloadLiveData.value = ResourceWrapper.Loading() }
                is ResourceWrapper.Success -> {

                    val currentSize = (it.data?.currentSize ?: 0) / (1024.0 * 1024.0)
                    val totalSize = (it.data?.totalSize ?: 0) / (1024.0 * 1024.0)

                    val actionStateData = ActionStateData(
                        progress = it.data?.progress ?: 0,
                        videoPath = it.data?.filePath,
                        currentSize = String.format("%.2f", currentSize),
                        totalSize = String.format("%.2f", totalSize)
                    )
                    _videoDownloadLiveData.value = ResourceWrapper.Success(data = actionStateData)

                }
                is ResourceWrapper.Error -> {
                    _videoDownloadLiveData.value = ResourceWrapper.Error(it.error ?: CustomException(ERROR_DEFAULT))
                    showError(it.error)
                }
            }
        }.launchIn(viewModelScope)
    }
}