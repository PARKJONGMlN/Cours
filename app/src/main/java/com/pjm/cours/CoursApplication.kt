package com.pjm.cours

import android.app.Application
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.remote.ApiContainer

class CoursApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferenceManager(this)
    }

    companion object {
        val apiContainer = ApiContainer()
        lateinit var preferencesManager: PreferenceManager
    }
}