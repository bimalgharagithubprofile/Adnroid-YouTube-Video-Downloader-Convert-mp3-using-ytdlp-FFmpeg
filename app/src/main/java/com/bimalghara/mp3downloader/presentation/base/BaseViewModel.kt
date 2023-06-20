package com.bimalghara.mp3downloader.presentation.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.domain.use_case.GetErrorDetailsUseCase
import com.bimalghara.mp3downloader.utils.SingleEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by BimalGhara
 */
abstract class BaseViewModel(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val errorDetailsUseCase: GetErrorDetailsUseCase,
) : ViewModel(){
    private val logTag = javaClass.simpleName

    private val _errorSingleEvent = MutableLiveData<SingleEvent<Any>>()
    val errorSingleEvent: LiveData<SingleEvent<Any>> get() = _errorSingleEvent


    fun showError(errorDetails: CustomException?) = viewModelScope.launch {
        errorDetails?.message?.let {
            Log.e(logTag, "showing error: $it")
            val error = errorDetailsUseCase(it)
            _errorSingleEvent.value = SingleEvent(error.description)
        }
    }

    suspend fun getError(errorCode: String): String  = withContext(dispatcherProviderSource.io){
        return@withContext errorDetailsUseCase(errorCode).description
    }


}