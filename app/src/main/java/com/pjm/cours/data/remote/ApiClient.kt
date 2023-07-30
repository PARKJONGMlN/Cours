package com.pjm.cours.data.remote

import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.model.User
import retrofit2.http.*

interface ApiClient {

    @DELETE("meeting_member/{chatRoomId}/{userId}.json")
    suspend fun deleteMeetingMember(
        @Path("chatRoomId") chatRoomId: String,
        @Path("userId") userId: String,
        @Query("auth") auth: String?
    ): ApiResponse<Unit>

    @DELETE("member_meeting/{userId}/{chatRoomId}.json")
    suspend fun deleteMemberMeeting(
        @Path("userId") userId: String,
        @Path("chatRoomId") chatRoomId: String,
        @Query("auth") auth: String?
    ): ApiResponse<Unit>

    @PUT("user/{uid}.json")
    suspend fun createUser(
        @Path("uid") uid: String,
        @Query("auth") auth: String?,
        @Body user: User
    ): ApiResponse<Map<String, String>>

    @GET("user/{userId}.json")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String?
    ): ApiResponse<User>

    @PATCH("user/{userId}.json")
    suspend fun upDateUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String?,
        @Body nickname: Map<String, String>
    ): ApiResponse<Map<String, String>>

    @POST("post.json")
    suspend fun createPost(
        @Query("auth") auth: String?,
        @Body user: Post
    ): ApiResponse<Map<String, String>>

    @PATCH("meeting_member/{postId}.json")
    suspend fun addMeetingMemberList(
        @Path("postId") postId: String,
        @Query("auth") auth: String?,
        @Body user: Map<String, Boolean>
    ): ApiResponse<Map<String, Boolean>>

    @PATCH("member_meeting/{userId}.json")
    suspend fun addMemberMeetingList(
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