package com.translate.translator.voice.translation.dictionary.all.language.util

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.util.Pair
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.services.ScreenCaptureService


object NotificationUtils {
    const val NOTIFICATION_ID = 1337
    private const val NOTIFICATION_CHANNEL_ID = "com.maximus.elitetranslator.app"
    private const val NOTIFICATION_CHANNEL_NAME = "com.maximus.elitetranslator.app"


    fun getNotification(context: Context): Pair<Int, Notification> {
        NotificationUtils.createNotificationChannel(context)
        val notification: Notification = NotificationUtils.createNotification(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationUtils.NOTIFICATION_ID, notification)
        return Pair(NotificationUtils.NOTIFICATION_ID, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationUtils.NOTIFICATION_CHANNEL_ID,
                NotificationUtils.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(context: Context): Notification {
        val snoozePendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 0,ScreenCaptureService.getStopIntent(context),PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(context, 0,ScreenCaptureService.getStopIntent(context),0)
        }
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, NotificationUtils.NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_tab_screen_icon)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText(context.getString(R.string.screenCapture))
        builder.addAction(R.drawable.ic_close_black_icon, "Stop", snoozePendingIntent)
        builder.setOngoing(true)
        builder.setCategory(Notification.CATEGORY_SERVICE)
        builder.priority = Notification.PRIORITY_LOW
        builder.setShowWhen(true)
        return builder.build()
    }



}
