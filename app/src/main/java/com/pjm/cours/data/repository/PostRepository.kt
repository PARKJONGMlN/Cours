package com.pjm.cours.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.ApiClient
import com.pjm.cours.data.remote.ApiResponse
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.util.Constants
import com.pjm.cours.util.DateFormat
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepository @Inject constructor(
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
    ): ApiResponse<Map<String, String>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val user = (apiClient.getUser(auth = idToken, userId = userId) as ApiResultSuccess).data
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
            val postId = (result as ApiResultSuccess).data["name"]
            apiClient.registerMeetingMember(
                userId = userId,
                auth = idToken,
                post = mapOf(postId!! to true)
            )
            apiClient.registerMemberMeeting(
                postId = postId,
                auth = idToken,
                user = mapOf(userId to true)
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun addMember(
        postId: String,
        currentMemberCount: String
    ): ApiResponse<Map<String, String>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val updateCount = currentMemberCount.toInt() + 1
            apiClient.updateCurrentMemberCount(
                postId = postId,
                auth = idToken,
                mapOf("currentMemberCount" to updateCount.toString())
            )
            apiClient.registerMeetingMember(
                userId = userId,
                auth = idToken,
                post = mapOf(postId to true)
            )
            apiClient.registerMemberMeeting(
                postId = postId,
                auth = idToken,
                user = mapOf(userId to true)
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }

    }

    suspend fun getPostList(): ApiResponse<Map<String, Post>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            apiClient.getPosts(idToken)
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun getPost(postId: String): ApiResponse<Post> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            apiClient.getPost(postId, idToken)
        } catch (e: Exception) {
            ApiResultException(e)
        }

    }

}