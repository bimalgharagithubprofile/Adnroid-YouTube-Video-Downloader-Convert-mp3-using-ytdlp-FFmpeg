package com.bimalghara.mp3downloader.utils

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import java.io.File
import javax.annotation.Nullable


object FileUtil {

    val protectedDirectories:MutableList<String> = arrayListOf()

    init {
        protectedDirectories.addAll(
            listOf(
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_DOCUMENTS,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_RINGTONES,
                Environment.MEDIA_BAD_REMOVAL,
                Environment.MEDIA_CHECKING,
                Environment.MEDIA_EJECTING,
                Environment.MEDIA_MOUNTED,
                Environment.MEDIA_MOUNTED_READ_ONLY,
                Environment.MEDIA_NOFS,
                Environment.MEDIA_REMOVED,
                Environment.MEDIA_SHARED,
                Environment.MEDIA_UNKNOWN,
                Environment.MEDIA_UNMOUNTABLE,
                Environment.MEDIA_UNMOUNTED
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            protectedDirectories.addAll(
                listOf(
                    Environment.DIRECTORY_AUDIOBOOKS,
                    Environment.DIRECTORY_SCREENSHOTS,
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            protectedDirectories.addAll(
                listOf(
                    Environment.DIRECTORY_RECORDINGS
                )
            )
        }
    }

}