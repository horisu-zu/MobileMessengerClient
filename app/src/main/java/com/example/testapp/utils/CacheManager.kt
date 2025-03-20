package com.example.testapp.utils

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object CacheManager {

    private var cache: SimpleCache? = null

    @Synchronized
    fun getCache(context: Context): SimpleCache {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, "exo_cache")
            if (!cacheDir.exists()) cacheDir.mkdir()

            val evictor = LeastRecentlyUsedCacheEvictor(256 * 1024 * 1024) //256 MB
            val databaseProvider = StandaloneDatabaseProvider(context)

            cache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return cache!!
    }

    fun releaseCache() {
        cache?.release()
        cache = null
    }
}