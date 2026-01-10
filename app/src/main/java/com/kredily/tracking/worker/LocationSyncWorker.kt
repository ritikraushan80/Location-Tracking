package com.kredily.tracking.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kredily.tracking.data.local.AppDatabase
import com.kredily.tracking.data.remote.LocationApi
import com.kredily.tracking.data.remote.LocationRequest
import com.kredily.tracking.util.LogTags
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Ritik on: 10/01/26
 */

class LocationSyncWorker(
    ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.create(applicationContext)
        val dao = db.locationDao()

        val api = Retrofit.Builder().baseUrl("https://6960c80ce7aa517cb7971587.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(LocationApi::class.java)

        val pending = dao.getPending()

        if (pending.isEmpty()) return Result.success()

        return try {

            api.upload(pending.map {
                LocationRequest(
                    it.employeeId, it.latitude, it.longitude, it.accuracy, it.timestamp, it.speed
                )
            })
            dao.markSynced(pending.map { it.id })
            Log.d(LogTags.SYNC, "Sync successful, marked records as synced")
            Result.success()
        } catch (e: Exception) {

            Log.e(
                LogTags.SYNC, "Sync failed, will retry", e
            )

            Result.retry()
        }
    }
}
