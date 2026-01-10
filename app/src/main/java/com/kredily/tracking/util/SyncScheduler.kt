package com.kredily.tracking.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kredily.tracking.worker.LocationSyncWorker


/**
 * Created by Ritik on: 10/01/26
 */

object SyncScheduler {

    fun startOneTimeSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<LocationSyncWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
