package com.bimalghara.mp3downloader.data.network

import okhttp3.ResponseBody

interface DownloadCallback {
    fun onDataReceive(responseBody: ResponseBody, callback: DownloadCallback)
    fun onProgressUpdateProgress(progress: Int, combineSize: String)
    fun onDownloadComplete(filePath: String, combineSize: String)
    fun onDownloadFailed(errorMessage: String)
}