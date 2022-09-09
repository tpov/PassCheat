package com.example.passcheat

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Delay

class MainActivity : Activity() {
    lateinit var receiver: BootUpReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = BootUpReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(receiver, filter);

        checkPermission()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermission() {
        if (!Settings.canDrawOverlays(this)) {
            getOverlayPermission()
            toastAddAccessApp()
            Thread.sleep(5000) //
            finish()
        } else {
            startService()
            finish()
        }
    }

    private fun toastAddAccessApp() {
        Toast.makeText(this, R.string.access_application, Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startService()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(receiver);
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService() {
        this.startForegroundService(Intent(this, Services::class.java))
    }

    companion object {

        private const val MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 991
    }
}