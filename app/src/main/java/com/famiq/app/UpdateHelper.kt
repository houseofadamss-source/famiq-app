package com.famiq.app

import com.famiq.app.network.GitHubApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UpdateHelper {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GitHubApiService = retrofit.create(GitHubApiService::class.java)

    fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        // Simple comparison: v1.0.1 vs v1.0.2
        val current = currentVersion.removePrefix("v").replace(".", "").toIntOrNull() ?: 0
        val latest = latestVersion.removePrefix("v").replace(".", "").toIntOrNull() ?: 0
        return latest > current
    }
}
