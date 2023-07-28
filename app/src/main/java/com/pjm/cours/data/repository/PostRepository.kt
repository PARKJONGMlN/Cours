package com.pjm.cours.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.local.dao.ChatPreviewDao
import com.pjm.cours.data.local.entities.ChatPreviewEntity
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.*
import com.pjm.cours.util.Constants
import com.pjm.cours.util.Constants.LATITUDE
import com.pjm.cours.util.Constants.LONGITUDE
import com.pjm.cours.util.DateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await
import net.daum.mf.map.api.MapPoint
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val preferenceManager: PreferenceManager,
    private val imageUriRemoteDataSource: ImageUriDataSource,
    private val chatPreviewDao: ChatPreviewDao,
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
            apiClient.addMemberMeetingList(
                userId = userId,
                auth = idToken,
                post = mapOf(postId!! to true)
            )
            apiClient.addMeetingMemberList(
                postId = postId,
                auth = idToken,
                user = mapOf(userId to true)
            )
            result
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
            val result = apiClient.updateCurrentMemberCount(
                postId = postId,
                auth = idToken,
                mapOf("currentMemberCount" to updateCount.toString())
            )
            apiClient.addMemberMeetingList(
                userId = userId,
                auth = idToken,
                post = mapOf(postId to true)
            )
            apiClient.addMeetingMemberList(
                postId = postId,
                auth = idToken,
                user = mapOf(userId to true)
            )
            result
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    fun getPostList(
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) = flow {
        try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val result = apiClient.getPosts(idToken)
            when (result) {
                is ApiResultSuccess -> {
                    val deferredPosts = coroutineScope {
                        result.data.map { ResponseResult ->
                            async {
                                ResponseResult.value.copy(
                                    key = ResponseResult.key,
                                    hostUser = ResponseResult.value.hostUser.copy(
                                        profileUri = getDownLoadImageUri(ResponseResult.value.hostUser.profileUri)
                                    )
                                )
                            }
                        }
                    }
                    emit(deferredPosts.awaitAll().sortedByDescending { it.meetingDate })
                }
                is ApiResultError -> {
                    onError()
                }
                is ApiResultException -> {
                    onError()
                }
            }
        } catch (e: Exception) {
            onError()
        }
    }.onCompletion {
        onSuccess()
    }.flowOn(Dispatchers.Default)

    suspend fun getPost(postId: String): ApiResponse<Post> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            apiClient.getPost(postId, idToken)
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    fun setUserCurrentPoint(currentMapPoint: MapPoint) {
        val mapPoint = currentMapPoint.mapPointGeoCoord
        preferenceManager.setUserCurrentPoint(
            LATITUDE,
            mapPoint.latitude.toString(),
            LONGITUDE,
            mapPoint.longitude.toString()
        )
    }

    private suspend fun getDownLoadImageUri(hostImageUri: String) =
        imageUriRemoteDataSource.getImageDownLoadUri(hostImageUri).toString()

    fun getUserCurrentPoint(): MapPoint? = preferenceManager.getUserCurrentPoint()


    fun getChatPreviewList(): Flow<List<ChatPreviewEntity>> {
        return chatPreviewDao.getPreviewList()
    }
}