package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // In a real app, you would fetch profile data from NetworkClient.apiService.getProfile(userId)
    // For this demo, we use high-end mock data matching the VIP feel.
    val isMe = userId == "me"
    val mockName = if (isMe) "Alexandre R." else "Eleanor V."
    val mockLocation = if (isMe) "Monaco" else "Paris, France"
    val mockBio = "Curating the finest moments across the globe. Private Equity | Collector."
    val mockCoverUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=800&q=80"

    val mockVideos = listOf(
        "https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1520443240718-fce21901db79?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1533254181816-17b5f1cb0e4c?auto=format&fit=crop&w=400&q=80"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "THE CONCIERGE",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header: Cover Photo & Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Background Cover
                AsyncImage(
                    model = mockCoverUrl,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Dark Gradient overlay to blend into background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DeepBlack),
                                startY = 200f
                            )
                        )
                )

                // Avatar (Overlap)
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-20).dp)
                        .clip(CircleShape)
                        .border(2.dp, Gold, CircleShape)
                )
            }

            // Info Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mockName,
                        color = Silver,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Elite",
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mockLocation.uppercase(),
                    color = Gold.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mockBio,
                    color = Silver.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                if (!isMe) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { /* Handle exclusive invite */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold.copy(alpha = 0.15f),
                                contentColor = Gold
                            ),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("REQUEST MEET", letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { /* Direct message */ },
                            modifier = Modifier
                                .size(48.dp)
                                .background(DeepBlack)
                                .border(1.dp, Gold.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = "Message", tint = Gold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { /* Edit Profile */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Silver),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Silver.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("MANAGE PROFILE", letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Video Grid (Portfolio)
            Text(
                text = "PORTFOLIO",
                color = Gold,
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(mockVideos.size) { index ->
                    AsyncImage(
                        model = mockVideos[index],
                        contentDescription = "Video Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(0.75f) // Slightly taller than square for video vibe
                            .background(Color.DarkGray)
                            .clickable { /* Play video in full screen */ }
                    )
                }
            }
        }
    }
}
