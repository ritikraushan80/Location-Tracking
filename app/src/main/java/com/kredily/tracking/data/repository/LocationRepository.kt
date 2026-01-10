package com.kredily.tracking.data.repository

import android.util.Log
import com.kredily.tracking.data.local.LocationDao
import com.kredily.tracking.data.local.LocationEntity
import com.kredily.tracking.util.LogTags


/**
 * Created by Ritik on: 10/01/26
 */

class LocationRepository(
    private val dao: LocationDao
) {

    suspend fun save(location: LocationEntity) {
        dao.insert(location)
        Log.d(LogTags.DB, "Inserted location into Room DB")
    }

    suspend fun pending() = dao.getPending()

    suspend fun markSynced(ids: List<Long>) {
        dao.markSynced(ids)
    }

    fun pendingCount() = dao.pendingCount()
}
