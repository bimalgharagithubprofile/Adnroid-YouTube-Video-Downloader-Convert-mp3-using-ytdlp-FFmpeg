package com.bimalghara.mp3downloader.data.error

/**
 * Created by BimalGhara
 */

class ErrorDetails(val code: String, val description: String = "") {
    constructor(exception: CustomException) : this(code = ERROR_CUSTOM_EXCEPTION, description = exception.message ?: "")
}

