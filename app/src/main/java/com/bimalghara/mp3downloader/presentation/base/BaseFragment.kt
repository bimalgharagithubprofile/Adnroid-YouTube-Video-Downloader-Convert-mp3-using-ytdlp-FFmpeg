package com.bimalghara.mp3downloader.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.bimalghara.mp3downloader.utils.SingleEvent
import com.bimalghara.mp3downloader.utils.showToast
import com.google.android.material.snackbar.Snackbar

/**
 * Created by BimalGhara
 */

abstract class BaseFragment<B: ViewBinding> : Fragment() {
    protected lateinit var binding: B
    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    abstract fun observeViewModel()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getFragmentBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    fun observeError(event: LiveData<SingleEvent<Any>>) {
        binding.root.showToast(viewLifecycleOwner, event, Snackbar.LENGTH_LONG)
    }
}