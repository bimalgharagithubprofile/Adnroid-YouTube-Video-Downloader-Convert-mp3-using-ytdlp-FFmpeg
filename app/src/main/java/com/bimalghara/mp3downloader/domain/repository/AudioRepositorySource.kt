package com.bimalghara.mp3downloader.domain.repository

import android.content.Context
import android.net.Uri


/**
 * Created by BimalGhara
 */

interface AudioRepositorySource {


    suspend fun requestSaveAudio(appContext: Context, audioPath: String, destinationUri: Uri, callback: (Int) -> Unit)

}