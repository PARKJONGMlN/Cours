package com.pjm.cours.data.model

data class NotificationBody(
    val to: String,
    val data: NotificationData
) {
    data class NotificationData(
        val title: String,
        val sender: String,
        val message: String,
        val chatRoomId: String,
    )
}