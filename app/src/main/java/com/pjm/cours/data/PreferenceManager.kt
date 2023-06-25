package com.pjm.cours.data

import android.content.Context

class PreferenceManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "com.pjm.cours.PREFERENCE_KEY",
        Context.MODE_PRIVATE
    )

    fun getString(key: String, defValue: String): String {
        return sharedPreferences.getString(key, defValue) ?: defValue
    }
}