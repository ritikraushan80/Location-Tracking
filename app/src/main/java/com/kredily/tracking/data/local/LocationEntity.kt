package com.kredily.tracking.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by Ritik on: 10/01/26
 */

@Entity(tableName = "location_logs")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float?,
    val timestamp: Long,
    val synced: Boolean = false
)
