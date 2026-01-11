package com.kredily.tracking.data.repository

import android.util.Log
import com.kredily.tracking.data.local.LocationDao
import com.kredily.tracking.data.local.LocationEntity
import com.kredily.tracking.util.LogTags


/**
 * Created by Ritik on: 10/01/26
 */

class LocationRepository(private val dao: LocationDao) {

    suspend fun save(location: LocationEntity): Boolean {
        return try {
            dao.insert(location)
            Log.d(LogTags.DB, "Inserted location into Room DB: lat=${location.latitude}, lng=${location.longitude}")
            true
        } catch (e: Exception) {
            Log.e(LogTags.DB, "Failed to insert location into Room DB", e)
            false
        }
    }

    suspend fun pending() = dao.getPending()

    suspend fun markSynced(ids: List<Long>): Boolean {
        return try {
            dao.markSynced(ids)
            Log.d(LogTags.DB, "Marked ${ids.size} locations as synced")
            true
        } catch (e: Exception) {
            Log.e(LogTags.DB, "Failed to mark locations as synced", e)
            false
        }
    }

    fun pendingCount() = dao.pendingCount()
}
