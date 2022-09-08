package com.example.passcheat

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.startForegroundService(
            applicationContext,
            Services.newIntent(applicationContext)
        )

        finish()
    }
}