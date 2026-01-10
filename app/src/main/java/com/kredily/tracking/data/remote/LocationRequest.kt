package com.kredily.tracking.data.remote


/**
 * Created by Ritik on: 10/01/26
 */

data class LocationRequest(
    val employeeId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val speed: Float?
)
