package com.ssoaharison.recall.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ssoaharison.recall.util.InternetStatus.INTERNET_VIA_CELLULAR
import com.ssoaharison.recall.util.InternetStatus.INTERNET_VIA_ETHERNET
import com.ssoaharison.recall.util.InternetStatus.INTERNET_VIA_WIFI
import com.ssoaharison.recall.util.InternetStatus.NO_INTERNET

class InternetChecker {
    fun isOnline(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    INTERNET_VIA_ETHERNET
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    INTERNET_VIA_WIFI
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    INTERNET_VIA_CELLULAR
                }
                else -> {
                    NO_INTERNET
                }
            }
        }
        return NO_INTERNET
    }

}