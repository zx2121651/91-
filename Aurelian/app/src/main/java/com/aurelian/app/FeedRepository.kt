package com.aurelian.app

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FeedRepository {

    // Simple in-memory cache for demonstrating Cache-First strategy.
    // In a real production app, this would be Room DB or DataStore.
    private var cachedFeed: List<User>? = null

    /**
     * Emits data in a "Cache-First, Network-Then" approach.
     * Perfect for luxury apps where the first screen must load in < 100ms.
     */
    fun getFeedVideos(): Flow<Result<List<User>>> = flow {
        // 1. Emit Cache immediately if available
        cachedFeed?.let {
            Log.d("FeedRepository", "Emitting CACHE instantly")
            emit(Result.success(it))
        }

        try {
            Log.d("FeedRepository", "Fetching from NETWORK via API...")

            // Call the real Express.js backend running on localhost:3000 (10.0.2.2 for Android)
            val response = NetworkClient.apiService.getFeedVideos()
            val networkData = response.data

            // Update cache
            cachedFeed = networkData

            // 3. Emit fresh network data
            Log.d("FeedRepository", "Emitting NETWORK data")
            emit(Result.success(networkData))

        } catch (e: Exception) {
            Log.e("FeedRepository", "Network fetch failed", e)
            // Only emit failure if we didn't have any cache to show
            if (cachedFeed == null) {
                emit(Result.failure(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    // Simulating the backend response with high-quality MP4 sources
    private fun fetchMockNetworkData(): List<User> {
        return listOf(
            User(
                id = "mock_user_1",
                name = "Alexandre R.",
                location = "Monaco Yacht Club",
                bio = "Enjoying the summer breeze. #Monaco",
                videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            User(
                id = "mock_user_2",
                name = "Eleanor V.",
                location = "Paris, France",
                bio = "Night stroll around the Louvre.",
                videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            ),
            User(
                id = "mock_user_3",
                name = "Sebastian K.",
                location = "Geneva, Switzerland",
                bio = "Testing the limits of time.",
                videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
            )
        )
    }
}
