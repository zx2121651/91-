package com.aurelian.app

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object VideoCacheManager {
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null

    // 100 MB max cache size
    private const val MAX_CACHE_SIZE: Long = 100 * 1024 * 1024

    fun initialize(context: Context) {
        if (simpleCache != null) return

        val cacheDir = File(context.cacheDir, "media3_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
        val databaseProvider = StandaloneDatabaseProvider(context)

        simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getCacheDataSourceFactory(): CacheDataSource.Factory {
        return cacheDataSourceFactory ?: throw IllegalStateException("VideoCacheManager not initialized")
    }

    fun getCache(): SimpleCache {
        return simpleCache ?: throw IllegalStateException("VideoCacheManager not initialized")
    }
}
