package com.kredily.tracking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kredily.tracking.data.local.AppDatabase
import com.kredily.tracking.data.repository.LocationRepository
import com.kredily.tracking.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


/**
 * Created by Ritik on: 10/01/26
 */

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.create(application).locationDao()

    private val repository = LocationRepository(dao)

    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val pendingCount: Flow<Int> = repository.pendingCount()

    /**---- Clean up network monitor to prevent memory leaks -----*/
    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregister()
    }
}

