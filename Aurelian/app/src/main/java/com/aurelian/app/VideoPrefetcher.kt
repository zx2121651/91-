package com.aurelian.app

import android.net.Uri
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

@UnstableApi
object VideoPrefetcher {
    private const val TAG = "VideoPrefetcher"

    // Limit to preloading just the first 2 Megabytes to save bandwidth
    private const val PRELOAD_BYTES = 2L * 1024L * 1024L

    private val prefetchJobs = ConcurrentHashMap<String, Job>()

    /**
     * Pre-fetches the first [PRELOAD_BYTES] of a video URL into the SimpleCache.
     */
    suspend fun prefetch(url: String?) {
        if (url == null) return

        // If already prefetching this URL, don't start again
        if (prefetchJobs.containsKey(url)) return

        val job = Job()
        prefetchJobs[url] = job

        withContext(Dispatchers.IO + job) {
            try {
                val cache = VideoCacheManager.getCache()
                val cacheDataSourceFactory = VideoCacheManager.getCacheDataSourceFactory()
                val dataSource = cacheDataSourceFactory.createDataSource()

                val dataSpec = DataSpec.Builder()
                    .setUri(Uri.parse(url))
                    .setPosition(0)
                    .setLength(PRELOAD_BYTES)
                    .build()

                val cacheWriter = CacheWriter(
                    dataSource,
                    dataSpec,
                    null, // pre-allocated byte array
                    object : CacheWriter.ProgressListener {
                        override fun onProgress(requestLength: Long, bytesCached: Long, newBytesCached: Long) {
                            if (bytesCached >= PRELOAD_BYTES) {
                                // Once we hit our target preload size, we can manually cancel or stop.
                                // CacheWriter stops automatically if we set length in DataSpec,
                                // but we could also interrupt here if needed.
                                Log.d(TAG, "Prefetched 2MB for $url")
                            }
                        }
                    }
                )

                Log.d(TAG, "Starting prefetch for $url")
                cacheWriter.cache()
                Log.d(TAG, "Finished prefetch for $url")

            } catch (e: Exception) {
                // CancellationExceptions are normal if user scrolls away fast
                Log.e(TAG, "Prefetch interrupted or failed for $url", e)
            } finally {
                prefetchJobs.remove(url)
            }
        }
    }

    /**
     * Cancels an ongoing prefetch for a specific URL, useful when the user
     * scrolls past a video quickly, returning bandwidth to the active player.
     */
    fun cancelPrefetch(url: String?) {
        if (url == null) return
        prefetchJobs.remove(url)?.cancel()
        Log.d(TAG, "Cancelled prefetch for $url")
    }

    fun cancelAll() {
        prefetchJobs.values.forEach { it.cancel() }
        prefetchJobs.clear()
        Log.d(TAG, "Cancelled all active prefetch jobs")
    }
}
