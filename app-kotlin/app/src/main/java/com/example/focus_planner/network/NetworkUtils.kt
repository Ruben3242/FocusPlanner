package com.example.focus_planner.network

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkUtils {

    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    suspend fun isApiAvailable(): Boolean {
        return try {
            val url = URL("https://8a29-92-189-98-92.ngrok-free.app/api/health")
            withContext(Dispatchers.IO) {
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.connect()
                connection.responseCode == 200
            }
        } catch (e: IOException) {
            false
        }
    }
}
