package com.pjm.cours.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}