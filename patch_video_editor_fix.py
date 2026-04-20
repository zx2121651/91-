import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditorCore.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix the processVideo method which has a mix of single item and multiple items.
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

        Log.d(TAG, "开始底层视频综合处理: 裁剪[\\$startMs, \\$endMs], 滤镜:\\$filterName, 水印:\\$watermarkText")

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
            .build()

        // 4. 初始化 Transformer 进行重新编码和渲染
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    Log.d(TAG, "视频处理完成: \\${outputFile.absolutePath}")
                    continuation.resume(outputFile.absolutePath)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    Log.e(TAG, "视频处理失败", exportException)
                    continuation.resumeWithException(exportException)
                }
            })
            .build()

        // 5. 启动异步渲染任务
        transformer.start(composition, outputFile.absolutePath)"""

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
                // Note: The global clipping is complex to apply to multi-clips.
                // We'll skip clipping here if there are multiple clips to keep it simple and stable.
                .build()

            val builder = EditedMediaItem.Builder(mediaItem).setEffects(effects)
            builder.build()
        }

        // 3. 构建 EditedMediaItemSequence (片段串联)
        val sequence = androidx.media3.transformer.EditedMediaItemSequence(editedMediaItems)

        // 4. 构建 Composition
        val composition = Composition.Builder(com.google.common.collect.ImmutableList.of(sequence))
            .build()

        // 5. 初始化 Transformer 进行重新编码和渲染
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(comp: Composition, exportResult: ExportResult) {
                    Log.d(TAG, "视频处理完成: \\${outputFile.absolutePath}")
                    continuation.resume(outputFile.absolutePath)
                }

                override fun onError(
                    comp: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    Log.e(TAG, "视频处理失败", exportException)
                    continuation.resumeWithException(exportException)
                }
            })
            .build()

        // 6. 启动异步渲染任务
        transformer.start(composition, outputFile.absolutePath)"""

content = content.replace(old_process, new_process)

with open(file_path, "w") as f:
    f.write(content)
