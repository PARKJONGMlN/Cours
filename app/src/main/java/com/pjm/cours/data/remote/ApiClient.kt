package com.pjm.cours.data.remote

import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiClient {

    @POST("user.json")
    suspend fun createUser(
        @Query("auth") auth: String?,
        @Body user: User
    ): Response<Map<String, String>>

    @POST("post.json")
    suspend fun createPost(
        @Query("auth") auth: String?,
        @Body user: Post
    ): Response<Map<String, String>>

    @GET("post.json")
    suspend fun getPosts(
        @Query("auth") auth: String?
    ): Response<Map<String, Post>>

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