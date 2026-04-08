package com.aurelian.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (List<String>) -> Unit // 传递多个录制好的视频 URI
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    if (!hasCameraPermission || !hasAudioPermission) {
        Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
            Text("我们需要相机和麦克风权限来拍摄高级短视频", color = Silver)
        }
        return
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var recording: Recording? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // 断点续拍相关状态
    val videoClips = remember { mutableStateListOf<String>() }
    var totalRecordedTimeMs by remember { mutableStateOf(0L) }
    var currentClipStartTime by remember { mutableStateOf(0L) }
    val MAX_DURATION_MS = 15000L // 最大拍摄15秒

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    // 更新当前录制时间
    LaunchedEffect(isRecording) {
        if (isRecording) {
            currentClipStartTime = System.currentTimeMillis()
            while (isRecording) {
                delay(100) // 每100ms更新一次
                val currentClipTime = System.currentTimeMillis() - currentClipStartTime
                if (totalRecordedTimeMs + currentClipTime >= MAX_DURATION_MS) {
                    // 到达最大时长，自动停止
                    recording?.stop()
                    recording = null
                    isRecording = false
                    totalRecordedTimeMs = MAX_DURATION_MS
                    break
                }
            }
        } else {
            if (currentClipStartTime > 0) {
                totalRecordedTimeMs += (System.currentTimeMillis() - currentClipStartTime)
                currentClipStartTime = 0L
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView = it }
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(lensFacing, previewView) {
            val view = previewView ?: return@LaunchedEffect
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
            val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }

        // Top UI: 进度条 + 控制器
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            // 分段进度条 (高级 UI)
            val currentClipTime = if (isRecording) System.currentTimeMillis() - currentClipStartTime else 0L
            val progress = ((totalRecordedTimeMs + currentClipTime).toFloat() / MAX_DURATION_MS.toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress)
                        .background(Gold)
                )
                // TODO: 可以根据 videoClips 的时长绘制分隔线
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                }
                if (!isRecording) {
                    IconButton(onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "翻转镜头", tint = Color.White)
                    }
                }
            }
        }

        // Bottom UI: 回删、录制、完成
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 60.dp, start = 32.dp, end = 32.dp)
        ) {
            // 回删按钮 (录制了片段且未在录制中时显示)
            if (videoClips.isNotEmpty() && !isRecording) {
                IconButton(
                    onClick = {
                        if (videoClips.isNotEmpty()) {
                            // 实际场景应读取文件时长，此处简单均分减去或维护一个时长列表
                            // 为了简化，我们只移除列表最后一项并粗略减少时长（理想情况应该有一个 Clip 数据类）
                            val removedUri = videoClips.removeLast()
                            // Mock 时长扣除：真实场景应解析媒体文件真实时长
                            totalRecordedTimeMs -= (totalRecordedTimeMs / (videoClips.size + 1))
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "回删", tint = Silver, modifier = Modifier.size(32.dp))
                }
            }

            // 录制按钮 (长按录制)
            val infiniteTransition = rememberInfiniteTransition(label = "recording")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRecording) 1.2f else 1f,
                animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color(0xFFE53935) else Color.White)
                    .scale(scale)
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (totalRecordedTimeMs < MAX_DURATION_MS && !isRecording) {
                                    val videoFile = File(context.cacheDir, "clip_" + SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(System.currentTimeMillis()) + ".mp4")
                                    val outputOptions = FileOutputOptions.Builder(videoFile).build()
                                    try {
                                        recording = videoCapture?.output
                                            ?.prepareRecording(context, outputOptions)
                                            ?.apply { if (hasAudioPermission) withAudioEnabled() }
                                            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                                when (recordEvent) {
                                                    is VideoRecordEvent.Start -> isRecording = true
                                                    is VideoRecordEvent.Finalize -> {
                                                        if (!recordEvent.hasError()) {
                                                            videoClips.add(recordEvent.outputResults.outputUri.toString())
                                                        } else {
                                                            Log.e("CameraScreen", "Video capture error: \${recordEvent.error}")
                                                        }
                                                    }
                                                }
                                            }
                                    } catch (e: SecurityException) {
                                        Toast.makeText(context, "缺少权限", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                if (isRecording) {
                                    recording?.stop()
                                    recording = null
                                    isRecording = false
                                }
                                true
                            }
                            else -> false
                        }
                    }
            )

            // 完成按钮
            if (videoClips.isNotEmpty() && !isRecording) {
                IconButton(
                    onClick = { onNavigateToEdit(videoClips.toList()) },
                    modifier = Modifier.align(Alignment.CenterEnd).background(Gold, CircleShape).padding(4.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "完成", tint = DeepBlack, modifier = Modifier.size(28.dp))
                }
            }
        }

        // 提示文本
        if (videoClips.isEmpty() && !isRecording) {
            Text(
                "长按拍摄",
                color = Silver,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            )
        }
    }
}
