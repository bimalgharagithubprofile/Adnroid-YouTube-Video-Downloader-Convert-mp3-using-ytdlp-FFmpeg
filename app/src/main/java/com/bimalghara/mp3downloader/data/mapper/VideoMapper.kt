package com.bimalghara.mp3downloader.data.mapper

import com.bimalghara.mp3downloader.data.model.VideoInfoDTO
import com.bimalghara.mp3downloader.domain.model.VideoDetails

fun VideoInfoDTO.toDomain() : VideoDetails {

    return VideoDetails(
        title = title,
        likeCount = likeCount,
        viewCount = viewCount,
        thumbnail = thumbnail,
        duration = duration,
        fileSizeApproximate = fileSizeApproximate,
        url = url,
        ext = ext,
    )
}