package com.bimalghara.mp3downloader.common.di

import android.content.Context
import com.bimalghara.mp3downloader.common.dispatcher.DefaultDispatcherProvider
import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.utils.NetworkConnectivityImpl
import com.bimalghara.mp3downloader.utils.NetworkConnectivitySource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by BimalGhara
 */

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideDefaultDispatcher(): DispatcherProviderSource {
        return DefaultDispatcherProvider()
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivity(@ApplicationContext context: Context): NetworkConnectivitySource {
        return NetworkConnectivityImpl(context)
    }
}