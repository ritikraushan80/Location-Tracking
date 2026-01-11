package com.kredily.tracking.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kredily.tracking.worker.LocationSyncWorker
import java.util.concurrent.TimeUnit


/**
 * Created by Ritik on: 10/01/26
 */

object SyncScheduler {

    private const val PERIODIC_WORK_NAME = "location_sync_periodic"
    private const val IMMEDIATE_SYNC_WORK_NAME = "location_sync_immediate"

    /**----- Start a one-time sync immediately or manually triggers sync ------*/
    fun startOneTimeSync(context: Context) {
        try {
//            Log.d(LogTags.SYNC, "Starting one-time sync via WorkManager")

            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            /**----- Multiple rapid sync requests ------*/
            val request =
                OneTimeWorkRequestBuilder<LocationSyncWorker>().setConstraints(constraints)
                    .setInitialDelay(Config.IMMEDIATE_SYNC_DELAY_SECONDS, TimeUnit.SECONDS)
                    .addTag("immediate_sync").build()

            /**----- Replace any pending immediate sync ------*/
            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_SYNC_WORK_NAME, androidx.work.ExistingWorkPolicy.REPLACE, request
            )
            Log.d(LogTags.SYNC, "One-time sync work enqueued successfully")
        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "Failed to enqueue sync work", e)
        }
    }

    /**--------- Schedule periodic sync minimum 15 minutes ---------*/
    fun scheduleLocationSync(context: Context) {
        try {

            val intervalMinutes = maxOf(Config.PERIODIC_SYNC_INTERVAL_MINUTES, 15L)
//            Log.d(LogTags.SYNC, "Scheduling periodic sync every $intervalMinutes minutes (Android minimum: 15)")

            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val work = PeriodicWorkRequestBuilder<LocationSyncWorker>(
                intervalMinutes, TimeUnit.MINUTES
            ).setConstraints(constraints).addTag("periodic_sync").build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, work
            )
            Log.d(LogTags.SYNC, "Periodic sync work scheduled successfully")
        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "Failed to schedule periodic sync", e)
        }
    }

}
