package com.pjm.cours.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.model.User
import com.pjm.cours.data.remote.ApiClient
import com.pjm.cours.data.remote.ApiResponse
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.util.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val preferenceManager: PreferenceManager
) {

    fun getGoogleIdToken() = preferenceManager.getString(Constants.KEY_GOOGLE_ID_TOKEN, "")


    fun saveGoogleIdToken(idToken: String) {
        preferenceManager.setGoogleIdToken(Constants.KEY_GOOGLE_ID_TOKEN, idToken)
    }

    fun saveUserId(userId: String) {
        preferenceManager.setUserId(Constants.USER_ID, userId)
    }

    suspend fun addUser(user: User): ApiResponse<Map<String, String>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val uri = uploadImage(user.profileUri.toUri())
            apiClient.createUser(idToken, User(uri, user.nickname, user.intro, user.email))
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
}