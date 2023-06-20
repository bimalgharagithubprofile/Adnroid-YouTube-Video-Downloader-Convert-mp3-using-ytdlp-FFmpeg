package com.bimalghara.mp3downloader.domain.model



data class FileProcessState(
    var progress: Int = 0,
    var filePath:String? = null,//this will always be a absolutePath
    var currentSize:Long = 0,
    var totalSize:Long = 0,
    var currentDuration:String? = null,
    var totalDuration:String? = null
)
