package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.model.InternetStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectivityObserver(context: Context) {

    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _status = MutableStateFlow(evaluateCurrentStatus())
    val status: StateFlow<InternetStatus> = _status.asStateFlow()

    private var isRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            refreshStatus()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            refreshStatus()
        }

        override fun onLost(network: Network) {
            refreshStatus()
        }

        override fun onUnavailable() {
            refreshStatus()
        }
    }

    fun startObserving() {
        val cm = connectivityManager ?: return
        if (isRegistered) return
        refreshStatus()
        runCatching {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, networkCallback)
            isRegistered = true
        }
    }

    fun stopObserving() {
        val cm = connectivityManager ?: return
        if (!isRegistered) return
        runCatching {
            cm.unregisterNetworkCallback(networkCallback)
        }
        isRegistered = false
    }

    fun refreshStatus() {
        _status.value = evaluateCurrentStatus()
    }

    private fun evaluateCurrentStatus(): InternetStatus {
        val cm = connectivityManager ?: return InternetStatus.DISCONNECTED
        return runCatching {
            val activeNetwork = cm.activeNetwork ?: return InternetStatus.DISCONNECTED
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return InternetStatus.DISCONNECTED

            val hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

            if (!hasTransport) {
                return InternetStatus.DISCONNECTED
            }

            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            when {
                hasInternet && isValidated -> InternetStatus.VALIDATED_INTERNET
                hasInternet -> InternetStatus.CONNECTED_NO_INTERNET
                else -> InternetStatus.DISCONNECTED
            }
        }.getOrDefault(InternetStatus.DISCONNECTED)
    }
}
