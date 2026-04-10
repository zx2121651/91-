import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/Network.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update getFeedVideos to support userId
old_api = """    @GET("api/v1/feed/videos")
    suspend fun getFeedVideos(): FeedResponse"""

new_api = """    @GET("api/v1/feed/videos")
    suspend fun getFeedVideos(@Query("userId") userId: String? = null): FeedResponse"""

if "userId: String?" not in content:
    content = content.replace(old_api, new_api)

with open(file_path, "w") as f:
    f.write(content)
