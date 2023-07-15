package com.pjm.cours.data.remote

import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface ApiClient {

    @POST("user.json")
    suspend fun createUser(
        @Query("auth") auth: String?,
        @Body user: User
    ): Response<Map<String, String>>

    @GET("user/{userId}.json")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String?
    ): Response<User>

    @POST("post.json")
    suspend fun createPost(
        @Query("auth") auth: String?,
        @Body user: Post
    ): Response<Map<String, String>>

    @POST("meeting_member/{postId}.json")
    suspend fun registerMemberMeeting(
        @Path("postId") postId: String,
        @Query("auth") auth: String?,
        @Body user: Map<String, Boolean>
    ): Response<Map<String, String>>

    @PATCH("member_meeting/{userId}.json")
    suspend fun registerMeetingMember(
        @Path("userId") userId: String,
        @Query("auth") auth: String?,
        @Body post: Map<String, Boolean>
    ): Response<Map<String, Boolean>>

    @GET("post.json")
    suspend fun getPosts(
        @Query("auth") auth: String?
    ): Response<Map<String, Post>>

    @GET("post/{postId}.json")
    suspend fun getPost(
        @Path("postId") postId: String?,
        @Query("auth") auth: String?,
    ): Response<Post>

    @PATCH("post/{postId}.json")
    suspend fun updateCurrentMemberCount(
        @Path("postId") postId: String?,
        @Query("auth") auth: String?,
        @Body currentMemberCount: Map<String, String>
    ): Response<Map<String, String>>

    @POST("chat/{postId}/messages.json")
    suspend fun sendMessage(
        @Path("postId") postId: String,
        @Query("auth") auth: String?,
        @Body message: Message
    ): Response<Map<String, String>>

    companion object {

        private const val baseUrl = BuildConfig.BASE_URL

        fun create(): ApiClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ApiClient::class.java)
        }
    }
}