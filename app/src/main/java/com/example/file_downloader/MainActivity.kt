package com.example.file_downloader

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.file_downloader.AudioExtractor.ConversionProgressListener
import com.example.file_downloader.interfaces.VideoService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private var startForResult: ActivityResultLauncher<Intent>? = null

    private var progressBar: ProgressBar? = null

    private val handler = Handler(Looper.getMainLooper())

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)

        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100)
        }
        else {
            getFileWriteAccess()
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    startFileDownload()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
            getFileWriteAccess()
        }
    }

    private fun getFileWriteAccess() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (!Environment.isExternalStorageManager()) {

                val checkAllFilesAccessIntent = Intent()
                checkAllFilesAccessIntent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                checkAllFilesAccessIntent.data = Uri.fromParts("package", this.packageName, null)
                startForResult?.launch(checkAllFilesAccessIntent)
            }
            else {
                startFileDownload()
            }
        }
        else {
            startFileDownload()
        }
    }

    private fun startFileDownload() {

        val retrofit = Retrofit.Builder()
            .baseUrl("http://commondatastorage.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val videoService = retrofit.create(VideoService::class.java)
        val downloadManager = DownloadManager(videoService)

        val videoUrl = "/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        val destinationDir = File(filesDir.path + "/videos")
        if (!destinationDir.exists()){
            destinationDir.mkdir()
        }

        val destinationFilePath = File(destinationDir.absolutePath + "/" + videoUrl.split("/").last())
        if (!destinationFilePath.exists()){
            destinationFilePath.createNewFile()
        }


        downloadManager.downloadVideo(videoUrl, destinationFilePath.absolutePath, object : DownloadManager.DownloadCallback {

            override fun onDownloadStarted() {
                Log.w(TAG, "onDownloadStarted()")

                handler.post  {
                    // Show the progress bar
                    progressBar?.visibility = View.VISIBLE
                }
            }

            override fun onProgressUpdate(progress: Int) {
                Log.w(TAG, "onProgressUpdate: $progress")

                handler.post  {
                    // Update the progress bar
                    progressBar?.progress = progress
                }
            }

            override fun onDownloadComplete(filePath: String) {
                Log.w(TAG, "onDownloadComplete: $filePath")

                handler.post {
                    // Hide the progress bar
                    progressBar?.visibility = View.GONE
                }

                convertMp4ToM4a(filePath, destinationDir.absolutePath + "/audio.mp3")
            }

            override fun onDownloadFailed(errorMessage: String) {
                Log.w(TAG, "onDownloadFailed: $errorMessage")

                handler.post  {
                    // Hide the progress bar
                    progressBar?.visibility = View.GONE
                }
            }
        })
    }

    fun convertMp4ToM4a(
        inputFilePath: String,
        outputFilePath: String
    ) {

        val conversionProgressListener = object : ConversionProgressListener {
            override fun onProgressUpdated(progress: Int) {
                Log.w(TAG, "convertMp4ToM4a: progress => $progress")
            }
        }

        AudioExtractor().genVideoUsingMuxer(
            inputFilePath,
            outputFilePath,
            -1,
            -1,
            true,
            false,
            conversionProgressListener
        )

    }



    private fun Long.formatNumber(): String {
        val suffixes = listOf("", "K", "M", "B")
        var value = this.toDouble()
        var suffixIndex = 0

        while (value >= 1000 && suffixIndex < suffixes.size - 1) {
            value /= 1000
            suffixIndex++
        }

        val numberFormat = DecimalFormat("#,##0.#")
        val formattedValue = numberFormat.format(value)
        return "$formattedValue${suffixes[suffixIndex]}"
    }

}