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



    private const val PRIMARY_VOLUME_NAME = "primary"

    @Nullable
    fun getFullPathFromTreeUri(@Nullable treeUri: Uri?, con: Context): String? {
        if (treeUri == null) return null
        var volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri), treeUri, con) ?: return File.separator
        if (volumePath.endsWith(File.separator)) volumePath = volumePath.substring(0, volumePath.length - 1)

        var documentPath = getDocumentPathFromTreeUri(treeUri)
        if (documentPath?.endsWith(File.separator) == true) documentPath = documentPath.substring(0, documentPath.length - 1)
        return if (documentPath!!.isNotEmpty() && documentPath != volumePath) {
            if (documentPath.startsWith(File.separator)) {
                volumePath + documentPath
            } else {
                volumePath + File.separator.toString() + documentPath
            }
        } else volumePath
    }

    private fun getVolumePath(volumeId: String?, treeUri: Uri, context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getVolumePathForAndroid11AndAbove(
            volumeId,
            context
        ) else getVolumePathAndroid10AndBelow(treeUri, context)
    }

    private fun getVolumePathAndroid10AndBelow(_uri: Uri, context: Context): String? {
        try {
            if (DocumentsContract.isTreeUri(_uri)) {
                val documentId = DocumentsContract.getTreeDocumentId(_uri)
                if(documentId.contains(":".toRegex())) {
                    val split = documentId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    val path = split[1]
                    return if ("primary".equals(type, ignoreCase = true)) {
                        Environment.getExternalStorageDirectory().path
                    } else {
                        path
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return null
    }

    @TargetApi(Build.VERSION_CODES.R)
    private fun getVolumePathForAndroid11AndAbove(volumeId: String?, context: Context): String? {
        return try {
            val mStorageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                    ?: return null
            val storageVolumes = mStorageManager.storageVolumes
            for (storageVolume in storageVolumes) {
                val uuid = storageVolume.uuid
                val primary = storageVolume.isPrimary ?: return null
                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME == volumeId) return storageVolume.directory!!.path
                // other volumes?
                if (uuid != null && uuid == volumeId) return storageVolume.directory!!.path
            }
            // not found.
            null
        } catch (ex: java.lang.Exception) {
            null
        }
    }

    private fun getVolumeIdFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split = docId.split(":").toTypedArray()
        return if (split.isNotEmpty()) split[0] else null
    }

    private fun getDocumentPathFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split: Array<String?> = docId.split(":").toTypedArray()
        return if (split.size >= 2 && split[1] != null) split[1] else File.separator
    }
}