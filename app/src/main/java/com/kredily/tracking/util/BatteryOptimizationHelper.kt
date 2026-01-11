package com.kredily.tracking.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log


/**
 * Created by Ritik on: 10/01/26
 */
object BatteryOptimizationHelper {

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                pm.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "Error checking battery optimization status", e)
            // If we can't check, assume optimizations are active (safer assumption)
            false
        }
    }

    fun requestIgnoreBatteryOptimizations(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        return try {
            if (isIgnoringBatteryOptimizations(activity)) {
                Log.d(LogTags.SYNC, "Already ignoring battery optimizations")
                return true
            }

            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
//                Log.d(LogTags.SYNC, "Battery optimization settings opened")
                true
            } else {
//                Log.w(LogTags.SYNC, "Battery optimization settings not available on this device")
                openBatterySettings(activity)
                false
            }
        } catch (e: SecurityException) {
//            Log.e(LogTags.SYNC, "SecurityException: Cannot request battery optimization exemption", e)
            openBatterySettings(activity)
            false
        } catch (e: Exception) {
//            Log.e(LogTags.SYNC, "Error requesting battery optimization exemption", e)
            openBatterySettings(activity)
            false
        }
    }

    private fun openBatterySettings(activity: Activity) {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    }
                    else -> {
                        action = Settings.ACTION_SETTINGS
                    }
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
                Log.d(LogTags.SYNC, "Opened battery settings page")
            }
        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "Error opening battery settings", e)
        }
    }
}
