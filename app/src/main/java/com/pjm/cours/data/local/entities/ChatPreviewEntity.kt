package com.pjm.cours.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_preview")
class ChatPreviewEntity(

    @PrimaryKey @ColumnInfo(name = "post_id") val postId: String = "",
    @ColumnInfo(name = "host_image_uri") val hostImageUri: String = "",
    @ColumnInfo(name = "post_title") val postTitle: String = "",
    @ColumnInfo(name = "send_date") val sendDate: String = "",
    @ColumnInfo(name = "last_message") val lastMessage: String = "",
    @ColumnInfo(name = "un_read_message_count") val unReadMessageCount: String = "",

)