package com.pjm.cours

import android.app.Application
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.local.AppDatabase
import com.pjm.cours.data.remote.ApiContainer
import com.pjm.cours.data.remote.ChatDataSource
import com.pjm.cours.data.remote.ImageUriDataSource
import com.pjm.cours.data.repository.ChatRepository

class CoursApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy {
        ChatRepository(
            ChatDataSource(),
            ImageUriDataSource(),
            preferencesManager,
            database.messageDao(),
            apiContainer.provideApiClient()
        )
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferenceManager(this)
    }

    companion object {
        val apiContainer = ApiContainer()
        lateinit var preferencesManager: PreferenceManager
    }
}