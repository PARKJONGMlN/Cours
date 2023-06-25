package com.pjm.cours

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.util.Constants.KEY_MAIL_ADDRESS

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToFirstScreen()
    }

    private fun moveToFirstScreen() {
        if (CoursApplication.preferencesManager.getString(KEY_MAIL_ADDRESS, "").isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MapActivity::class.java))
        }
        finish()
    }
}