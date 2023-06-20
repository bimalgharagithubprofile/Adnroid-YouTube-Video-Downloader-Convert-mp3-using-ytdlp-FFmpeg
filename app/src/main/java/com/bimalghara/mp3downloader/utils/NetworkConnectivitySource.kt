package com.bimalghara.mp3downloader.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by BimalGhara
 */

class NetworkConnectivityImpl @Inject constructor(val context: Context) : NetworkConnectivitySource {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override suspend fun getStatus(ioDispatcher: CoroutineContext): NetworkConnectivitySource.Status = withContext(ioDispatcher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return@withContext NetworkConnectivitySource.Status.Unavailable
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return@withContext NetworkConnectivitySource.Status.Unavailable
            return@withContext when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkConnectivitySource.Status.Available
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkConnectivitySource.Status.Available
                else -> NetworkConnectivitySource.Status.Unavailable
            }
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            (return@withContext if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                NetworkConnectivitySource.Status.Available
            } else {
                NetworkConnectivitySource.Status.Unavailable
            })
        }
    }
}

interface NetworkConnectivitySource {
    suspend fun getStatus(ioDispatcher: CoroutineContext): Status

    enum class Status {
        Available, Unavailable
    }
}
