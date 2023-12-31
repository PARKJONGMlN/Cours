package com.pjm.cours.data.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.local.dao.ChatPreviewDao
import com.pjm.cours.data.local.dao.MessageDao
import com.pjm.cours.data.model.User
import com.pjm.cours.data.remote.*
import com.pjm.cours.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val preferenceManager: PreferenceManager,
    private val imageUriRemoteDataSource: ImageUriDataSource,
    private val chatPreviewDao: ChatPreviewDao,
    private val messageDao: MessageDao,
) {

    fun getGoogleIdToken() = preferenceManager.getString(Constants.KEY_GOOGLE_ID_TOKEN, "")

    fun saveGoogleIdToken(idToken: String) {
        preferenceManager.setGoogleIdToken(Constants.KEY_GOOGLE_ID_TOKEN, idToken)
    }

    fun saveUserId(userId: String) {
        preferenceManager.setUserId(Constants.USER_ID, userId)
    }

    suspend fun addUser(uid: String, user: User): ApiResponse<Map<String, String>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val uri = uploadImage(user.profileUri.toUri())
            apiClient.createUser(uid, idToken, User(uri, user.nickname, user.intro, user.email))
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun setUserFcmToken(fcmToken: String): ApiResponse<Map<String, String>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val result = apiClient.upDateUser(userId, idToken, mapOf("fcmToken" to fcmToken))
            result
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val email = FirebaseAuth.getInstance().currentUser?.email
        val location = "image/${email}_${System.currentTimeMillis()}"
        val imageRef = storageRef.child(location)
        imageRef.putFile(uri).await()
        return location
    }

    suspend fun getUserInfo(): ApiResponse<User> {
        return try {
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            apiClient.getUser(userId, idToken)
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    fun getUserInfo(
        onComplete: () -> Unit,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) = flow {
        try {
            preferenceManager.setUserId(
                Constants.USER_ID,
                FirebaseAuth.getInstance().currentUser?.uid ?: "",
            )
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val result = apiClient.getUser(userId, idToken)
            when (result) {
                is ApiResultSuccess -> {
                    emit(
                        result.data.copy(
                            profileUri = getDownLoadImageUri(result.data.profileUri),
                        ),
                    )
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
        onComplete()
    }.flowOn(Dispatchers.Default)

    private suspend fun getDownLoadImageUri(hostImageUri: String) =
        imageUriRemoteDataSource.getImageDownLoadUri(hostImageUri).toString()

    suspend fun logOut() {
        preferenceManager.setGoogleIdToken(Constants.KEY_GOOGLE_ID_TOKEN, "")
        chatPreviewDao.deleteAll()
        messageDao.deleteAll()
    }

    suspend fun deleteAccount(): Boolean {
        try {
            preferenceManager.setGoogleIdToken(Constants.KEY_GOOGLE_ID_TOKEN, "")
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val result = apiClient.getMemberMeetingList(userId, idToken)
            when (result) {
                is ApiResultSuccess -> {
                    isMeetingJoined(result, idToken, userId)
                    return true
                }

                is ApiResultException -> {
                    return false
                }

                is ApiResultError -> {
                    return if (result.code in 200..299) {
                        deleteLocalDB(userId, idToken)
                        true
                    } else {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "Exception: $e")
            return false
        }
    }

    private suspend fun isMeetingJoined(
        result: ApiResultSuccess<Map<String, Boolean>>,
        idToken: String?,
        userId: String,
    ) {
        val memberMeetingIdList = result.data.keys
        for (postId in memberMeetingIdList) {
            val result = apiClient.getPost(postId, idToken)
            when (result) {
                is ApiResultSuccess -> {
                    val updateMemberCount = result.data.currentMemberCount.toInt() - 1
                    apiClient.updateCurrentMemberCount(
                        postId,
                        idToken,
                        mapOf("currentMemberCount" to updateMemberCount.toString()),
                    )
                }

                is ApiResultException -> {
                }

                is ApiResultError -> {
                }
            }
            apiClient.deleteMeetingMember(postId, userId, idToken)
            apiClient.deleteMemberMeeting(userId, postId, idToken)
        }
        deleteLocalDB(userId, idToken)
    }

    private suspend fun deleteLocalDB(userId: String, idToken: String?) {
        chatPreviewDao.deleteAll()
        messageDao.deleteAll()
        deleteFirebaseAuth(userId, idToken)
    }

    private suspend fun deleteFirebaseAuth(userId: String, idToken: String?) {
        apiClient.deleteUser(userId, idToken)
    }
}
