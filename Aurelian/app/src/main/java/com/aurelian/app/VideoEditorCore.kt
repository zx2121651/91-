package com.aurelian.app

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
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
 * 视频底层剪辑引擎核心工具类。
 * 使用 Media3 Transformer API 实现真实的文件级别的视频处理（如裁剪）。
 */
@OptIn(UnstableApi::class)
object VideoEditorCore {

    private const val TAG = "VideoEditorCore"

    /**
     * 对给定的视频进行时长裁剪。
     *
     * @param context 上下文
     * @param inputUri 原始视频的 Uri
     * @param startMs 裁剪起始时间（毫秒）
     * @param endMs 裁剪结束时间（毫秒）
     * @param outputFile 输出的目标文件路径
     * @return 返回处理成功后的文件绝对路径，如果失败则抛出异常
     */
    suspend fun trimVideo(
        context: Context,
        inputUri: Uri,
        startMs: Long,
        endMs: Long,
        outputFile: File
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "开始底层视频裁剪: 起始 \$startMs ms, 结束 \$endMs ms")

        // 1. 构建带有 Clipping (裁剪) 信息的 MediaItem
        val mediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .build()

        val editedMediaItem = EditedMediaItem.Builder(mediaItem).build()

        // 2. 初始化 Media3 Transformer
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    Log.d(TAG, "视频裁剪完成: \${outputFile.absolutePath}")
                    continuation.resume(outputFile.absolutePath)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    Log.e(TAG, "视频裁剪失败", exportException)
                    continuation.resumeWithException(exportException)
                }
            })
            .build()

        // 3. 开始执行异步导出任务
        transformer.start(editedMediaItem, outputFile.absolutePath)

        // 4. 支持协程取消操作
        continuation.invokeOnCancellation {
            transformer.cancel()
            if (outputFile.exists()) {
                outputFile.delete()
            }
        }
    }
}
