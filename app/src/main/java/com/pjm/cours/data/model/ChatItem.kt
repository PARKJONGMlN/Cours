package com.pjm.cours.data.model

sealed class ChatItem
data class OtherChat(
    val nickname: String,
    val message: String
) : ChatItem()

data class MyChat(
    val message: String,
) : ChatItem()