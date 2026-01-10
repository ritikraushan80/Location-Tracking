package com.kredily.tracking.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * Created by Ritik on: 10/01/26
 */

class NetworkMonitor(context: Context) {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    init {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                _isOnline.value = true

                /**--------- Sync on network up (optional) ---------*/
//                SyncScheduler.startOneTimeSync(context)
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        })
    }
}

