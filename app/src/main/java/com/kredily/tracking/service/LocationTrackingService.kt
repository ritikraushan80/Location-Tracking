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
import com.kredily.tracking.util.LogTags
import com.kredily.tracking.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Created by Ritik on: 10/01/26
 */

class LocationTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var repository: LocationRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        startForeground(
            1, createNotification()
        )

        requestLocationUpdates()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L
        ).build()

        fusedClient.requestLocationUpdates(
            request, locationCallback, Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            Log.d(
                LogTags.LOCATION,
                "Location received → lat=${location.latitude}, " + "lng=${location.longitude}, acc=${location.accuracy}, " + "speed=${location.speed}"
            )

            serviceScope.launch {
                repository.save(
                    LocationEntity(
                        employeeId = "EMP001",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        speed = location.speed,
                        timestamp = System.currentTimeMillis()
                    )
                )
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


