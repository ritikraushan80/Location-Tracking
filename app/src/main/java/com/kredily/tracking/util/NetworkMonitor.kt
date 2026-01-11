package com.kredily.tracking.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * Created by Ritik on: 10/01/26
 */

class NetworkMonitor(context: Context) {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            _isOnline.value = true
//            Log.d(LogTags.SYNC, "Network available - triggering sync")
            SyncScheduler.startOneTimeSync(context)
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
//            Log.d(LogTags.SYNC, "Network lost")
        }
    }

    init {
        checkInitialNetworkState()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun checkInitialNetworkState() {
        val network = connectivityManager.activeNetwork ?: run {
            _isOnline.value = false
            return
        }
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
            _isOnline.value = false
            return
        }
        _isOnline.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "Error unregistering network callback", e)
        }
    }
}

