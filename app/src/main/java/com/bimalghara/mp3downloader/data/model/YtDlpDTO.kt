package com.bimalghara.mp3downloader.data.model

class YtDlpDTO(
    val command: List<String?>,
    val exitCode: Int,
    val elapsedTime: Long,
    val out: String,
    val err: String
)