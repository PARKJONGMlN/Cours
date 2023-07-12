package com.pjm.cours.data.model

data class Post(
    val hostUserId: String = "",
    val hostUser: User = User(),
    val title: String = "",
    val body: String = "",
    val limitMemberCount: String = "",
    val currentMemberCount: String = "",
    val location: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val meetingDate: String = "",
    val createdAt: String = "",
    val category: String = "",
    val language: String = "",
    val key: String = "",
)
