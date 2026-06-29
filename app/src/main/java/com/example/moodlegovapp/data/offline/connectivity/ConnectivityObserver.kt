package com.example.moodlegovapp.data.offline.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkType { WIFI, CELLULAR, OTHER, NONE }

/**
 * Observes device connectivity using [ConnectivityManager] (requires only the
 * already-declared ACCESS_NETWORK_STATE permission — no extra manifest entries).
 *
 * Used to decide:
 *  - whether to attempt a network call at all vs read straight from cache
 *  - whether a download counts as "large" per the Moodle doc's thresholds
 *    (Wi-Fi >= 20MB, cellular/data >= 2MB)
 *  - whether the user's "Wi-Fi only sync" preference should block auto-sync
 */
class ConnectivityObserver private constructor(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isOnline = MutableStateFlow(currentlyOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _networkType = MutableStateFlow(currentNetworkType())
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refresh()
        override fun onLost(network: Network) = refresh()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = refresh()
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    private fun refresh() {
        _isOnline.value = currentlyOnline()
        _networkType.value = currentNetworkType()
    }

    private fun currentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun currentNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.WIFI // treat as unmetered
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkType.NONE
            else -> NetworkType.OTHER
        }
    }

    fun isOnlineNow(): Boolean = currentlyOnline()

    companion object {
        @Volatile private var instance: ConnectivityObserver? = null

        fun getInstance(context: Context): ConnectivityObserver =
            instance ?: synchronized(this) {
                instance ?: ConnectivityObserver(context.applicationContext).also { instance = it }
            }
    }
}
