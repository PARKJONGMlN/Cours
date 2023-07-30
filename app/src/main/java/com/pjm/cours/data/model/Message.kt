package com.pjm.cours.data.model

data class Message(
    val senderUid: String = "",
    val text: String = "",
    val sender: String = "",
    val timestamp: Long = 0
)