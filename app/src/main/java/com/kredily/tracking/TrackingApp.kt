package com.kredily.tracking

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kredily.tracking.util.NotificationHelper
import com.kredily.tracking.worker.LocationSyncWorker
import java.util.concurrent.TimeUnit


/**
 * Created by Ritik on: 10/01/26
 */

class TrackingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleLocationSync()
        NotificationHelper.createChannel(this)
    }

    private fun scheduleLocationSync() {
        val work = PeriodicWorkRequestBuilder<LocationSyncWorker>(
            60, TimeUnit.SECONDS
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "location_sync", ExistingPeriodicWorkPolicy.KEEP, work
        )
    }
}