package com.example.testapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MessengerApp: Application() {

    override fun onCreate() {
        super.onCreate()

        setupMonitoring()
    }

    private fun setupMonitoring() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)

                isAppInForeground = true
                Log.d(TAG, "onResume")
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)

                isAppInForeground = false
                Log.d(TAG, "onPause")
            }
        })
    }

    companion object {
        var isAppInForeground = false
        val TAG: String = MessengerApp::class.java.simpleName
    }
}