package com.pjm.cours.ui.chatlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.databinding.ActivityChatListBinding

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}