package com.famiq.app

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getLocation(context: Context): Pair<Double, Double>? {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cancellationToken = CancellationTokenSource()
            val location = client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationToken.token
            ).await()
            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else {
                // Default Jakarta kalau lokasi tidak tersedia
                Pair(-6.2088, 106.8456)
            }
        } catch (e: Exception) {
            // Default Jakarta
            Pair(-6.2088, 106.8456)
        }
    }
}