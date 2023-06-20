package com.bimalghara.mp3downloader.data.error.mapper

import android.content.Context
import com.bimalghara.mp3downloader.R
import com.bimalghara.mp3downloader.data.error.*
import com.bimalghara.mp3downloader.utils.getStringFromResource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ErrorMapperImpl @Inject constructor(@ApplicationContext val context: Context) :
    ErrorMapperSource {



    override fun getErrorByCode(errorCode: String): ErrorDetails {
        return ErrorDetails(code = errorCode, description = errorsMap.getValue(errorCode))
    }

    override val errorsMap: Map<String, String>
        get() = mapOf(
            Pair(ERROR_DEFAULT, context.getStringFromResource(R.string.error_default)),

            Pair(ERROR_NO_INTERNET_CONNECTION, context.getStringFromResource(R.string.no_internet)),
            Pair(ERROR_NETWORK_ERROR, context.getStringFromResource(R.string.network_error)),
            Pair(ERROR_SOCKET_TIMEOUT, context.getStringFromResource(R.string.socket_timeout)),

            Pair(ERROR_NO_PERMISSION, context.getStringFromResource(R.string.error_no_permission)),
            Pair(ERROR_FAILED_ROOT_FOLDER, context.getStringFromResource(R.string.error_failed_to_create_root_folder)),
            Pair(ERROR_SELECT_DIRECTORY_FAILED, context.getStringFromResource(R.string.error_select_directory_failed)),
            Pair(ERROR_PROTECTED_DIRECTORY, context.getStringFromResource(R.string.error_protected_directory)),
            Pair(ERROR_WRITE_PERMISSION, context.getStringFromResource(R.string.error_write_permission)),

            Pair(ERROR_EMPTY_FIELDS, context.getStringFromResource(R.string.error_empty_fields)),
            Pair(ERROR_INVALID_DIRECTORY, context.getStringFromResource(R.string.error_invalid_directory)),

            Pair(ERROR_INVALID_URL, context.getStringFromResource(R.string.error_invalid_url)),
            Pair(ERROR_INVALID_VIDEO_TITLE, context.getStringFromResource(R.string.error_invalid_video_title)),
            Pair(ERROR_INVALID_VIDEO_DURATION, context.getStringFromResource(R.string.error_invalid_video_duration)),
            Pair(ERROR_INVALID_EXTENSION, context.getStringFromResource(R.string.error_invalid_extension)),
            Pair(ERROR_INVALID_AUDIO_PATH, context.getStringFromResource(R.string.error_invalid_audio_path)),
            Pair(ERROR_INVALID_DESTINATION_PATH, context.getStringFromResource(R.string.error_invalid_destination_path)),

        ).withDefault { "Oops! Something went wrong" }

}
