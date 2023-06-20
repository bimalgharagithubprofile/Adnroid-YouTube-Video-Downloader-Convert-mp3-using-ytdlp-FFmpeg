package com.bimalghara.mp3downloader.common.dispatcher

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Created by BimalGhara
 */

interface DispatcherProviderSource {
    val main: CoroutineContext
        get() = Dispatchers.Main
    val default: CoroutineContext
        get() = Dispatchers.Default
    val io: CoroutineContext
        get() = Dispatchers.IO
    val unconfined: CoroutineContext
        get() = Dispatchers.Unconfined
}