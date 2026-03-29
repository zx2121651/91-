import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'r') as f:
    content = f.read()

# Add UnstableApi import for VideoPrefetcher if needed
if "import androidx.media3.common.util.UnstableApi" not in content:
    content = content.replace("import androidx.lifecycle.ViewModel", "import androidx.media3.common.util.UnstableApi\nimport androidx.lifecycle.ViewModel")

# Add the new prefetch triggering logic and replace the old dummy function
old_prefetch = r"""    fun preloadVideo\(url: String\?\) \{
        if \(url == null\) return
        viewModelScope\.launch \{
            try \{
                val cacheDataSourceFactory = VideoCacheManager\.getCacheDataSourceFactory\(\)
                val mediaItem = MediaItem\.fromUri\(Uri\.parse\(url\)\)
                // Simplified prefetching: just creating the source triggers a partial buffering if configured,
                // For a robust implementation, a CacheWriter should be used to fetch the first 2MB\.
                // Due to Media3 API complexity, we delegate the cache miss resolution to ExoPlayer internally\.
            \} catch \(e: Exception\) \{
                Log\.e\("MainFeedViewModel", "Error preloading", e\)
            \}
        \}
    \}"""

new_prefetch = """    @androidx.media3.common.util.UnstableApi
    fun preloadVideo(url: String?) {
        if (url == null) return
        viewModelScope.launch {
            try {
                // Delegate to our new dedicated VideoPrefetcher which efficiently fetches only 2MB
                VideoPrefetcher.prefetch(url)
            } catch (e: Exception) {
                Log.e("MainFeedViewModel", "Error preloading $url", e)
            }
        }
    }

    // Optional: Expose cancel prefetch if we decide to wire it to UI events
    @androidx.media3.common.util.UnstableApi
    fun cancelPreload(url: String?) {
        VideoPrefetcher.cancelPrefetch(url)
    }"""

content = re.sub(old_prefetch, new_prefetch, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'w') as f:
    f.write(content)
