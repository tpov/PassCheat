package com.example.passcheat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class BootUpReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PassCheat", "Broadcast: $intent")
        val i = Intent(context, Services::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startForegroundService(i)
    }
}