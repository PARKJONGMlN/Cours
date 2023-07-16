package com.pjm.cours.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "com.pjm.cours.PREFERENCE_KEY",
        Context.MODE_PRIVATE
    )

    fun getString(key: String, defValue: String): String {
        return sharedPreferences.getString(key, defValue) ?: defValue
    }

    fun setGoogleIdToken(key: String, googleIdToken: String) {
        sharedPreferences.edit().putString(key, googleIdToken).apply()
    }

    fun setUserId(key: String, userId: String) {
        sharedPreferences.edit().putString(key, userId).apply()
    }

    fun removeGoogleIdToken(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}