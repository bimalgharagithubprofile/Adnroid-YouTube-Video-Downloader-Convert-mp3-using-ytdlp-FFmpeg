package com.bimalghara.mp3downloader.presentation.splash

import android.content.Intent
import com.bimalghara.mp3downloader.R
import com.bimalghara.mp3downloader.databinding.ActivitySplashBinding
import com.bimalghara.mp3downloader.presentation.MainActivity
import com.bimalghara.mp3downloader.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by BimalGhara
 */

@AndroidEntryPoint
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun initViewBinding() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }



    override fun onPostResume() {
        super.onPostResume()

        navigateToMainScreen()
    }


    private fun navigateToMainScreen() {
        CoroutineScope(Dispatchers.Main).launch {

            val nextScreenIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(nextScreenIntent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
}