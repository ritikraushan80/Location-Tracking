package com.kredily.tracking.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


/**
 * Created by Ritik on: 10/01/26
 */

object NotificationHelper {
    const val CHANNEL_ID = "tracking_channel"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Live Tracking", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Location tracking in background"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
