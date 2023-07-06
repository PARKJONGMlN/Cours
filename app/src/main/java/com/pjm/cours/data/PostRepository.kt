package com.pjm.cours.data

import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.ApiClient
import com.pjm.cours.util.Constants
import com.pjm.cours.util.DateFormat
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class PostRepository(
    private val apiClient: ApiClient,
    private val preferenceManager: PreferenceManager
) {

    suspend fun createPost(
        title: String,
        body: String,
        limitMemberCount: String,
        location: String,
        latitude: String,
        longitude: String,
        meetingDate: String,
        category: String,
        language: String
    ): Response<Map<String, String>> {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
        val userId = preferenceManager.getString(Constants.USER_ID, "")
        val user = apiClient.getUser(auth = idToken, userId = userId).body()!!
        val currentTime = DateFormat.getCurrentTime()
        val result = apiClient.createPost(
            idToken,
            Post(
                hostUserId = userId,
                hostUser = user,
                title = title,
                body = body,
                limitMemberCount = limitMemberCount,
                currentMemberCount = "1",
                location = location,
                latitude = latitude,
                longitude = longitude,
                meetingDate = meetingDate,
                category = category,
                language = language,
                createdAt = currentTime,
            )
        )
        val postId = result.body()?.get("name")
        apiClient.registerMeetingMember(
            userId = userId,
            auth = idToken,
            post = mapOf(postId!! to true)
        )
        return apiClient.registerMemberMeeting(
            postId = postId,
            auth = idToken,
            user = mapOf(userId to true)
        )
    }

    suspend fun registerMember(postId: String, currentMemberCount: String): Response<Map<String, String>> {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
        val userId = preferenceManager.getString(Constants.USER_ID, "")
        val updateCount = currentMemberCount.toInt() + 1
        apiClient.updateCurrentMemberCount(postId = postId, auth = idToken, mapOf("currentMemberCount" to updateCount.toString()))
        apiClient.registerMeetingMember(
            userId = userId,
            auth = idToken,
            post = mapOf(postId to true)
        )
        return apiClient.registerMemberMeeting(
            postId = postId,
            auth = idToken,
            user = mapOf(userId to true)
        )
    }

    suspend fun getPostList(): Response<Map<String, Post>> {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
        return apiClient.getPosts(idToken)
    }

    suspend fun getPost(postId: String): Response<Post> {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
        return apiClient.getPost(postId, idToken)
    }

}