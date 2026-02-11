package com.soaharisonstebane.mneme.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class InternetChecker {
    fun isOnline(
        context: Context,
        onNoInternet: () -> Unit,
        onInternetViaEthernet: () -> Unit,
        onInternetViaWifi: () -> Unit,
        onInternetViaCellular: () -> Unit,
    ) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    onInternetViaEthernet()
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    onInternetViaWifi()
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    onInternetViaCellular()
                }
                else -> {
                    onNoInternet()
                }
            }
        }
    }

}