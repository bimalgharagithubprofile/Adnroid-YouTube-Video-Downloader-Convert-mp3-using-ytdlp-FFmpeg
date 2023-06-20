package com.bimalghara.mp3downloader.data.repository

import android.content.Context
import android.util.Log
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.domain.repository.AudioRepositorySource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val dispatcherProviderSource: DispatcherProviderSource
) : AudioRepositorySource {
    private val logTag = javaClass.simpleName


    override suspend fun requestSaveAudio(
        appContext: Context,
        audioPath: String,
        destinationPath: String,
        callback: (Int) -> Unit
    ) {
        try {
            var destinationAudioRawName = audioPath.split("/").last()
            val fn = destinationAudioRawName.split(".")
            val destinationAudioRawNameWithoutExt = fn[0]

            val targetFolder = File(destinationPath)
            val numberOfFileAlreadyExist = targetFolder.listFiles()?.filter { it.name.startsWith(destinationAudioRawNameWithoutExt) }?.size
            if (numberOfFileAlreadyExist != null && numberOfFileAlreadyExist > 0) {
                destinationAudioRawName = "$destinationAudioRawNameWithoutExt ($numberOfFileAlreadyExist).${fn.last()}"
            }

            val newAudioFile = File(destinationPath, destinationAudioRawName)

            val sourceFile = FileInputStream(audioPath).channel
            val destinationFile = FileOutputStream(newAudioFile).channel

            val fileSize = sourceFile.size()
            var totalBytesCopied = 0L

            val bufferSize = 8 * 1024 // 8KB buffer size
            val buffer = ByteBuffer.allocate(bufferSize)

            while (sourceFile.read(buffer) != -1) {
                buffer.flip()
                destinationFile.write(buffer)
                buffer.clear()

                totalBytesCopied += bufferSize.toLong()
                val progress = ((totalBytesCopied.toDouble() / fileSize.toDouble()) * 100).toInt()
                Log.e(logTag, "saving progress: $progress")
                if (progress < 100)//100 will be sent at last(check end of this function block - callback(100))
                    callback(progress)
            }

            sourceFile.close()
            destinationFile.close()

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