package com.aurelian.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit // 传递录制好的视频 URI
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    if (!hasCameraPermission || !hasAudioPermission) {
        Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
            Text("我们需要相机和麦克风权限来拍摄高级短视频", color = Silver)
        }
        return
    }

    // Camera setup
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var recording: Recording? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(lensFacing, previewView) {
            val view = previewView ?: return@LaunchedEffect
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }

        // Top actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
            }
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "翻转镜头", tint = Color.White)
            }
        }

        // Bottom record button (High-end UI)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "recording")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRecording) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color(0xFFE53935) else Color.White)
                    .scale(scale)
                    .clickable {
                        if (isRecording) {
                            recording?.stop()
                            recording = null
                            isRecording = false
                        } else {
                            val videoFile = File(
                                context.cacheDir,
                                SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".mp4"
                            )
                            val outputOptions = FileOutputOptions.Builder(videoFile).build()

                            try {
                                recording = videoCapture?.output
                                    ?.prepareRecording(context, outputOptions)
                                    ?.apply {
                                        if (hasAudioPermission) {
                                            withAudioEnabled()
                                        }
                                    }
                                    ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                        when (recordEvent) {
                                            is VideoRecordEvent.Start -> {
                                                isRecording = true
                                            }
                                            is VideoRecordEvent.Finalize -> {
                                                if (!recordEvent.hasError()) {
                                                    val uri = recordEvent.outputResults.outputUri.toString()
                                                    onNavigateToEdit(uri)
                                                } else {
                                                    recording?.close()
                                                    recording = null
                                                    isRecording = false
                                                    Log.e("CameraScreen", "Video capture ends with error: \${recordEvent.error}")
                                                }
                                            }
                                        }
                                    }
                            } catch (e: SecurityException) {
                                Toast.makeText(context, "缺少权限", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            )
        }
    }
}
