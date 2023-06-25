package com.pjm.cours

import android.app.Application
import com.pjm.cours.data.PreferenceManager

class CoursApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferenceManager(this)
    }

    companion object {
        lateinit var preferencesManager: PreferenceManager
    }
}