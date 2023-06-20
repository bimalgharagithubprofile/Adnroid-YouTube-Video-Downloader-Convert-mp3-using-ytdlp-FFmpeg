package com.example.file_downloader

import android.media.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.nio.ByteBuffer

class AudioExtractor {
    interface ConversionProgressListener {
        fun onProgressUpdated(progress: Int)
    }

    fun genVideoUsingMuxer(
        srcPath: String?,
        dstPath: String?,
        startMs: Int,
        endMs: Int,
        useAudio: Boolean,
        useVideo: Boolean,
        progressListener: ConversionProgressListener?
    ) {
        // Set up MediaExtractor to read from the source.
        val extractor = MediaExtractor()
        extractor.setDataSource(srcPath!!)
        val trackCount = extractor.trackCount
        // Set up MediaMuxer for the destination.
        val muxer: MediaMuxer
        muxer = MediaMuxer(dstPath!!, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        val indexMap = HashMap<Int, Int>(trackCount)
        var bufferSize = -1
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            var selectCurrentTrack = false
            if (mime!!.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i)
                val dstIndex = muxer.addTrack(format)
                indexMap[i] = dstIndex
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    bufferSize = Math.max(newSize, bufferSize)
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE
        }
        // Set up the orientation and starting time for extractor.
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(srcPath)
        val degreesString =
            retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        if (degreesString != null) {
            val degrees = degreesString.toInt()
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees)
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        val offset = 0
        var trackIndex = -1
        val dstBuf = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        muxer.start()
        val durationUs = extractor.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION)

        // Create a handler to post progress updates on the main thread
        val handler = Handler(Looper.getMainLooper())
        while (true) {
            bufferInfo.offset = offset
            bufferInfo.size = extractor.readSampleData(dstBuf, offset)
            if (bufferInfo.size < 0) {
                Log.d(TAG, "Saw input EOS.")
                bufferInfo.size = 0
                break
            } else {
                bufferInfo.presentationTimeUs = extractor.sampleTime
                if (endMs > 0 && bufferInfo.presentationTimeUs > endMs * 1000L) {
                    Log.d(TAG, "The current sample is over the trim end time.")
                    break
                } else {
                    bufferInfo.flags = extractor.sampleFlags
                    trackIndex = extractor.sampleTrackIndex
                    muxer.writeSampleData(indexMap[trackIndex]!!, dstBuf, bufferInfo)
                    extractor.advance()

                    // Calculate progress and update the listener on the main thread
                    val progress = (bufferInfo.presentationTimeUs * 100 / durationUs).toInt()
                    handler.post { progressListener?.onProgressUpdated(progress) }
                }
            }
        }
        muxer.stop()
        muxer.release()
    }

    companion object {
        private const val TAG = "AudioExtractorDecoder"
        private const val DEFAULT_BUFFER_SIZE = 1024 * 1024
    }
}