import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update videoUri parsing to support multiple uris
# Split the input videoUri by commas and prepare them for ExoPlayer
old_player = """        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            repeatMode = Player.REPEAT_MODE_ALL
            addListener(object : Player.Listener {"""

new_player = """        val player = ExoPlayer.Builder(context).build().apply {
            val uris = videoUri.split(",")
            val mediaItems = uris.map { MediaItem.fromUri(Uri.parse(it)) }
            setMediaItems(mediaItems)
            repeatMode = Player.REPEAT_MODE_ALL
            addListener(object : Player.Listener {"""

if "setMediaItems(mediaItems)" not in content:
    content = content.replace(old_player, new_player)

# In VideoEditScreen 'processVideo' call, pass the whole raw comma-separated string,
# and we will handle splitting inside VideoEditorCore.

with open(file_path, "w") as f:
    f.write(content)
