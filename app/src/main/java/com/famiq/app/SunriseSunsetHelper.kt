// SunriseSunsetHelper.kt
package com.famiq.app

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import kotlin.math.*

object SunriseSunsetHelper {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    /**
     * Panggil ini setiap kali lokasi update atau app foreground.
     * Jika lokasi null, fallback ke jam 06:00–18:00 sebagai siang.
     */
    fun update(location: Location?) {
        _isDarkMode.value = isNightNow(location)
    }

    private fun isNightNow(location: Location?): Boolean {
        val now = Calendar.getInstance()
        val lat = location?.latitude ?: return isNightFallback(now)
        val lon = location.longitude

        val (sunrise, sunset) = calculateSunriseSunset(
            lat, lon,
            now.get(Calendar.DAY_OF_YEAR),
            now.get(Calendar.YEAR)
        )

        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return currentMinutes < sunrise || currentMinutes > sunset
    }

    private fun isNightFallback(cal: Calendar): Boolean {
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 18
    }

    /**
     * Algoritma NOAA sederhana, return Pair(sunriseMinutes, sunsetMinutes)
     * dalam menit dari tengah malam (waktu lokal).
     */
    private fun calculateSunriseSunset(
        lat: Double, lon: Double,
        dayOfYear: Int, year: Int
    ): Pair<Int, Int> {
        val d = dayOfYear + (if (isLeapYear(year)) 0 else 0).toDouble()

        // Deklinasi matahari
        val declination = -23.45 * cos(Math.toRadians((360.0 / 365.0) * (d + 10)))

        // Hour angle
        val cosH = (cos(Math.toRadians(90.833)) -
                sin(Math.toRadians(lat)) * sin(Math.toRadians(declination))) /
                (cos(Math.toRadians(lat)) * cos(Math.toRadians(declination)))

        if (cosH < -1 || cosH > 1) {
            // Midnight sun atau polar night — fallback
            return Pair(360, 1200)
        }

        val H = Math.toDegrees(acos(cosH))

        // UTC offset kasar (gunakan timezone device)
        val utcOffset = Calendar.getInstance().timeZone.rawOffset / 3600000.0

        val sunriseUTC = 12 - H / 15 - lon / 15
        val sunsetUTC  = 12 + H / 15 - lon / 15

        val sunriseLocal = ((sunriseUTC + utcOffset) * 60).toInt()
        val sunsetLocal  = ((sunsetUTC  + utcOffset) * 60).toInt()

        return Pair(sunriseLocal, sunsetLocal)
    }

    private fun isLeapYear(year: Int) =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}