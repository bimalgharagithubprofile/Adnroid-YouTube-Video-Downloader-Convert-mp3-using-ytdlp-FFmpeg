package com.bimalghara.mp3downloader.domain.model

data class ActionStateData(
    var progress:Int = 0,
    var videoPath:String? = null, //this will always be a absolutePath
    var audioPath:String? = null, //this will always be a absolutePath
    var currentSize:String = "0B", //size is in MB
    var totalSize:String = "0B", //size is in MB
    var currentDuration:String = "0", //hh:mm:ss
    var totalDuration:String = "0" //hh:mm:ss
)