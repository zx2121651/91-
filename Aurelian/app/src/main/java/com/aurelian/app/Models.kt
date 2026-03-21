package com.aurelian.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

data class User(
    val id: Int,
    val name: String,
    val bio: String,
    val location: String
)

data class Message(
    val id: Int,
    val sender: User,
    val content: String,
    val timestamp: String
)

data class Event(
    val id: Int,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String,
    val dressCode: String
)
