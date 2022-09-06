package com.example.passcheat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.example.passcheat.Services.LockType.getCurrent
import java.io.File
import java.lang.NullPointerException

@RequiresApi(Build.VERSION_CODES.O)
class Services : Service() {
    private val app: MainActivity = MainActivity()

    // declaring variables
    private lateinit var notificationManager: NotificationManager

    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("PassCheat", "onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PassCheat", "onStartCommand")
        Log.d("PassCheat" , "SOMETHING: ${DevicePolicyManager.PASSWORD_QUALITY_SOMETHING}" )
        Log.d("PassCheat" , "SOMETHING: ${DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK.toLong()}" )
        
        initialization()

        createChannels()
        Thread.sleep(5000)
        return START_STICKY
    }

    object LockType {
        private const val PASSWORD_TYPE_KEY = "lockscreen.password_type"

        /**
         * This constant means that android using some unlock method not described here.
         * Possible new methods would be added in the future releases.
         */
        const val SOMETHING_ELSE = 0

        /**
         * Android using "None" or "Slide" unlock method. It seems there is no way to determine which method exactly used.
         * In both cases you'll get "PASSWORD_QUALITY_SOMETHING" and "LOCK_PATTERN_ENABLED" == 0.
         */
        const val NONE_OR_SLIDER = 1

        /**
         * Android using "Face Unlock" with "Pattern" as additional unlock method. Android don't allow you to select
         * "Face Unlock" without additional unlock method.
         */
        const val FACE_WITH_PATTERN = 3

        /**
         * Android using "Face Unlock" with "PIN" as additional unlock method. Android don't allow you to select
         * "Face Unlock" without additional unlock method.
         */
        const val FACE_WITH_PIN = 4

        /**
         * Android using "Face Unlock" with some additional unlock method not described here.
         * Possible new methods would be added in the future releases. Values from 5 to 8 reserved for this situation.
         */
        const val FACE_WITH_SOMETHING_ELSE = 9

        /**
         * Android using "Pattern" unlock method.
         */
        const val PATTERN = 10

        /**
         * Android using "PIN" unlock method.
         */
        const val PIN = 11

        /**
         * Android using "Password" unlock method with password containing only letters.
         */
        const val PASSWORD_ALPHABETIC = 12

        /**
         * Android using "Password" unlock method with password containing both letters and numbers.
         */
        const val PASSWORD_ALPHANUMERIC = 13

        /**
         * Returns current unlock method as integer value. You can see all possible values above
         * @param contentResolver we need to pass ContentResolver to Settings.Secure.getLong(...) and
         * Settings.Secure.getInt(...)
         * @return current unlock method as integer value
         */
        fun getCurrent(contentResolver: ContentResolver?): Int {

            val mode = Settings.Secure.getLong(
                contentResolver, PASSWORD_TYPE_KEY,
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING.toLong()
            )
            return if (mode == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING.toLong()) {
                if (Settings.Secure.getInt(
                        contentResolver,
                        Settings.Secure.LOCK_PATTERN_ENABLED,
                        0
                    ) == 1
                ) {
                    PATTERN
                } else NONE_OR_SLIDER
            } else if (mode == DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK.toLong()) {

                val dataDirPath = Environment.getDataDirectory().absolutePath
                if (nonEmptyFileExists("$dataDirPath/system/gesture.key")) {
                    FACE_WITH_PATTERN
                } else if (nonEmptyFileExists("$dataDirPath/system/password.key")) {
                    FACE_WITH_PIN
                } else FACE_WITH_SOMETHING_ELSE

            } else if (mode == DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC.toLong()) {
                Log.d("PassCheat", "if PASSWORD_QUALITY_ALPHANUMERIC")
                PASSWORD_ALPHANUMERIC
            } else if (mode == DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC.toLong()) {
                Log.d("PassCheat", "if PASSWORD_QUALITY_ALPHABETIC")
                PASSWORD_ALPHABETIC
            } else if (mode == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC.toLong()) {
                Log.d("PassCheat", "if PASSWORD_QUALITY_NUMERIC")
                PIN
            } else SOMETHING_ELSE
        }

        private fun nonEmptyFileExists(filename: String): Boolean {
            val file = File(filename)
            return file.exists() && file.length() > 0
        }
    }

    private var lockType = try {
        getCurrent(contentResolver)
    } catch (e: NullPointerException) {
        0
    } finally {
        0
    }

    private fun initialization() {
        val type = when (lockType) {
            LockType.FACE_WITH_PATTERN -> {
                Log.d("PassCheat", "FACE_WITH_PATTERN")
                1
            }
            LockType.FACE_WITH_PIN -> {
                Log.d("PassCheat", "FACE_WITH_PIN")
                2
            }
            LockType.NONE_OR_SLIDER -> {
                Log.d("PassCheat", "NONE_OR_SLIDER")
                3
            }
            LockType.PIN -> {
                Log.d("PassCheat", "PIN")
                4
            }
            LockType.PASSWORD_ALPHANUMERIC -> {
                Log.d("PassCheat", "PASSWORD_ALPHANUMERIC")
                5
            }
            LockType.FACE_WITH_SOMETHING_ELSE -> {
                Log.d("PassCheat", "FACE_WITH_SOMETHING_ELSE")
                6
            }
            LockType.PASSWORD_ALPHABETIC -> {
                Log.d("PassCheat", "PASSWORD_ALPHABETIC")
                7
            }
            LockType.PATTERN -> {
                Log.d("PassCheat", "PATTERN")
                8
            }
            LockType.SOMETHING_ELSE -> {
                Log.d("PassCheat", "SOMETHING_ELSE")
                9
            }
            else -> {
                Log.d("PassCheat", "else")
                9
            }
        }

        if (lockType >= LockType.FACE_WITH_PATTERN && lockType <= LockType.FACE_WITH_SOMETHING_ELSE) {
            Log.d("PassCheat", "LockType.FACE_WITH_PATTERN true")
        }

        Log.d("PassCheat", "lockType: $lockType")
        Log.d("PassCheat", "type: $type")
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.ic_launcher_background
                    )
                )
        } else {

            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setChannelId("Please create password!")
        }
        notificationManager.notify(1234, builder.build())
    }
}