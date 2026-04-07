package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var rsvpStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = NetworkClient.apiService.getEvents("ALL")
            events = response.data
        } catch (e: Exception) {
            // Handle error, maybe show a snackbar
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EXCLUSIVE GATHERINGS",
                        color = Gold,
                        fontSize = 12.sp,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Light
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Silver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack,
                    titleContentColor = Gold
                )
            )
        },
        containerColor = DeepBlack
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Gold,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (events.isEmpty()) {
                Text(
                    text = "No upcoming events in your circle.",
                    color = Silver.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(events) { event ->
                        EventCard(
                            event = event,
                            onRsvpClick = { eventId ->
                                coroutineScope.launch {
                                    try {
                                        val response = NetworkClient.apiService.rsvpEvent(eventId, RsvpRequest(1))
                                        rsvpStatus = "Status: ${response.data.status}"
                                    } catch (e: Exception) {
                                        rsvpStatus = "Request Failed"
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Simple RSVP Status Toast/Snackbar alternative for now
            rsvpStatus?.let { status ->
                Snackbar(
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter),
                    containerColor = DeepBlack,
                    contentColor = Gold,
                    shape = RoundedCornerShape(0.dp),
                    action = {
                        TextButton(onClick = { rsvpStatus = null }) {
                            Text("DISMISS", color = Silver)
                        }
                    }
                ) {
                    Text(status)
                }
            }
        }
    }
}

@Composable
fun EventCard(event: EventResponse, onRsvpClick: (String) -> Unit) {
    // In a real app, the coverUrl would come from the API. Mocking it here.
    val mockCoverUrl = if (event.title.contains("Art")) {
        "https://images.unsplash.com/photo-1518998053901-5348d3961a04?auto=format&fit=crop&w=800&q=80"
    } else {
        "https://images.unsplash.com/photo-1515523110800-9415d13b84a8?auto=format&fit=crop&w=800&q=80"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(0.dp)) // Sharp, luxurious border
            .clickable { /* Navigate to Event Details */ }
    ) {
        // Full bleed image
        AsyncImage(
            model = mockCoverUrl,
            contentDescription = event.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient to make text readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DeepBlack.copy(alpha = 0.9f)),
                        startY = 300f
                    )
                )
        )

        // Text Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = event.date.take(10), // Simplistic date formatting
                color = Gold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.title,
                color = Silver,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onRsvpClick(event.eventId) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("REQUEST INVITATION", fontSize = 10.sp, letterSpacing = 1.5.sp)
            }
        }
    }
}
