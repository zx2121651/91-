import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update CameraScreen to pass the joined URIs
old_camera = """onNavigateToEdit = { videoUri ->
                        navController.navigate("video_edit/${java.net.URLEncoder.encode(videoUri, "UTF-8")}/false")
                    }"""

new_camera = """onNavigateToEdit = { videoUris ->
                        val joinedUris = videoUris.joinToString(",")
                        navController.navigate("video_edit/${java.net.URLEncoder.encode(joinedUris, "UTF-8")}/false")
                    }"""

content = content.replace(old_camera, new_camera)

# No change needed for VideoEditScreen routing, it will just receive a comma separated string.
# But we need to update VideoEditScreen itself to handle it.

with open(file_path, "w") as f:
    f.write(content)
