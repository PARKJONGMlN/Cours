package com.pjm.cours.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pjm.cours.data.local.dao.ChatPreviewDao
import com.pjm.cours.data.local.dao.MessageDao
import com.pjm.cours.data.local.entities.ChatPreviewEntity
import com.pjm.cours.data.local.entities.MessageEntity

@Database(entities = [MessageEntity::class, ChatPreviewEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun chatPreviewDao(): ChatPreviewDao
}