package com.pjm.cours.di

import android.content.Context
import androidx.room.Room
import com.pjm.cours.data.local.AppDatabase
import com.pjm.cours.data.local.dao.ChatPreviewDao
import com.pjm.cours.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cour_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providerChatPreviewDao(appDatabase: AppDatabase): ChatPreviewDao {
        return appDatabase.chatPreviewDao()
    }

    @Provides
    fun providerMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }
}