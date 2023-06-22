package com.bimalghara.mp3downloader.presentation.home

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.error.ERROR_NO_INTERNET_CONNECTION
import com.bimalghara.mp3downloader.domain.model.VideoDetails
import com.bimalghara.mp3downloader.domain.use_case.GetErrorDetailsUseCase
import com.bimalghara.mp3downloader.domain.use_case.RequestVideoInfoFromNetworkUseCase
import com.bimalghara.mp3downloader.presentation.base.BaseViewModel
import com.bimalghara.mp3downloader.utils.NetworkConnectivitySource
import com.bimalghara.mp3downloader.utils.ResourceWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by BimalGhara
 */

@HiltViewModel
class HomeViewModel @Inject constructor(
    val dispatcherProviderSource: DispatcherProviderSource,
    private val networkConnectivitySource: NetworkConnectivitySource,
    errorDetailsUseCase: GetErrorDetailsUseCase,
    private val requestVideoInfoFromNetworkUseCase: RequestVideoInfoFromNetworkUseCase
) : BaseViewModel(dispatcherProviderSource, errorDetailsUseCase) {
    private val logTag = javaClass.simpleName

    private val _selectedPathLiveData = MutableLiveData<DocumentFile?>(null)
    val selectedPathLiveData: LiveData<DocumentFile?> get() = _selectedPathLiveData

    private var _videoInfoJob: Job? = null
    private val _videoDetailsLiveData = MutableLiveData<ResourceWrapper<VideoDetails>>()
    val videoDetailsLiveData: LiveData<ResourceWrapper<VideoDetails>> get() = _videoDetailsLiveData



    fun setSelectedPath(documentFile: DocumentFile?) {
        _selectedPathLiveData.value = documentFile
    }

    private suspend fun getNetworkStatus(): NetworkConnectivitySource.Status {
        val result = networkConnectivitySource.getStatus(dispatcherProviderSource.io)
        Log.i(logTag, "network status: $result")
        return result
    }



    fun grabVideoInfo(appContext: Context, url: Editable?) = viewModelScope.launch {
        val networkStatus = async { getNetworkStatus() }.await()

        if (networkStatus != NetworkConnectivitySource.Status.Available) {
            showError(CustomException(cause = ERROR_NO_INTERNET_CONNECTION))//just to notify user about no-internet
        } else {
            requestVideoDataFromCloud(appContext, url)
        }
    }



    /*
    * grab video info from network
    */
    private fun requestVideoDataFromCloud(appContext: Context, url: Editable?) {
        _videoInfoJob?.cancel()//to prevent creating duplicate flow, fun is called multiple times
        _videoInfoJob = requestVideoInfoFromNetworkUseCase(appContext ,url, selectedPathLiveData.value?.uri?.path).onEach {
            _videoDetailsLiveData.value = it
            when (it) {
                is ResourceWrapper.Error -> showError(it.error)
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }
}