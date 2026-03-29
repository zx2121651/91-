import re

with open('Aurelian/app/src/main/java/com/aurelian/app/FeedRepository.kt', 'r') as f:
    content = f.read()

# Replace the fake network fetch with the real one
search_pattern = r"""        try \{
            // 2\. Simulate high-latency network \(e\.g\., 1\.5s delay to represent heavy backend\)
            Log\.d\("FeedRepository", "Fetching from NETWORK\.\.\."\)
            delay\(1500\)

            val networkData = fetchMockNetworkData\(\)

            // Update cache
            cachedFeed = networkData

            // 3\. Emit fresh network data
            Log\.d\("FeedRepository", "Emitting NETWORK data"\)
            emit\(Result\.success\(networkData\)\)

        \} catch \(e: Exception\) \{"""

replace_pattern = """        try {
            Log.d("FeedRepository", "Fetching from NETWORK via API...")

            // Call the real Express.js backend running on localhost:3000 (10.0.2.2 for Android)
            val response = NetworkClient.apiService.getFeedVideos()
            val networkData = response.data

            // Update cache
            cachedFeed = networkData

            // 3. Emit fresh network data
            Log.d("FeedRepository", "Emitting NETWORK data")
            emit(Result.success(networkData))

        } catch (e: Exception) {"""

content = re.sub(search_pattern, replace_pattern, content)

# Optionally, we can remove the fetchMockNetworkData function to keep it clean, but not strictly necessary

with open('Aurelian/app/src/main/java/com/aurelian/app/FeedRepository.kt', 'w') as f:
    f.write(content)
