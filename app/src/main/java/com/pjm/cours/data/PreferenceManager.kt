package com.pjm.cours.data

import android.content.Context
import com.pjm.cours.util.Constants.LATITUDE
import com.pjm.cours.util.Constants.LONGITUDE
import dagger.hilt.android.qualifiers.ApplicationContext
import net.daum.mf.map.api.MapPoint
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

    fun setUserCurrentPoint(
        latitudeKey: String,
        currentLatitude: String,
        longitudeKey: String,
        currentLongitude: String
    ) {
        sharedPreferences.edit().putString(latitudeKey, currentLatitude).apply()
        sharedPreferences.edit().putString(longitudeKey, currentLongitude).apply()
    }

    fun getUserCurrentPoint(): MapPoint?{
        val latitude = sharedPreferences.getString(LATITUDE, "") ?: ""
        val longitude = sharedPreferences.getString(LONGITUDE, "") ?: ""
        return if(latitude.isEmpty() || longitude.isEmpty() ){
            null
        } else {
            MapPoint.mapPointWithGeoCoord(latitude.toDouble(), longitude.toDouble())
        }

    }


    fun removeGoogleIdToken(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}