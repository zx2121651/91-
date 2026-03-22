package com.aurelian.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateToEventDetails: (String) -> Unit,
    viewModel: EventsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is EventsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        }
        is EventsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "加载失败", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, color = Silver)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchEvents() }) {
                        Text("重试")
                    }
                }
            }
        }
        is EventsUiState.Success -> {
            val events = state.events

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("沙龙与晚宴", color = Gold, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
                    )
                },
                containerColor = DeepBlack
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text(
                            text = "近期高端局",
                            color = Silver,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(events) { event ->
                        EventCard(event = event, onNavigateToEventDetails = onNavigateToEventDetails)
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: EventResponse, onNavigateToEventDetails: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToEventDetails(event.eventId) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                // Since EventResponse might not have imageUrl (based on API doc), we use a placeholder or check if added
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1574096079513-d8259312b785?auto=format&fit=crop&w=800&q=80",
                    contentDescription = "Event Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000)),
                                startY = 100f
                            )
                        )
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(event.title, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.date, color = Silver, fontSize = 14.sp)
                }
            }
        }
    }
}
