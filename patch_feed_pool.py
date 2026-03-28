import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    content = f.read()

import_pattern = "import androidx.media3.exoplayer.source.ProgressiveMediaSource"
new_import = """import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.common.Player"""

# Assuming Player is already imported but let's make sure, we just replace the player initialization block.

old_player_block = r"""    var isVideoReady by remember \{ mutableStateOf\(false\) \}

    val exoPlayer = remember \{
        val cacheDataSourceFactory = VideoCacheManager\.getCacheDataSourceFactory\(\)
        val mediaItem = MediaItem\.fromUri\(Uri\.parse\(user\.videoUrl\)\)
        val mediaSource = ProgressiveMediaSource\.Factory\(cacheDataSourceFactory\)
            \.createMediaSource\(mediaItem\)

        ExoPlayer\.Builder\(context\)\.build\(\)\.apply \{
            setMediaSource\(mediaSource\)
            repeatMode = Player\.REPEAT_MODE_ALL
            addListener\(object : Player\.Listener \{
                override fun onPlaybackStateChanged\(playbackState: Int\) \{
                    if \(playbackState == Player\.STATE_READY\) \{
                        isVideoReady = true
                    \}
                \}
            \}\)
            prepare\(\)
        \}
    \}

    LaunchedEffect\(isSelected\) \{
        if \(isSelected\) \{
            exoPlayer\.play\(\)
        \} else \{
            exoPlayer\.pause\(\)
        \}
    \}

    DisposableEffect\(Unit\) \{
        onDispose \{
            exoPlayer\.release\(\)
        \}
    \}"""

new_player_block = """    var isVideoReady by remember { mutableStateOf(false) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Use DisposableEffect tied to user.id or URL to ensure it correctly manages the player instance
    DisposableEffect(user.id) {
        val player = ExoPlayerPool.acquirePlayer(context)

        val cacheDataSourceFactory = VideoCacheManager.getCacheDataSourceFactory()
        val mediaItem = MediaItem.fromUri(Uri.parse(user.videoUrl))
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player.apply {
            setMediaSource(mediaSource)
            repeatMode = Player.REPEAT_MODE_ALL

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isVideoReady = true
                    }
                }
            })
            prepare()
        }

        exoPlayer = player

        onDispose {
            // Return to pool instead of releasing completely
            ExoPlayerPool.releasePlayer(player)
            exoPlayer = null
            isVideoReady = false
        }
    }

    LaunchedEffect(isSelected, exoPlayer) {
        if (isSelected) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }
    }"""

content = re.sub(old_player_block, new_player_block, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(content)
