package com.pjm.cours.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.CoursApplication
import com.pjm.cours.ui.map.MapActivity
import com.pjm.cours.util.Constants.KEY_GOOGLE_ID_TOKEN

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToFirstScreen()
    }

    private fun moveToFirstScreen() {
        val localGoogleIdToken = CoursApplication.preferencesManager.getString(KEY_GOOGLE_ID_TOKEN, "")
        if (localGoogleIdToken.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MapActivity::class.java))
        }
        finish()
    }
}