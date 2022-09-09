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
        Log.d("PassCheat", "Broadcast")
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.d("PassCheat", "Broadcast: ACTION_SCREEN_ON")
                val i = Intent(context, Services::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }
        }
    }
}