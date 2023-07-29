package com.pjm.cours.data.remote

import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.NotificationBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmClient {

    @Headers("Content-Type: application/json")
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") auth: String = "key=${BuildConfig.FCM_KEY}",
        @Body notification: NotificationBody
    ): ApiResponse<NotificationBody>
}