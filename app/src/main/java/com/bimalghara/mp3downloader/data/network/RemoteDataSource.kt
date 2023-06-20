package com.bimalghara.mp3downloader.data.network


/**
 * Created by BimalGhara
 */

interface RemoteDataSource {


    suspend fun requestDownload(url: String, callback: DownloadCallback)


}
