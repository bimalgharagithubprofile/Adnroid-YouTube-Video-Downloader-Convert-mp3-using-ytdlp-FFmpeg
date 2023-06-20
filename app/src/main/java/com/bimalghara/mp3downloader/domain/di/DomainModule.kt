package com.bimalghara.mp3downloader.domain.di

import com.bimalghara.mp3downloader.common.dispatcher.DispatcherProviderSource
import com.bimalghara.mp3downloader.domain.repository.AudioRepositorySource
import com.bimalghara.mp3downloader.domain.repository.VideoRepositorySource
import com.bimalghara.mp3downloader.domain.repository.ErrorDetailsSource
import com.bimalghara.mp3downloader.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Created by BimalGhara
 */

@InstallIn(SingletonComponent::class)
@Module
class DomainModule {

    @Provides
    fun provideGetErrorDetailsUseCase(dispatcherProviderSource: DispatcherProviderSource, errorDetailsSource: ErrorDetailsSource): GetErrorDetailsUseCase{
        return GetErrorDetailsUseCase(dispatcherProviderSource = dispatcherProviderSource, errorDetailsSource = errorDetailsSource)
    }

    @Provides
    fun provideGetVideoInfoFromNetworkUseCase(dispatcherProviderSource: DispatcherProviderSource, videoRepositorySource: VideoRepositorySource): RequestVideoInfoFromNetworkUseCase {
        return RequestVideoInfoFromNetworkUseCase(dispatcherProviderSource = dispatcherProviderSource, videoRepositorySource = videoRepositorySource)
    }
    @Provides
    fun provideDownloadVideoFromNetworkUseCase(dispatcherProviderSource: DispatcherProviderSource, videoRepositorySource: VideoRepositorySource): RequestDownloadVideoFromNetworkUseCase {
        return RequestDownloadVideoFromNetworkUseCase(dispatcherProviderSource = dispatcherProviderSource, videoRepositorySource = videoRepositorySource)
    }
    @Provides
    fun provideConvertVideoUseCase(dispatcherProviderSource: DispatcherProviderSource, videoRepositorySource: VideoRepositorySource): ConvertVideoUseCase {
        return ConvertVideoUseCase(dispatcherProviderSource = dispatcherProviderSource, videoRepositorySource = videoRepositorySource)
    }
    @Provides
    fun provideSaveAudioUseCase(dispatcherProviderSource: DispatcherProviderSource, audioRepositorySource: AudioRepositorySource): SaveAudioUseCase {
        return SaveAudioUseCase(dispatcherProviderSource = dispatcherProviderSource, audioRepositorySource = audioRepositorySource)
    }

}