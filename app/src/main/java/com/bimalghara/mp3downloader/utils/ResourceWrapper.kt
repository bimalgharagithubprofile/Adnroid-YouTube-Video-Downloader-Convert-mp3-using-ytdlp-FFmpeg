package com.bimalghara.mp3downloader.utils

import com.bimalghara.mp3downloader.data.error.CustomException

/**
 * Created by BimalGhara
 */
sealed class ResourceWrapper<T>(val data:T?=null, val error: CustomException?=null){

    class Loading<T> : ResourceWrapper<T>()
    class Success<T>(data: T?): ResourceWrapper<T>(data=data)
    class Error<T>(customException: CustomException) : ResourceWrapper<T>(error = customException)

    override fun toString(): String {
        return when (this){
            is Loading -> "Loading"
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[Exception=${error?.message}]"
        }
    }
}
