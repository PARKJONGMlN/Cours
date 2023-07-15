package com.pjm.cours.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pjm.cours.data.local.dao.ChatPreviewDao
import com.pjm.cours.data.local.dao.MessageDao
import com.pjm.cours.data.local.entities.ChatPreviewEntity
import com.pjm.cours.data.local.entities.MessageEntity

@Database(entities = [MessageEntity::class, ChatPreviewEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun chatPreviewDao(): ChatPreviewDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cour_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }

}