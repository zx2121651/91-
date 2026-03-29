package com.aurelian.app

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object ExoPlayerPool {

    private const val MAX_POOL_SIZE = 3
    private val availablePlayers = mutableListOf<ExoPlayer>()
    private val inUsePlayers = mutableSetOf<ExoPlayer>()

    @Synchronized
    fun acquirePlayer(context: Context): ExoPlayer {
        val player = if (availablePlayers.isNotEmpty()) {
            availablePlayers.removeAt(0)
        } else if (inUsePlayers.size < MAX_POOL_SIZE) {
            ExoPlayer.Builder(context.applicationContext).build()
        } else {
            // Evict the oldest used player if the pool is full.
            // In a strict managed pool, you might throw or wait.
            // Here we force evict for simplicity in rapid scrolling.
            val oldest = inUsePlayers.iterator().next()
            inUsePlayers.remove(oldest)
            oldest.stop()
            oldest.clearMediaItems()
            oldest
        }

        inUsePlayers.add(player)
        return player
    }

    @Synchronized
    fun releasePlayer(player: ExoPlayer) {
        if (inUsePlayers.remove(player)) {
            player.stop()
            player.clearMediaItems()
            availablePlayers.add(player)
        }
    }

    @Synchronized
    fun releaseAll() {
        inUsePlayers.forEach { it.release() }
        inUsePlayers.clear()

        availablePlayers.forEach { it.release() }
        availablePlayers.clear()
    }
}
