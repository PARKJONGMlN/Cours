package com.pjm.cours.data.model

sealed class ChatItem {
    abstract val postId: String
    abstract val messageId: String
    abstract val sender: String
    abstract val text: String
    abstract val sendDate: String
}

data class OtherChat(
    override val postId: String = "",
    override val messageId: String = "",
    override val sender: String = "",
    override val text: String = "",
    override val sendDate: String = ""
) : ChatItem()

data class MyChat(
    override val postId: String = "",
    override val messageId: String = "",
    override val sender: String = "",
    override val text: String = "",
    override val sendDate: String = ""
) : ChatItem()