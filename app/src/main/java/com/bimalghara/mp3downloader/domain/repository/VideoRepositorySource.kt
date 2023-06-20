package com.bimalghara.mp3downloader.domain.repository

import android.content.Context
import com.bimalghara.mp3downloader.domain.model.VideoDetails


/**
 * Created by BimalGhara
 */

interface VideoRepositorySource {

    suspend fun requestVideoInfoFromNetwork(appContext: Context, url: String): VideoDetails

    suspend fun requestDownloadVideoFromNetwork(appContext: Context, url: String, ext: String, callback: (Triple<Int, String?, String>) -> Unit)

    suspend fun requestConvertVideo(appContext: Context, videoPath: String, videoTitle: String, videoDuration: Int, callback: (Triple<Int, String?, String>) -> Unit)


}