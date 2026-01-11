package com.kredily.tracking.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kredily.tracking.data.local.AppDatabase
import com.kredily.tracking.data.local.LocationEntity
import com.kredily.tracking.data.remote.LocationApi
import com.kredily.tracking.data.remote.LocationRequest
import com.kredily.tracking.data.repository.LocationRepository
import com.kredily.tracking.util.Config
import com.kredily.tracking.util.LogTags
import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.pow

/**
 * Created by Ritik on: 10/01/26
 */

class LocationSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        if (!isNetworkAvailable()) {
            return Result.retry()
        }

        val db = AppDatabase.create(applicationContext)
        val dao = db.locationDao()
        val repository = LocationRepository(dao)

        val api = Retrofit.Builder().baseUrl(Config.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(LocationApi::class.java)

        var allPending = dao.getPending()

        if (allPending.isEmpty()) {
            return Result.success()
        }

        Log.d(LogTags.SYNC, "Starting sync for ${allPending.size} pending locations")

        var totalSynced = 0
        var hasErrors = false

        while (allPending.isNotEmpty()) {
            val batch = allPending.take(Config.SYNC_BATCH_SIZE)
            val batchIds = batch.map { it.id }

            val result = syncBatch(api, batch, repository, batchIds)

            when (result) {
                SyncResult.SUCCESS -> {
                    totalSynced += batch.size
                    Log.d(
                        LogTags.SYNC, "Synced batch of ${batch.size} locations. Total: $totalSynced"
                    )
                }

                SyncResult.RETRY -> {
                    hasErrors = true
                    Log.w(LogTags.SYNC, "Batch sync failed, will retry")
                    break
                }

                SyncResult.FAILURE -> {
                    hasErrors = true
                    Log.e(LogTags.SYNC, "Batch sync failed permanently")
                    break
                }
            }

            /**------ Get remaining pending locations ------*/
            allPending = dao.getPending()
        }

        return when {
            totalSynced > 0 && !hasErrors -> {
                Log.d(LogTags.SYNC, "Sync completed successfully. Synced $totalSynced locations")
                Result.success()
            }

            hasErrors -> {
                Log.w(LogTags.SYNC, "Sync completed with errors. Synced $totalSynced locations")
                Result.retry()
            }

            else -> Result.success()
        }
    }

    private suspend fun syncBatch(
        api: LocationApi,
        batch: List<LocationEntity>,
        repository: LocationRepository,
        batchIds: List<Long>
    ): SyncResult {
        return try {
            val requests = batch.map {
                LocationRequest(
                    it.employeeId, it.latitude, it.longitude, it.accuracy, it.timestamp, it.speed
                )
            }

            var attempt = 0
            var lastException: Exception? = null

            while (attempt < Config.MAX_RETRY_ATTEMPTS) {
                try {
                    api.upload(requests)

                    val marked = repository.markSynced(batchIds)
                    if (marked) {
                        Log.d(
                            LogTags.SYNC,
                            "Successfully synced and marked batch of ${batch.size} locations"
                        )
                        return SyncResult.SUCCESS
                    } else {
                        Log.e(
                            LogTags.SYNC,
                            "Failed to mark locations as synced after successful upload"
                        )
                        return SyncResult.RETRY
                    }
                } catch (e: Exception) {
                    lastException = e
                    attempt++

                    when {
                        e is HttpException && e.code() in 400..499 -> {
                            Log.e(LogTags.SYNC, "Client error (${e.code()}): ${e.message}", e)
                            return SyncResult.FAILURE
                        }

                        e is HttpException && e.code() in 500..599 -> {
                            Log.w(
                                LogTags.SYNC,
                                "Server error (${e.code()}), attempt $attempt/${Config.MAX_RETRY_ATTEMPTS}",
                                e
                            )
                            if (attempt < Config.MAX_RETRY_ATTEMPTS) {
                                delay(calculateBackoffDelay(attempt))
                            }
                        }

                        e is SocketTimeoutException || e is UnknownHostException || e is IOException -> {
                            Log.w(
                                LogTags.SYNC,
                                "Network error, attempt $attempt/${Config.MAX_RETRY_ATTEMPTS}",
                                e
                            )
                            if (attempt < Config.MAX_RETRY_ATTEMPTS) {
                                delay(calculateBackoffDelay(attempt))
                            }
                        }

                        else -> {
                            Log.e(
                                LogTags.SYNC,
                                "Unknown error, attempt $attempt/${Config.MAX_RETRY_ATTEMPTS}",
                                e
                            )
                            if (attempt < Config.MAX_RETRY_ATTEMPTS) {
                                delay(calculateBackoffDelay(attempt))
                            }
                        }
                    }
                }
            }

            Log.e(
                LogTags.SYNC,
                "Failed to sync batch after ${Config.MAX_RETRY_ATTEMPTS} attempts",
                lastException
            )
            SyncResult.RETRY

        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "Unexpected error during batch sync", e)
            SyncResult.RETRY
        }
    }

    private fun calculateBackoffDelay(attempt: Int): Long {
        return (2.0.pow(attempt.toDouble()) * 1000).toLong()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

    private enum class SyncResult {
        SUCCESS, RETRY, FAILURE
    }
}
