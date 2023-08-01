package com.pjm.cours.data.local.dao

import androidx.room.*
import com.pjm.cours.data.local.entities.ChatPreviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatPreviewDao {

    @Query("SELECT * FROM chat_preview ORDER BY send_date DESC")
    fun getPreviewList(): Flow<List<ChatPreviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatPreview: ChatPreviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chatPreview: List<ChatPreviewEntity>)

    @Query("UPDATE chat_preview SET send_date = :sendDate, last_message = :lastMessage WHERE post_id = :postId")
    suspend fun update(postId: String, sendDate: String, lastMessage: String)

    @Query("DELETE FROM chat_preview WHERE post_id = :postId")
    suspend fun deleteByPostId(postId: String)

    @Query("DELETE FROM chat_preview")
    suspend fun deleteAll()

}