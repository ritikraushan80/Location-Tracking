package com.kredily.tracking.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kredily.tracking.data.local.AppDatabase
import com.kredily.tracking.data.local.LocationEntity
import com.kredily.tracking.data.repository.LocationRepository
import com.kredily.tracking.util.Config
import com.kredily.tracking.util.LogTags
import com.kredily.tracking.util.NotificationHelper
import com.kredily.tracking.util.SyncScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Created by Ritik on: 10/01/26
 */

class LocationTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var repository: LocationRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var lastSyncTime = 0L
    private val MIN_SYNC_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        if (!hasLocationPermission()) {
            stopSelf()
            return
        }

        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        repository = LocationRepository(
            AppDatabase.create(this).locationDao()
        )

        startForeground(1, createNotification())

        requestLocationUpdates()

        /**------ Start periodic sync when app is in background -------*/
        startPeriodicSyncCheck()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, Config.LOCATION_UPDATE_INTERVAL_MS
        ).build()

        fusedClient.requestLocationUpdates(
            request, locationCallback, Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: run {
                Log.w(LogTags.LOCATION, "Location result is null")
                return
            }

            Log.d(
                LogTags.LOCATION,
                "Location received → lat=${location.latitude}, " + "lng=${location.longitude}, acc=${location.accuracy}, " + "speed=${location.speed}"
            )

            serviceScope.launch {
                val saved = repository.save(
                    LocationEntity(
                        employeeId = "EMP001",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        speed = if (location.hasSpeed()) location.speed else null,
                        timestamp = System.currentTimeMillis()
                    )
                )
                if (!saved) {
                    Log.e(LogTags.LOCATION, "Failed to save location to database")
                } else {
                    checkAndTriggerSync()
                }
            }
        }
    }

    private fun startPeriodicSyncCheck() {
        serviceScope.launch {
            try {
                delay(TimeUnit.SECONDS.toMillis(30))

                while (isActive) {
                    try {
                        checkAndTriggerSync()
                        delay(TimeUnit.MINUTES.toMillis(Config.SERVICE_SYNC_CHECK_INTERVAL_MINUTES))
                    } catch (e: CancellationException) {
                        Log.d(LogTags.SYNC, "Periodic sync check cancelled (service stopping)")
                        throw e
                    } catch (e: Exception) {
//                        Log.e(LogTags.SYNC, "Error in periodic sync check", e)
                        if (isActive) {
                            delay(TimeUnit.MINUTES.toMillis(Config.SERVICE_SYNC_CHECK_INTERVAL_MINUTES))
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d(LogTags.SYNC, "Periodic sync check coroutine cancelled")
            }
        }
    }

    private fun checkAndTriggerSync() {
        val now = System.currentTimeMillis()
        if (now - lastSyncTime < MIN_SYNC_INTERVAL_MS) {
            Log.d(LogTags.SYNC, "Sync throttled, last sync was ${(now - lastSyncTime) / 1000}s ago")
            return
        }

        serviceScope.launch {
            try {
                val pendingCount = repository.pendingCount()
                val count = pendingCount.first()

                if (count > 0) {
//                    Log.d(LogTags.SYNC, "Service: Found $count pending locations, triggering sync")
                    SyncScheduler.startOneTimeSync(this@LocationTrackingService)
                    lastSyncTime = now
                } else {
//                    Log.d(LogTags.SYNC, "Service: No pending locations to sync")
                }
            } catch (e: Exception) {
                Log.e(LogTags.SYNC, "Error checking pending locations in service", e)
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(
            this, NotificationHelper.CHANNEL_ID
        ).setContentTitle("Live Tracking Active").setContentText("Location tracking running")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}


