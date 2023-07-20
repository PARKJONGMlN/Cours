package com.pjm.cours.data.model

data class PostPreview(
    val postId: String,
    val title: String,
    val hostImageUri: String,
    val currentMemberCount: String,
    val limitMemberCount: String,
    val location: String,
    val latitude: String,
    val longitude: String,
    val category: String,
    val language: String,
    val distance: String = ""
)