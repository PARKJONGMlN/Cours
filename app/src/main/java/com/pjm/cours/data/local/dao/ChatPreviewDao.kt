package com.pjm.cours.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pjm.cours.data.local.entities.ChatPreviewEntity

@Dao
interface ChatPreviewDao {

    @Query("SELECT * FROM chat_preview ORDER BY send_date DESC")
    fun getPreviewList(): LiveData<List<ChatPreviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatPreview: ChatPreviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chatPreview: List<ChatPreviewEntity>)

    @Query("UPDATE chat_preview SET send_date = :sendDate, last_message = :lastMessage WHERE post_id = :postId")
    suspend fun update(postId: String, sendDate: String, lastMessage: String)

}