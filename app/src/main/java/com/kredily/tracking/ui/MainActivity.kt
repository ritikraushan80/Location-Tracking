package com.kredily.tracking.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kredily.tracking.databinding.ActivityMainBinding
import com.kredily.tracking.service.LocationTrackingService
import com.kredily.tracking.util.BatteryOptimizationHelper
import com.kredily.tracking.util.SyncScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TrackingViewModel by viewModels()

    companion object {
        private const val REQ_PERMISSION = 1001
        private const val PREF_TRACKING = "pref_tracking"
        private const val KEY_IS_TRACKING = "is_tracking"

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**------- Request battery optimization exemption ---------*/
        if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
            BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
        }

        if (!hasForegroundPermissions()) {
            requestForegroundPermissions()
        }

        val shouldBeTracking = isTracking()
        val isServiceRunning = isServiceRunning(LocationTrackingService::class.java)


        if (shouldBeTracking && !isServiceRunning) {
            setTracking(false)
            updateButtonUI(false)
        } else {
            updateButtonUI(shouldBeTracking)
        }

        observeViewModel()

        /**--------- Click Listener for Tracking -------------*/
        binding.btnTracking.setOnClickListener {
            if (!isTracking()) {
                when {
                    !hasForegroundPermissions() -> {
                        requestForegroundPermissions()
                    }

                    !hasBackgroundPermission() -> {
                        requestBackgroundLocationPermission()
                    }

                    else -> {
                        startTracking()
                        setTracking(true)
                        updateButtonUI(true)
                    }
                }
            } else {
                stopTracking()
            }
        }

        /**--------- Custom Sync -------------*/
        binding.btnSync.setOnClickListener {
            lifecycleScope.launch {
                val isOnline = viewModel.isOnline.first()

                if (isOnline) {
                    Toast.makeText(
                        this@MainActivity, "Sync started", Toast.LENGTH_SHORT
                    ).show()

                    SyncScheduler.startOneTimeSync(this@MainActivity)

                } else {
                    Toast.makeText(
                        this@MainActivity, "You are offline", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    /**----------- Observer for network status and pending logs -------------*/
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.isOnline.collect { online ->
                        binding.tvNetworkStatus.text =
                            if (online) "Now Online" else "You are offline"

                        binding.tvNetworkStatus.setTextColor(
                            if (online) Color.GREEN
                            else Color.RED
                        )
                    }
                }

                launch {
                    viewModel.pendingCount.collect { count ->
                        binding.tvPending.text = "Pending logs: $count"
                    }
                }
            }
        }
    }

    /**------------ Foreground permissions -------------*/
    private fun requestForegroundPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestPermissions(
            permissions.toTypedArray(), REQ_PERMISSION
        )
    }

    /**------------------ Background permission ------------------*/
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQ_PERMISSION
            )
        }
    }

    /**---------------- Permission checks ------------------*/
    private fun hasForegroundPermissions(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            else true

        return fineGranted && notificationGranted
    }

    /**-------- Check if background permission is granted ---------*/
    private fun hasBackgroundPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode != REQ_PERMISSION) return

        when {
            hasForegroundPermissions() && !hasBackgroundPermission() -> {
                requestBackgroundLocationPermission()
            }

            hasForegroundPermissions() && hasBackgroundPermission() -> {
                Toast.makeText(
                    this, "All permissions granted", Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Toast.makeText(
                    this, "Permissions are required for live tracking", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**-------------------  Start foreground service ----------------------*/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTracking() {
        startForegroundService(
            Intent(this, LocationTrackingService::class.java)
        )
    }

    /**-------------------  Stop foreground service ----------------------*/
    private fun stopTracking() {
        stopService(Intent(this, LocationTrackingService::class.java))
        setTracking(false)
        updateButtonUI(false)
    }

    private fun isTracking(): Boolean =
        getSharedPreferences(PREF_TRACKING, MODE_PRIVATE).getBoolean(KEY_IS_TRACKING, false)

    private fun setTracking(active: Boolean) {
        getSharedPreferences(PREF_TRACKING, MODE_PRIVATE).edit {
            putBoolean(
                KEY_IS_TRACKING, active
            )
        }
    }

    private fun updateButtonUI(isTracking: Boolean) {
        binding.btnTracking.text = if (isTracking) "Stop Tracking" else "Start Tracking"
    }

    /**-------------------  Check if service is running ----------------------*/
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Integer.MAX_VALUE)
        return services.any { it.service.className == serviceClass.name }
    }

}
