package com.kredily.tracking.data.remote

import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Created by Ritik on: 10/01/26
 */

interface LocationApi {

    @POST("location/location_tracking")
    suspend fun upload(@Body body: List<LocationRequest>)
}
