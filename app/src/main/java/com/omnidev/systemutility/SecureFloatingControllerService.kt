package com.omnidev.systemutility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.omnidev.systemutility.R

class SecureFloatingControllerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingOverlayView: FloatingOverlayView
    private lateinit var windowManagerLayoutParams: WindowManager.LayoutParams
    private var isOverlayAdded = false

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "floating_controller_channel"
        const val ACTION_STOP_FOREGROUND = "com.omnidev.systemutility.action.STOP_FOREGROUND"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingOverlayView = FloatingOverlayView(this)

        windowManagerLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_SECURE,
            PixelFormat.TRANSLUCENT
        )
        windowManagerLayoutParams.gravity = Gravity.TOP or Gravity.START
        windowManagerLayoutParams.x = 100
        windowManagerLayoutParams.y = 100
        windowManagerLayoutParams.packageName = packageName
        windowManagerLayoutParams.title = "Secure Floating Controller"

        floatingOverlayView.setOnDragRequested {
            ShizukuExecutionModule.executeShellCommandAsync("input swipe 300 500 700 500 500") { success, output ->
                // Drag command executed via Shizuku
            }
        }

        floatingOverlayView.setOnCloseRequested {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_FOREGROUND) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (!isOverlayAdded) {
            windowManager.addView(floatingOverlayView, windowManagerLayoutParams)
            isOverlayAdded = true
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Controller",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Keeps the floating overlay active"
            channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, SecureFloatingControllerService::class.java).apply {
            action = ACTION_STOP_FOREGROUND
        }
        val pendingStop = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Secure Floating Controller")
            .setContentText("Overlay is active and protected by FLAG_SECURE")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Stop", pendingStop)
            .build()
    }

    override fun onDestroy() {
        if (isOverlayAdded) {
            windowManager.removeView(floatingOverlayView)
            isOverlayAdded = false
        }
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}