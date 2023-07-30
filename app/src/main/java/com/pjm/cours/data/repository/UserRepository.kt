package com.pjm.cours.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pjm.cours.data.PreferenceManager
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
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) = flow {
        try {
            val userId = preferenceManager.getString(Constants.USER_ID, "")
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val result = apiClient.getUser(userId, idToken)
            when (result) {
                is ApiResultSuccess -> {
                    emit(
                        result.data.copy(
                            profileUri = getDownLoadImageUri(result.data.profileUri)
                        )
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
        onSuccess()
    }.flowOn(Dispatchers.Default)

    private suspend fun getDownLoadImageUri(hostImageUri: String) =
        imageUriRemoteDataSource.getImageDownLoadUri(hostImageUri).toString()

    fun logOut() {
        preferenceManager.setGoogleIdToken(Constants.KEY_GOOGLE_ID_TOKEN, "")
    }

}