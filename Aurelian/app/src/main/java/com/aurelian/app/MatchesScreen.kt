package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun MatchesScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: MatchesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is MatchesUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        }
        is MatchesUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "加载失败", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, color = Silver)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchData() }) {
                        Text("重试")
                    }
                }
            }
        }
        is MatchesUiState.Success -> {
            val matches = state.matches.map { it.user }
            val admirersCount = state.admirers.size

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("匹配与心动", color = Gold, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
                    )
                },
                containerColor = DeepBlack
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    // Admirers Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(Color(0xFF1B1B1B), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mock blurred faces
                            Row(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(48.dp).background(Color.Gray, RoundedCornerShape(24.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(48.dp).background(Color.DarkGray, RoundedCornerShape(24.dp)))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${admirersCount} 人", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("正在偷偷关注你", color = Silver, fontSize = 12.sp)
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("搜索匹配...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFF1B1B1B),
                            unfocusedBorderColor = Color(0xFF303030),
                            focusedTextColor = Silver,
                            unfocusedTextColor = Silver
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    Text(
                        text = "私密推荐",
                        color = Silver,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Matches Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(matches) { match ->
                            MatchCard(match, onNavigateToProfile)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(user: User, onNavigateToProfile: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFF1B1B1B), RoundedCornerShape(12.dp))
            .clickable { onNavigateToProfile(user.name) }
    ) {
        AsyncImage(
            model = user.imageUrl,
            contentDescription = "匹配头像",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000)),
                        startY = 150f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(user.name, color = Silver, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(user.location, color = Gold, fontSize = 12.sp)
        }
    }
}
