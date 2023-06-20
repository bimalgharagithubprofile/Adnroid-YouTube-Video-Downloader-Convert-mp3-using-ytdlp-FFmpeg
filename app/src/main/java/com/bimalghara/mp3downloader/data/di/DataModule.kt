package com.bimalghara.mp3downloader.data.di

import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.data.error.mapper.ErrorMapperImpl
import com.bimalghara.mp3downloader.data.error.mapper.ErrorMapperSource
import com.bimalghara.mp3downloader.data.network.RemoteDataImpl
import com.bimalghara.mp3downloader.data.network.RemoteDataSource
import com.bimalghara.mp3downloader.data.network.retrofit.ApiServiceGenerator
import com.bimalghara.mp3downloader.data.repository.AudioRepositoryImpl
import com.bimalghara.mp3downloader.data.repository.ErrorDetailsImpl
import com.bimalghara.mp3downloader.data.repository.VideoRepositoryImpl
import com.bimalghara.mp3downloader.domain.repository.AudioRepositorySource
import com.bimalghara.mp3downloader.domain.repository.ErrorDetailsSource
import com.bimalghara.mp3downloader.domain.repository.VideoRepositorySource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by BimalGhara
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModuleErrors {

    //error for the app
    @Binds
    @Singleton
    abstract fun provideErrorMapper(errorMapper: ErrorMapperImpl): ErrorMapperSource
    @Binds
    @Singleton
    abstract fun provideErrorDetails(errorDetails: ErrorDetailsImpl): ErrorDetailsSource

}



@InstallIn(SingletonComponent::class)
@Module
class DataModuleDataSources {

    @Provides
    @Singleton
    fun provideVideoRepository(dispatcherProviderSource: DispatcherProviderSource, remoteDataSource: RemoteDataSource): VideoRepositorySource {
        return VideoRepositoryImpl(dispatcherProviderSource = dispatcherProviderSource, remoteDataSource = remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideAudioRepository(dispatcherProviderSource: DispatcherProviderSource): AudioRepositorySource {
        return AudioRepositoryImpl(dispatcherProviderSource = dispatcherProviderSource)
    }

    @Provides
    @Singleton
    fun provideUsersRemoteData(serviceGenerator: ApiServiceGenerator): RemoteDataSource {
        return RemoteDataImpl(serviceGenerator)
    }

}