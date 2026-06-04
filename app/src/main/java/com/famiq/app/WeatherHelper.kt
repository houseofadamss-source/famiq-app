package com.famiq.app

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class WeatherData(
    val suhu: Int,
    val kondisi: String,
    val iconCode: String,
    val kota: String       // ✅ tambah ini
)

object WeatherHelper {

    private val _weather = MutableStateFlow<WeatherData?>(null)
    val weather: StateFlow<WeatherData?> = _weather

    private const val API_KEY = "8567ae17a8d2b9749849012832ab6340"

    suspend fun fetch(location: Location) {
        withContext(Dispatchers.IO) {
            try {
                val lat = location.latitude
                val lon = location.longitude
                val url = "https://api.openweathermap.org/data/2.5/weather" +
                        "?lat=$lat&lon=$lon&appid=$API_KEY&units=metric&lang=id"

                android.util.Log.d("WeatherHelper", "Fetching: $url")

                val response = URL(url).readText()

                android.util.Log.d("WeatherHelper", "Response: $response")

                val json     = JSONObject(response)
                val suhu     = json.getJSONObject("main").getDouble("temp").toInt()
                val kondisi  = json.getJSONArray("weather").getJSONObject(0).getString("main")
                val iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon")
                val kota     = json.optString("name", "")   // ✅ ambil nama kota

                _weather.value = WeatherData(suhu, kondisi, iconCode, kota)

            } catch (e: Exception) {
                android.util.Log.e("WeatherHelper", "Error: ${e.message}")
                _weather.value = null
            }
        }
    }
}