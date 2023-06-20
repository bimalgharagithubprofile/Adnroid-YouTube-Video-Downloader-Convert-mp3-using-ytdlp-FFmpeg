package com.example.file_downloader

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.Looper
import com.example.file_downloader.interfaces.VideoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.awaitResponse
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class DownloadManager(private val videoService: VideoService) {

    private val TOTAL_PERCENT = 100

    interface DownloadCallback {
        fun onDownloadStarted()
        fun onProgressUpdate(progress: Int)
        fun onDownloadComplete(filePath: String)
        fun onDownloadFailed(errorMessage: String)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun downloadVideo(url: String, destinationPath: String, callback: DownloadCallback) {
        coroutineScope.launch {
            val call = videoService.downloadVideo(url)
            withContext(Dispatchers.Main) {
                callback.onDownloadStarted()
            }
            try {
                val response = call.awaitResponse()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        writeResponseBodyToDisk(responseBody, destinationPath, callback)
                    } else {
                        withContext(Dispatchers.Main) {
                            callback.onDownloadFailed("Empty response body.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback.onDownloadFailed("Download failed with response code: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onDownloadFailed("Download failed: ${e.message}")
                }
            }
        }
    }

    private suspend fun writeResponseBodyToDisk(
        body: ResponseBody,
        destinationPath: String,
        callback: DownloadCallback
    ) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(destinationPath)
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                val totalBytes = body.contentLength()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (totalBytes > 0) {
                        val progress = (totalBytesRead * TOTAL_PERCENT / totalBytes).toInt()
                        withContext(Dispatchers.Main) {
                            callback.onProgressUpdate(progress)
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    callback.onDownloadComplete(destinationPath)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    callback.onDownloadFailed("Failed to write the video file to disk: ${e.message}")
                }
            }
        }
    }
}

