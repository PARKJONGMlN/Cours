package com.pjm.cours.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class MessageEntity(

    @PrimaryKey @ColumnInfo(name = "message_id") val messageId: String = "",
    @ColumnInfo(name = "post_id") val postId: String = "",
    @ColumnInfo(name = "send_date") val sendDate: String = "",
    @ColumnInfo(name = "text") val text: String = "",
    @ColumnInfo(name = "sender") val sender: String = "",
)
