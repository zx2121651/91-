package com.aurelian.app

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userId", alternate = ["id"])
    val id: Int,
    val name: String,
    val bio: String,
    val location: String,
    @SerializedName("coverUrl", alternate = ["imageUrl"])
    val imageUrl: String = "",
    val videoUrl: String = ""
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
