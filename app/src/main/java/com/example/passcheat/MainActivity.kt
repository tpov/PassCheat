package com.example.passcheat

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi


class MainActivity : Activity() {
    lateinit var receiver: BootUpReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = BootUpReceiver()

        val filter2 = IntentFilter(Intent.ACTION_BOOT_COMPLETED)
        registerReceiver(receiver, filter2)

        val filter7 = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(receiver, filter7)

        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermission() {
        if (!Settings.canDrawOverlays(this)) {
            getOverlayPermission()
            toastAddAccessApp()
            Thread.sleep(5000) //For toast
            finish()
        } else {
            //startService()
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
               //startService()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(receiver);
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService() {
        if (!isMyServiceRunning(MainActivity::class.java)) this.startForegroundService(Intent(this, Services::class.java))
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    companion object {

        const val MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 991
    }
}