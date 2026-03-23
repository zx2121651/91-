package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    LaunchedEffect(Unit) {
        viewModel.rsvpEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepBlack
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is EventDetailsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold)
                    }
                }
                is EventDetailsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "加载失败", color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = state.message, color = Silver)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadEventDetails(eventId) }) {
                                Text("重试")
                            }
                        }
                    }
                }
                is EventDetailsUiState.Success -> {
                    val details = state.eventDetails

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                            AsyncImage(
                                model = details.coverUrl,
                                contentDescription = "活动头图",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, DeepBlack),
                                            startY = 500f
                                        )
                                    )
                            )
                            // Navigation Icon
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .statusBarsPadding()
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Gold)
                            }

                            // Title & Meta
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(24.dp)
                            ) {
                                Text(details.title, color = Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Silver, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(details.date, color = Silver, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Silver, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(details.location, color = Silver, fontSize = 14.sp)
                                }
                            }
                        }

                        // Content
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("活动详情", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = details.description,
                                color = Silver,
                                fontSize = 16.sp,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Text("着装要求 (Attire Protocol)", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = details.attireProtocol,
                                color = Silver,
                                fontSize = 16.sp,
                                lineHeight = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    // Floating Action Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, DeepBlack)))
                            .padding(24.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = { viewModel.rsvpEvent(eventId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("提交参与申请", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                    }
                }
            }
        }
    }
}
