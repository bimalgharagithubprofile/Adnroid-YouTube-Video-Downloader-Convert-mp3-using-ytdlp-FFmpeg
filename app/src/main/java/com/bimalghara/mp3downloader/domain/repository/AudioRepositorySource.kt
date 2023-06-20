package com.bimalghara.mp3downloader.domain.repository

import android.content.Context


/**
 * Created by BimalGhara
 */

interface AudioRepositorySource {


    suspend fun requestSaveAudio(appContext: Context, audioPath: String, destinationPath: String, callback: (Int) -> Unit)

}