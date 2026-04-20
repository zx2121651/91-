import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# We need to add setVideoEffects to the ExoPlayer.
# In Media3, player.setVideoEffects(...) is available if we use the same Effects.
# Let's add an effect update block:
effect_update = """
    // 实时更新播放器的滤镜效果
    LaunchedEffect(selectedFilter) {
        exoPlayer?.let { player ->
            val effects = VideoEditorCore.createVideoEffects(selectedFilter, null) // 预览时不加文字水印，仅滤镜
            player.setVideoEffects(effects)
        }
    }
"""

if "实时更新播放器的滤镜效果" not in content:
    content = content.replace("LaunchedEffect(sliderRange) {", effect_update + "\n    LaunchedEffect(sliderRange) {")


with open(file_path, "w") as f:
    f.write(content)
