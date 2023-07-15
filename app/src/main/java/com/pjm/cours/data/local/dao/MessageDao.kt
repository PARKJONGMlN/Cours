package com.pjm.cours.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pjm.cours.data.local.entities.MessageEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE post_id = :postId ORDER BY send_date DESC")
    fun getMessageListByPostId(postId: String): LiveData<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<MessageEntity>)

}