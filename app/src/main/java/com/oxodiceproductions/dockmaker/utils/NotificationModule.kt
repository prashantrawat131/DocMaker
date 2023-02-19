package com.oxodiceproductions.dockmaker.utils

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.oxodiceproductions.dockmaker.R

class NotificationModule {
    fun generateNotification(context: Context, title: String?, contentText: String?) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = NotificationCompat.Builder(
            context,
            context.getString(R.string.DocMakerNotificationChannelId)
        )
            .setSmallIcon(R.drawable.app_icon_orange_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(context)

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId++, builder.build())
    }

    companion object {
        var notificationId = 0
    }
}