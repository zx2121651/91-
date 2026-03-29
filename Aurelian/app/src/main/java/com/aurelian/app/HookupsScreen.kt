package com.aurelian.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val cities = listOf("全部", "Shanghai", "Beijing", "Shenzhen")
private val intents = listOf("全部", "Tonight", "Weekend")

@Composable
fun HookupsScreen(viewModel: HookupsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HookupsUiState.Loading -> {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("正在匹配可约会对象…", style = MaterialTheme.typography.titleMedium)
            }
        }

        is HookupsUiState.Error -> {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("加载失败：${state.message}")
                Button(onClick = { viewModel.fetchCards(reset = true) }) {
                    Text("重试")
                }
            }
        }

        is HookupsUiState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("速约筛选", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        cities.forEach { city ->
                            val selected = (state.selectedCity ?: "全部") == city
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.applyFilters(if (city == "全部") null else city, state.selectedIntent) },
                                label = { Text(city) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        intents.forEach { intent ->
                            val selected = (state.selectedIntent ?: "全部") == intent
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.applyFilters(state.selectedCity, if (intent == "全部") null else intent) },
                                label = { Text(intent) }
                            )
                        }
                    }
                }

                items(state.cards) { card ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(card.name, style = MaterialTheme.typography.titleMedium)
                            Text("${card.city} · ${card.intent} · ${card.age}岁")
                            Text(card.bio)
                            Text("标签：${card.tags.joinToString(" / ")}")

                            val status = state.requestStates[card.userId]
                            if (status != null) {
                                Text("当前状态：$status", style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.sendRequest(card.userId, "今晚见吗？") }) {
                                    Text("发起邀约")
                                }
                                Button(onClick = { viewModel.sendRequest(card.userId, "周末喝一杯？") }) {
                                    Text("周末邀约")
                                }
                            }
                        }
                    }
                }

                if (state.nextCursor != null) {
                    item {
                        Button(onClick = { viewModel.loadMore() }, modifier = Modifier.fillMaxWidth()) {
                            Text("加载更多")
                        }
                    }
                }
            }
        }
    }
}
