package com.pjm.cours.data.remote

import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.model.User
import retrofit2.http.*

interface ApiClient {

    @POST("user.json")
    suspend fun createUser(
        @Query("auth") auth: String?,
        @Body user: User
    ): ApiResponse<Map<String, String>>

    @GET("user/{userId}.json")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String?
    ): ApiResponse<User>

    @POST("post.json")
    suspend fun createPost(
        @Query("auth") auth: String?,
        @Body user: Post
    ): ApiResponse<Map<String, String>>

    @POST("meeting_member/{postId}.json")
    suspend fun registerMemberMeeting(
        @Path("postId") postId: String,
        @Query("auth") auth: String?,
        @Body user: Map<String, Boolean>
    ): ApiResponse<Map<String, String>>

    @PATCH("member_meeting/{userId}.json")
    suspend fun registerMeetingMember(
        @Path("userId") userId: String,
        @Query("auth") auth: String?,
        @Body post: Map<String, Boolean>
    ): ApiResponse<Map<String, Boolean>>

    @GET("post.json")
    suspend fun getPosts(
        @Query("auth") auth: String?
    ): ApiResponse<Map<String, Post>>

    @GET("post/{postId}.json")
    suspend fun getPost(
        @Path("postId") postId: String?,
        @Query("auth") auth: String?,
    ): ApiResponse<Post>

    @PATCH("post/{postId}.json")
    suspend fun updateCurrentMemberCount(
        @Path("postId") postId: String?,
        @Query("auth") auth: String?,
        @Body currentMemberCount: Map<String, String>
    ): ApiResponse<Map<String, String>>

    @POST("chat/{postId}/messages.json")
    suspend fun sendMessage(
        @Path("postId") postId: String,
        @Query("auth") auth: String?,
        @Body message: Message
    ): ApiResponse<Map<String, String>>
}