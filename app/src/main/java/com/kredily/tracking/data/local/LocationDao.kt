package com.kredily.tracking.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


/**
 * Created by Ritik on: 10/01/26
 */

@Dao
interface LocationDao {

    @Insert
    suspend fun insert(location: LocationEntity)

    @Query("SELECT * FROM location_logs WHERE synced = 0 ORDER BY id ASC")
    suspend fun getPending(): List<LocationEntity>

    @Query("UPDATE location_logs SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM location_logs WHERE synced = 0")
    fun pendingCount(): Flow<Int>
}
