package com.aurelian.app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Response wrappers based on our API_DOCUMENTATION.md
data class FeedResponse(
    val data: List<User>,
    val nextCursor: String?
)

interface AurelianApiService {
    // 10.0.2.2 is the special alias to the host loopback interface in Android Emulator
    @GET("api/v1/feed/videos")
    suspend fun getFeedVideos(): FeedResponse
}

object NetworkClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: AurelianApiService = retrofit.create(AurelianApiService::class.java)
}
