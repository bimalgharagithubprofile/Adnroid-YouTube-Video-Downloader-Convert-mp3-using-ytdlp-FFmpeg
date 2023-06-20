package com.bimalghara.mp3downloader.data.repository

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.mapper.toDomain
import com.bimalghara.mp3downloader.data.model.VideoInfoDTO
import com.bimalghara.mp3downloader.data.network.DownloadCallback
import com.bimalghara.mp3downloader.data.network.RemoteDataSource
import com.bimalghara.mp3downloader.data.network.ytdlp.YTDLP
import com.bimalghara.mp3downloader.domain.model.VideoDetails
import com.bimalghara.mp3downloader.domain.repository.VideoRepositorySource
import com.bimalghara.mp3downloader.utils.*
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


/**
 * Created by BimalGhara
 */

class VideoRepositoryImpl @Inject constructor(
    private val dispatcherProviderSource: DispatcherProviderSource,
    private val remoteDataSource: RemoteDataSource
) : VideoRepositorySource {
    private val logTag = javaClass.simpleName

    override suspend fun requestVideoInfoFromNetwork(
        appContext: Context,
        url: String
    ): VideoDetails {

        val callback: Function3<Float, Long, String, Unit> =
            { progress: Float, o2: Long?, line: String? ->
                Log.e(logTag, "callback: $progress, $o2, $line")
            }

        return try {
            val result = YTDLP.execute(appContext, url, callback)

            //convert raw response to DTO
            val videoInfoDTO = ObjectMapper().readValue(result.out, VideoInfoDTO::class.java)

            //convert DTO to Model
            val converted = videoInfoDTO.toDomain()

            converted

        } catch (e: CustomException) {
            throw e
        } catch (ex: Exception) {
            throw CustomException(cause = "Unable to parse video information: ${ex.localizedMessage}")
        }
    }

    override suspend fun requestDownloadVideoFromNetwork(
        appContext: Context,
        url: String,
        ext: String,
        callback: (Triple<Int, String?, String>) -> Unit
    ) {
        Log.e(logTag, "start downloading: $url")

        val videoRawName = "${System.currentTimeMillis()}.${ext}"

        val baseDir = File(appContext.noBackupFilesDir, DIRECTORY_BASE)
        if (!baseDir.exists()) baseDir.mkdir()
        val videoDir = File(baseDir, VIDEO_DIRECTORY)
        if (!videoDir.exists()) videoDir.mkdir()
        val videoFile = File(videoDir, videoRawName)
        if (!videoFile.exists()) videoFile.createNewFile()

        remoteDataSource.requestDownload(url, object : DownloadCallback {
            override fun onDataReceive(responseBody: ResponseBody, callback: DownloadCallback) {
                writeResponseBodyToDisk(responseBody, videoFile.absolutePath, callback)
            }

            override fun onProgressUpdateProgress(progress: Int, combineSize: String) {
                callback(Triple(progress, null, combineSize))
            }

            override fun onDownloadComplete(filePath: String, combineSize: String) {
                callback(Triple(100, filePath, combineSize))
            }

            override fun onDownloadFailed(errorMessage: String) {
                Log.e(logTag, "callback onDownloadFailed : $errorMessage")
                throw CustomException(cause = errorMessage)
            }
        })

    }

    private fun writeResponseBodyToDisk(
        body: ResponseBody,
        destinationPath: String,
        callback: DownloadCallback
    ) {
        try {
            val file = File(destinationPath)
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead: Long = 0
            val totalBytes = body.contentLength()

            var previousProgress = 0
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (totalBytes > 0) {
                    val progress = (totalBytesRead * 100 / totalBytes).toInt()
                    if (progress != previousProgress) {
                        if (progress < 100)//100 will be sent at last(check end of this function block - callback.onDownloadComplete)
                            callback.onProgressUpdateProgress(progress, "$totalBytesRead|$totalBytes")
                        previousProgress = progress
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            callback.onDownloadComplete(destinationPath, "$totalBytesRead|$totalBytes")
        } catch (e: IOException) {
            callback.onDownloadFailed("Failed to write the video file to disk: ${e.message}")
        }
    }


    override suspend fun requestConvertVideo(
        appContext: Context,
        videoPath: String,
        videoTitle: String,
        videoDurationSeconds: Int,
        callback: (Triple<Int, String?, String>) -> Unit
    ) {
        Log.e(logTag, "start converting: path=>$videoPath | duration=>$videoDurationSeconds")

        val audioRawName = "$videoTitle.mp3"

        val baseDir = File(appContext.noBackupFilesDir, DIRECTORY_BASE)
        if (!baseDir.exists()) baseDir.mkdir()
        val audioDir = File(baseDir, AUDIO_DIRECTORY)
        if (!audioDir.exists()) audioDir.mkdir()
        val audioFile = File(audioDir, audioRawName)


        val videoDisplayTime  = getDisplayTimeValue(videoDurationSeconds)
        var previousProgress = 0
        Config.enableLogCallback {
            val currentTimeValues = getTimeValuesFromString(it.text)
            val currentDurationSeconds = currentTimeValues.first * 3600 + currentTimeValues.second * 60 + currentTimeValues.third
            Log.e(logTag, "convert currentDurationSeconds: $currentDurationSeconds")
            if(currentDurationSeconds > 0) {
                val currentProgress = ((currentDurationSeconds / videoDurationSeconds) * 100).toInt()
                if (currentProgress != previousProgress && currentProgress<100) {//100 will be sent at last(check execute callback block - (returnCode == Config.RETURN_CODE_SUCCESS)
                    Log.e(logTag, "convert progress: $currentProgress")

                    val currentDisplayTime = getDisplayTimeValue(currentDurationSeconds.toInt())

                    callback(Triple(currentProgress, null, "$currentDisplayTime|$videoDisplayTime"))
                    previousProgress = currentProgress
                }
            }
        }

        val command = arrayOf("-i", videoPath, "-b:a", "${CONVERTING_BITRATE}K", "-vn", "-acodec", "libmp3lame", "-y", audioFile.absolutePath)
        FFmpeg.executeAsync(command) { executionId, returnCode ->

            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                Log.e(logTag, "convert success")

                //delete video file as no longer needed
                val videoFile = File(videoPath)
                if (videoFile.exists()) videoFile.delete()
                Log.e(logTag, "convert video deleted")

                callback(Triple(100, audioFile.absolutePath, "$videoDisplayTime|$videoDisplayTime"))

            } else {
                Log.e(logTag, "convert failed")
                throw CustomException(cause = "Converting file is interrupted! [FFmpeg.executeAsync]")
            }
        }


    }

}

