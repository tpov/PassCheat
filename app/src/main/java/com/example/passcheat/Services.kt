package com.example.passcheat

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*


@RequiresApi(Build.VERSION_CODES.O)
class Services : Service() {

    private var finish = false

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val notificationBuilder by lazy {
        createNotificationBuilder()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        coroutineScope.launch {
            while (!finish) {

                val manager = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                Log.d("PassCheat", manager.isDeviceSecure.toString())

                if (manager.isDeviceSecure) {
                    finish = true
                    stopSelf()
                    coroutineScope.cancel()
                } else {
                    val notification = notificationBuilder
                        .build()
                    notificationManager.notify(NOTIFICATION_ID, notification)
                    createNotificationChannel()
                    openSettings()
                    Log.d("PassCheat", "START_FLAG_RETRY")
                    finish = false
                    Toast.makeText(
                        applicationContext,
                        "Please create a password to protect your device!",
                        Toast.LENGTH_LONG
                    ).show()
                    delay(5000)
                }
            }
        }

        return START_STICKY
    }

    private fun openSettings() {
        var intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PARENT_PROFILE_PASSWORD)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotificationBuilder() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Title")
        .setContentText("Text")
        .setSmallIcon(R.drawable.ic_launcher_background)

    companion object {

        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "channel_name"
        private const val NOTIFICATION_ID = 1

        fun newIntent(context: Context): Intent {
            return Intent(context, Services::class.java)
        }
    }
}