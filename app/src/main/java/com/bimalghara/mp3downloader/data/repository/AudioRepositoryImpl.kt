package com.bimalghara.mp3downloader.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.domain.repository.AudioRepositorySource
import java.io.*
import java.nio.ByteBuffer
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val dispatcherProviderSource: DispatcherProviderSource
) : AudioRepositorySource {
    private val logTag = javaClass.simpleName


    override suspend fun requestSaveAudio(
        appContext: Context,
        audioPath: String,
        destinationUri: Uri,
        callback: (Int) -> Unit
    ) {
        try {
            var destinationAudioRawName = audioPath.split("/").last()
            val fn = destinationAudioRawName.split(".")
            val destinationAudioRawNameWithoutExt = fn[0]

            val targetDocumentFile = DocumentFile.fromTreeUri(appContext, destinationUri)
            Log.e(logTag, "saving targetDocumentFile: ${targetDocumentFile?.uri?.path}")
            val numberOfFileAlreadyExist = targetDocumentFile!!.listFiles().filter { it.name?.startsWith(destinationAudioRawNameWithoutExt) ?: false }.size
            if (numberOfFileAlreadyExist > 0) {
                destinationAudioRawName = "$destinationAudioRawNameWithoutExt ($numberOfFileAlreadyExist).${fn.last()}"
            }

            val newAudioDocumentFile = targetDocumentFile.createFile("audio/${fn.last()}", destinationAudioRawName)
            Log.e(logTag, "saving newAudioDocumentFile: ${newAudioDocumentFile?.uri?.path}")

            val contentResolver: ContentResolver = appContext.contentResolver

            val sourceFile = File(audioPath)
            val inputStream = contentResolver.openInputStream(sourceFile.toUri())
            val totalBytes = sourceFile.length()
            var copiedBytes: Long = 0

            val outputStream = contentResolver.openOutputStream(newAudioDocumentFile!!.uri)

            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int

            var previousProgress = 0
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream!!.write(buffer, 0, bytesRead)
                copiedBytes += bytesRead.toLong()

                val currentProgress = (copiedBytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                if (currentProgress != previousProgress && currentProgress<100) {//100 will be sent at last(check end of this function block - callback(100))
                    Log.e(logTag, "saving progress: $currentProgress")
                    callback(currentProgress)
                    previousProgress = currentProgress
                }
            }

            inputStream.close()
            outputStream!!.close()

            //delete converted audio file as no longer needed
            val audioFile = File(audioPath)
            if (audioFile.exists()) audioFile.delete()
            Log.e(logTag, "saving: converted audio deleted")

            callback(100)

        } catch (e: Exception) {
            throw CustomException(cause = "Unable to save audio file: ${e.localizedMessage}")
        }
    }


}