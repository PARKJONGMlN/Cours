package com.pjm.cours.data.model

data class ChatPreview(
    val postId: String = "",
    val hostImageUri: String = "",
    val postTitle: String = "",
    val lastMessage: String = "",
    val unReadMessageCount: String = "",
    val messageDate: String = ""
)