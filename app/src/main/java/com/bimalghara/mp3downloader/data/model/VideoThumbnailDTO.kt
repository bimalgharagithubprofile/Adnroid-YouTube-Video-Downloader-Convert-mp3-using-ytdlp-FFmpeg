package com.bimalghara.mp3downloader.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class VideoThumbnailDTO {
    val url: String? = null
    val id: String? = null
}