package com.famiq.app.network

import com.famiq.app.data.model.GitHubRelease
import retrofit2.http.GET

interface GitHubApiService {
    @GET("repos/houseofadamss-source/famiq-app/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
