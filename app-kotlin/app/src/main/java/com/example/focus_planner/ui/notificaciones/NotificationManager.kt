package com.example.focus_planner.ui.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

fun sendNotification(context: Context, message: String) {
    val channelId = "pomodoro_notifications"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Pomodoro Notifications", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Pomodoro")
        .setContentText(message)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

    notificationManager.notify(0, notification)
}
