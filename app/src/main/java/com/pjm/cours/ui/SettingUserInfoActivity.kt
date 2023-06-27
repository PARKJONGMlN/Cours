package com.pjm.cours.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.databinding.ActivitySettingUserInfoBinding

class SettingUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}