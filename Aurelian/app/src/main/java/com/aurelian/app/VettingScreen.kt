package com.aurelian.app

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VettingScreen(
    initialType: String = "ASSETS", // "ASSETS" or "IDENTITY"
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }

    // State for selected images (MOCK)
    val selectedImages = remember { mutableStateListOf<Uri>() }

    // Status state
    var vettingStatus by remember { mutableStateOf<List<VettingStatusItem>?>(null) }
    var isLoadingStatus by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages.addAll(uris)
    }

    LaunchedEffect(Unit) {
        try {
            val response = NetworkClient.apiService.getVettingStatus()
            vettingStatus = response.data
        } catch (e: Exception) {
            // Ignore for now
        } finally {
            isLoadingStatus = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高定会员认证", color = Silver, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Silver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {

            // 顶部的当前状态提示区
            if (isLoadingStatus) {
                CircularProgressIndicator(color = Gold, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val pendingReq = vettingStatus?.find { it.status == "PENDING" }
                val approvedReq = vettingStatus?.find { it.status == "APPROVED" }
                val rejectedReq = vettingStatus?.find { it.status == "REJECTED" }

                if (pendingReq != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF332B14), RoundedCornerShape(8.dp))
                            .border(1.dp, Gold, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "尊贵的会员，您提交的 [\${if(pendingReq.type == "ASSETS") "资产审核" else "身份认证"}] 正在由您的专属管家处理中。通常会在 24 小时内完成。请耐心等待。",
                            color = Gold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (approvedReq != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF14331C), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "恭喜！您的认证材料已通过。AURELIAN NIGHT 为您敞开大门。",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (rejectedReq != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF331414), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "很抱歉，您的上一次审核未通过。\n原因: \${rejectedReq.rejectReason ?: "材料不清晰或不符合高定门槛。"}\n您可以重新补充资料提交。",
                            color = Color(0xFFEF9A9A),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 如果当前没有 PENDING 的请求，展示提交流程
            val hasPending = vettingStatus?.any { it.status == "PENDING" } == true

            if (!hasPending) {
                Text("申请类型", color = Silver, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { selectedType = "ASSETS" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "ASSETS") Gold else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("资产审核", color = if (selectedType == "ASSETS") DeepBlack else Silver)
                    }
                    Button(
                        onClick = { selectedType = "IDENTITY" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "IDENTITY") Gold else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("身份认证", color = if (selectedType == "IDENTITY") DeepBlack else Silver)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("请上传相关凭证 (例如：房产证、行驶证、护照等)", color = Silver, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(selectedImages) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Document",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        )
                    }
                    item {
                        // 添加图片按钮
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                .clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加", tint = Silver, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("补充说明 (选填)", color = Silver.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (selectedImages.isEmpty()) {
                            Toast.makeText(context, "请至少上传一张证明材料", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                // 实际场景需要先将 Uri 上传至 S3 获得真实 URL
                                // 这里 MOCK 转换
                                val mockUrls = selectedImages.map { "https://example.com/mock_doc_\${System.currentTimeMillis()}.jpg" }

                                val response = NetworkClient.apiService.submitAssets(
                                    SubmitAssetsRequest(mockUrls, selectedType, notes)
                                )

                                Toast.makeText(context, response.status, Toast.LENGTH_LONG).show()

                                // 重新刷新状态
                                val newStatus = NetworkClient.apiService.getVettingStatus()
                                vettingStatus = newStatus.data
                            } catch (e: Exception) {
                                Toast.makeText(context, "提交失败: \${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = DeepBlack, modifier = Modifier.size(24.dp))
                    } else {
                        Text("提交审核", color = DeepBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
