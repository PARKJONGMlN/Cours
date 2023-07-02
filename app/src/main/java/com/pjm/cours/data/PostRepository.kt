package com.pjm.cours.data

import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.ApiClient
import com.pjm.cours.util.DateFormat
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class PostRepository(
    private val apiClient: ApiClient,
) {

    suspend fun createPost(
        title: String,
        body: String,
        numberOfMember: String,
        location: String,
        latitude: String,
        longitude: String,
        meetingDate: String,
        category: String,
        language: String
    ): Response<Map<String, String>> {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val currentTime = DateFormat.getCurrentTime()
        return apiClient.createPost(
            idToken,
            Post(
                userEmail = userEmail,
                title = title,
                body = body,
                numberOfMember = numberOfMember,
                location = location,
                latitude = latitude,
                longitude = longitude,
                meetingDate = meetingDate,
                category = category,
                language = language,
                createdAt = currentTime,
            )
        )
    }

    suspend fun getPostList(): Response<Map<String, Post>> {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
        return apiClient.getPosts(idToken)
    }
}