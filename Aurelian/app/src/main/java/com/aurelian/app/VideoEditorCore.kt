package com.aurelian.app

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Contrast

import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.TextOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 视频底层剪辑引擎核心工具类 (对标专业剪辑能力)。
 * 使用 Media3 Transformer API 实现真实的文件级别的视频处理（裁剪、调色、滤镜、贴纸）。
 */
@OptIn(UnstableApi::class)
object VideoEditorCore {

    private const val TAG = "VideoEditorCore"

    /**
     * 生成对应的 Media3 Effect 列表
     */
    fun createVideoEffects(filterName: String, watermarkText: String?): List<Effect> {
        val effects = mutableListOf<Effect>()

        // 1. 滤镜处理
        when (filterName) {
            "黑白(B&W)" -> {
                effects.add(RgbFilter.createGrayscaleFilter())
                effects.add(Contrast(0.5f)) // 增加对比度强化黑白质感
            }
            "胶片(Film)" -> {
                // 模拟胶片高对比度及特定色调偏移 (非完全准确，作为示例)
                effects.add(Contrast(0.3f))
                val colorMatrix = floatArrayOf(
                    1.1f, 0f,   0f,   0f,
                    0f,   0.9f, 0f,   0f,
                    0f,   0f,   0.8f, 0f,
                    0f,   0f,   0f,   1f
                )
                // Note: RgbMatrix 也是一种选择，这里为了简化先用基础的 Contrast
            }
            "电影感" -> {
                effects.add(Contrast(0.2f))
            }
            // "原画"及其他不做处理
        }

        // 2. 文字贴纸/水印处理
        if (!watermarkText.isNullOrBlank()) {
            val spannable = SpannableString(watermarkText)
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#E5D3A1")), // Gold 颜色
                0,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val textOverlay = TextOverlay.createStaticTextOverlay(spannable)
            effects.add(OverlayEffect(com.google.common.collect.ImmutableList.of(textOverlay)))
        }

        return effects
    }

    /**
     * 综合视频处理：支持时长裁剪、添加滤镜、添加文字水印，并最终重新编码导出。
     */
    suspend fun processVideo(
        context: Context,
        inputUri: Uri,
        startMs: Long,
        endMs: Long,
        filterName: String,
        watermarkText: String?,
        outputFile: File
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "开始底层视频综合处理: 裁剪[\$startMs, \$endMs], 滤镜:\$filterName, 水印:\$watermarkText")

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
                    Log.d(TAG, "视频处理完成: \${outputFile.absolutePath}")
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
        transformer.start(editedMediaItem, outputFile.absolutePath)

        // 6. 支持取消
        continuation.invokeOnCancellation {
            transformer.cancel()
            if (outputFile.exists()) {
                outputFile.delete()
            }
        }
    }
}
