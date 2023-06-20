package com.bimalghara.mp3downloader.presentation.process_file

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bimalghara.mp3downloader.R
import com.bimalghara.mp3downloader.databinding.FragmentProcessFileBinding
import com.bimalghara.mp3downloader.domain.model.ActionStateData
import com.bimalghara.mp3downloader.presentation.base.BaseFragment
import com.bimalghara.mp3downloader.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by BimalGhara
 */

@AndroidEntryPoint
class ProcessFileFragment : BaseFragment<FragmentProcessFileBinding>()  {
    private val logTag = javaClass.simpleName

    private val processFileFragmentArgs: ProcessFileFragmentArgs by navArgs()

    private val processFileViewModel: ProcessFileViewModel by viewModels()


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProcessFileBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //persists the data so that we can survive the config change (will not lose the state data)
        processFileFragmentArgs.videoInfo.let {
            processFileViewModel.setVideoInfo(processFileFragmentArgs.videoInfo)
            context?.let {
                processFileViewModel.downloadVideo(it, processFileFragmentArgs.videoInfo.url, processFileFragmentArgs.videoInfo.ext)
            }
        }

        binding.btnStartOver.setOnClickListener {
            findNavController().navigate(
                ProcessFileFragmentDirections.actionProcessFileFragmentToUsersFragment()
            )
        }

        setStateDownloading()
    }

    override fun observeViewModel() {
        observeError(processFileViewModel.errorSingleEvent)

        observe(processFileViewModel.videoDetailsLiveData) { video ->
            Log.d(logTag, "observe videoInfoLiveData | $video")

            video.thumbnail?.let { binding.ivThumbnail.loadImage(it) }

            binding.tvTitle.text = video.title
            binding.tvViews.text = "${video.viewCount?.toLong()?.formatCountsNumber() ?: 0} Views"
            binding.tvLikes.text = "${video.likeCount?.toLong()?.formatCountsNumber() ?: 0} Likes"
        }

        observe(processFileViewModel.videoDownloadLiveData) { actionStateData ->
            when(actionStateData) {
                is ResourceWrapper.Loading -> {
                    setStateDownloading()
                    binding.progressIndicator.setProgressCompat(0, true)
                }
                is ResourceWrapper.Success -> {
                    binding.progressIndicator.setProgressCompat(actionStateData.data?.progress ?: 0, true)
                    binding.tvActionSize.text = "${actionStateData.data?.currentSize}MB / ${actionStateData.data?.totalSize}MB"

                    if(actionStateData.data?.progress == 100){//download completed
                        binding.progressIndicator.setProgressCompat(0, false)
                        binding.tvActionSize.text = ""
                        binding.tvActionSize.toGone()
                        context?.let { processFileViewModel.convertVideo(it, actionStateData.data.videoPath) }
                    }
                }
                else -> { setStateFailed(actionStateData.error?.message) }
            }
        }
        observe(processFileViewModel.convertVideoLiveData) { actionStateData ->
            when(actionStateData) {
                is ResourceWrapper.Loading -> {
                    setStateConverting()
                    binding.progressIndicator.setProgressCompat(0, true)
                }
                is ResourceWrapper.Success -> {
                    binding.progressIndicator.setProgressCompat(actionStateData.data?.progress ?: 0, true)
                    binding.tvActionSize.text = "${actionStateData.data?.currentDuration} / ${actionStateData.data?.totalDuration}"

                    if(actionStateData.data?.progress == 100) {//convert completed
                        binding.progressIndicator.setProgressCompat(0, false)
                        binding.tvActionSize.text = ""
                        binding.tvActionSize.toGone()
                        context?.let { processFileViewModel.saveAudio(it, actionStateData.data.audioPath) }
                    }
                }
                else -> { setStateFailed(actionStateData.error?.message) }
            }
        }
        observe(processFileViewModel.saveAudioLiveData) { actionStateData ->
            when(actionStateData) {
                is ResourceWrapper.Loading -> {
                    setStateSaving()
                    binding.progressIndicator.setProgressCompat(0, true)
                }
                is ResourceWrapper.Success -> {
                    binding.progressIndicator.setProgressCompat(actionStateData.data?.progress ?: 0, true)
                    binding.tvActionSize.text = "${actionStateData.data?.progress ?: 0}%"

                    if(actionStateData.data?.progress == 100) {//save completed
                        setStateSuccess()
                    }
                }
                else -> { setStateFailed(actionStateData.error?.message) }
            }
        }

    }


    private fun setStateDownloading() {
        context?.let {
            binding.progressIndicator.setIndicatorColor(ResourcesCompat.getColor(resources, R.color.blue, null))

            binding.tvAction.text = it.getStringFromResource(R.string.action_downloading)
        }
        binding.tvActionSize.toVisible()
        binding.statusMessage.toGone()
        binding.ivStatus.toGone()
        binding.btnStartOver.toGone()
    }

    private fun setStateConverting() {
        context?.let {
            binding.progressIndicator.setIndicatorColor(ResourcesCompat.getColor(resources, R.color.orange, null))

            binding.tvAction.text = it.getStringFromResource(R.string.action_converting)
        }
        binding.tvActionSize.toVisible()
        binding.statusMessage.toGone()
        binding.ivStatus.toGone()
        binding.btnStartOver.toGone()
    }

    private fun setStateSaving() {
        context?.let {
            binding.progressIndicator.setIndicatorColor(ResourcesCompat.getColor(resources, R.color.ogy, null))

            binding.tvAction.text = it.getStringFromResource(R.string.action_saving)
        }
        binding.tvActionSize.toVisible()
        binding.statusMessage.toGone()
        binding.ivStatus.toGone()
        binding.btnStartOver.toGone()
    }

    private fun setStateSuccess() {
        context?.let {
            binding.progressIndicator.setProgressCompat(100, false)
            binding.progressIndicator.setIndicatorColor(ResourcesCompat.getColor(resources, R.color.green, null))

            binding.tvAction.text = it.getStringFromResource(R.string.action_success)
            binding.tvStatusMessage.text = it.getStringFromResource(R.string.message_success)
            binding.ivStatusMessage.loadImage(R.drawable.hint_success)
        }

        binding.tvActionSize.toGone()
        binding.statusMessage.toVisible()
        binding.ivStatus.loadImage(R.drawable.status_success)
        binding.ivStatus.toVisible()
        binding.btnStartOver.toVisible()
    }

    private fun setStateFailed(message: String?) = lifecycleScope.launch {
        context?.let {
            binding.progressIndicator.setProgressCompat(100, false)
            binding.progressIndicator.setIndicatorColor(ResourcesCompat.getColor(resources, R.color.red, null))

            message?.let {error ->
                if(error.startsWith("-"))
                    binding.tvStatusMessage.text = processFileViewModel.getError(error)
                else
                    binding.tvStatusMessage.text = error
            }
            binding.tvAction.text = it.getStringFromResource(R.string.action_failed)
            binding.ivStatusMessage.loadImage(R.drawable.hint_error)
        }

        binding.tvActionSize.toGone()
        if(message.isNullOrEmpty()) binding.statusMessage.toGone() else binding.statusMessage.toVisible()
        binding.ivStatus.loadImage(R.drawable.status_error)
        binding.ivStatus.toVisible()
        binding.btnStartOver.toVisible()
    }

}


