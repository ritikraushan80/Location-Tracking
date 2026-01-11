package com.kredily.tracking.util

/**
 * Created by Ritik on: 10/01/26
 */
object Config {

    const val API_BASE_URL = "https://6960c80ce7aa517cb7971587.mockapi.io/"

    const val SYNC_BATCH_SIZE = 100
    const val MAX_RETRY_ATTEMPTS = 3
    const val PERIODIC_SYNC_INTERVAL_MINUTES = 15L
    const val IMMEDIATE_SYNC_DELAY_SECONDS = 5L
    const val SERVICE_SYNC_CHECK_INTERVAL_MINUTES = 2L

    const val LOCATION_UPDATE_INTERVAL_MS = 10_000L

}

