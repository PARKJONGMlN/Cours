package com.pjm.cours.data.remote

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class ImageUriDataSource {

    private val storage = Firebase.storage.reference

    suspend fun getImageDownLoadUri(imageUri: String): Uri {
        return storage.child(imageUri).downloadUrl.await()
    }
}