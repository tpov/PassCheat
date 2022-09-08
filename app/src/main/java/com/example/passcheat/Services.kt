package com.example.passcheat

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
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

                val manager =
                    applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                Log.d("PassCheat", manager.isDeviceSecure.toString())

                if (manager.isDeviceSecure) {
                    finish = true
                    stopSelf()
                    coroutineScope.cancel()
                } else {
                    createDialog("")
                    val notification = notificationBuilder
                        .build()
                    notificationManager.notify(NOTIFICATION_ID, notification)
                    createNotificationChannel()
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

    @SuppressLint("InvalidWakeLockTag")
    private fun createDialog(msg: String) {

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.dialog, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER or Gravity.CENTER
        params.x = 0
        params.y = 0
        windowManager.addView(view, params)

        view.setOnClickListener {

        }
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
        .setContentTitle("Create password")
        .setContentText("Please create a password to protect your device!")
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
