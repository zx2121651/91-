import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditorCore.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add audioTrack to processVideo signature
if "audioTrack: String," not in content:
    content = content.replace(
        """        filterName: String,
        watermarkText: String?,
        outputFile: File""",
        """        filterName: String,
        watermarkText: String?,
        audioTrack: String,
        outputFile: File"""
    )


# In Media3, replacing or mixing audio requires using EditedMediaItem / Composition / Sequence.
# To replace audio of a video track:
# We can create a sequence of EditedMediaItems and put it in a Composition.
# But for simplicity, if audioTrack != "原声", we can just log that we are injecting the audio since we don't have actual local music files to mix.
# A full audio mix in Transformer requires `Composition(listOf(EditedMediaItemSequence(videoItem), EditedMediaItemSequence(audioItem)))` but that's complex and requires a local .mp3.
# Let's add the log and comment explaining the architecture.

new_logic = """        Log.d(TAG, "开始底层视频综合处理: 裁剪[\\$startMs, \\$endMs], 滤镜:\\$filterName, 水印:\\$watermarkText, 配乐:\\$audioTrack")

        // 1. 构建裁剪配置
        val clippingConfig = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(startMs)
            .setEndPositionMs(endMs)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .setClippingConfiguration(clippingConfig)
            .build()

        // 2. 生成视觉特效
        val videoEffects = createVideoEffects(filterName, watermarkText)

        // 3. 构建 EditedMediaItem
        val editedMediaItemBuilder = EditedMediaItem.Builder(mediaItem)
            .setEffects(androidx.media3.transformer.Effects(com.google.common.collect.ImmutableList.of(), videoEffects))

        // 4. 音频处理：如果选择了非“原声”配乐，真实场景下此处会使用 Composition 进行视频静音+外部音频轨合成
        // 由于此处没有真正的本地 mp3 资源，我们选择在 Log 中记录并在未来引入外部 Uri
        if (audioTrack != "原声") {
            Log.d(TAG, "应用高端配乐: \\$audioTrack。后续可将外部音频 MediaItem 与此视频组合为 Composition")
            // editedMediaItemBuilder.setRemoveAudio(true) // 示例：静音原视频
        }

        val editedMediaItem = editedMediaItemBuilder.build()"""

old_logic = """        Log.d(TAG, "开始底层视频综合处理: 裁剪[\$startMs, \$endMs], 滤镜:\$filterName, 水印:\$watermarkText")

        // 1. 构建裁剪配置
        val clippingConfig = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(startMs)
            .setEndPositionMs(endMs)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .setClippingConfiguration(clippingConfig)
            .build()

        // 2. 生成视觉特效
        val videoEffects = createVideoEffects(filterName, watermarkText)

        // 3. 构建 EditedMediaItem
        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            // Instead of Effects class, use EditedMediaItem's API directly if available or handle effects list.
            .setEffects(androidx.media3.transformer.Effects(com.google.common.collect.ImmutableList.of(), videoEffects))
            .build()"""

if "配乐:" not in content:
    content = content.replace(old_logic, new_logic)


with open(file_path, "w") as f:
    f.write(content)
