package com.bimalghara.mp3downloader.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoDetails(
    var selectedUri: Uri?, //destination folder selected by user

    var title: String? = null,
    var likeCount: String? = null,
    var viewCount: String? = null,
    var thumbnail: String? = null,
    var duration: Int = 0,
    var fileSizeApproximate: Long = 0,
    var url: String? = null,
    var ext: String? = null
) : Parcelable
