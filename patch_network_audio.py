import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/Network.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update PublishVideoRequest
old_request = "data class PublishVideoRequest(val title: String, val bio: String, val mediaId: String)"
new_request = "data class PublishVideoRequest(val title: String, val bio: String, val mediaId: String, val audioTrack: String = \"原声\")"

content = content.replace(old_request, new_request)

with open(file_path, "w") as f:
    f.write(content)

file_path2 = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path2, "r") as f:
    content2 = f.read()

# Update API call in VideoEditScreen
old_call = """                                val response = NetworkClient.apiService.publishVideo(
                                    PublishVideoRequest(title, bio, "media_\\${System.currentTimeMillis()}")
                                )"""
new_call = """                                val response = NetworkClient.apiService.publishVideo(
                                    PublishVideoRequest(title, bio, "media_\\${System.currentTimeMillis()}", selectedAudio)
                                )"""

content2 = content2.replace(old_call, new_call)
with open(file_path2, "w") as f:
    f.write(content2)

# Update Backend API
file_path3 = "Aurelian-Backend/src/routes/feed.js"
with open(file_path3, "r") as f:
    content3 = f.read()

old_backend = "const { title, bio, mediaId } = req.body;"
new_backend = "const { title, bio, mediaId, audioTrack } = req.body;"

content3 = content3.replace(old_backend, new_backend)

old_backend_log = "标题: ${title}`)"
new_backend_log = "标题: ${title}, 配乐: ${audioTrack}`)"

content3 = content3.replace(old_backend_log, new_backend_log)

with open(file_path3, "w") as f:
    f.write(content3)
