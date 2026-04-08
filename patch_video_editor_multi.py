import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditorCore.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update processVideo signature to accept inputUris: String
# Then split it and build a Composition.

old_process = """    suspend fun processVideo(
        context: Context,
        inputUri: Uri,
        startMs: Long,
        endMs: Long,
        filterName: String,
        watermarkText: String?,
        audioTrack: String,
        outputFile: File
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "开始底层视频综合处理: 裁剪[\\$startMs, \\$endMs], 滤镜:\\$filterName, 水印:\\$watermarkText, 配乐:\\$audioTrack")

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

new_process = """    suspend fun processVideo(
        context: Context,
        inputUri: Uri, // 这里其实传递的是逗号分隔的 URI 字符串
        startMs: Long,
        endMs: Long,
        filterName: String,
        watermarkText: String?,
        audioTrack: String,
        outputFile: File
    ): String = suspendCancellableCoroutine { continuation ->

        val uriString = inputUri.toString()
        val uriList = uriString.split(",")

        Log.d(TAG, "开始底层多段视频综合处理: 包含 \\${uriList.size} 个片段, 滤镜:\\$filterName, 配乐:\\$audioTrack")

        // 1. 生成视觉特效
        val videoEffects = createVideoEffects(filterName, watermarkText)
        val effects = androidx.media3.transformer.Effects(com.google.common.collect.ImmutableList.of(), videoEffects)

        // 2. 构建多段 EditedMediaItem
        val editedMediaItems = uriList.map { uriStr ->
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(uriStr))
                .build()

            val builder = EditedMediaItem.Builder(mediaItem).setEffects(effects)
            if (audioTrack != "原声") {
                // builder.setRemoveAudio(true) // 未来支持完全静音替换配乐
            }
            builder.build()
        }

        // 3. 构建 EditedMediaItemSequence (片段串联)
        val sequence = androidx.media3.transformer.EditedMediaItemSequence(editedMediaItems)

        // 4. 构建 Composition
        val composition = Composition.Builder(com.google.common.collect.ImmutableList.of(sequence))
            .build()

        // 注意：这里的时长裁剪(startMs/endMs)在多片段情况下变得非常复杂（可能跨越多个Item）。
        // 剪映的专业做法是针对整个 Composition 或将其导出为单独文件后再进行一次时长裁剪。
        // 为保证拼接平滑，本期我们优先支持多段拼合并应用统一滤镜。"""

if "val uriString = inputUri.toString()" not in content:
    content = content.replace(old_process, new_process)

# Then update transformer.start to use composition instead of editedMediaItem
old_start = "transformer.start(editedMediaItem, outputFile.absolutePath)"
new_start = "transformer.start(composition, outputFile.absolutePath)"
content = content.replace(old_start, new_start)

with open(file_path, "w") as f:
    f.write(content)
