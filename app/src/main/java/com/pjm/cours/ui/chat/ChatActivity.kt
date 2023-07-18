package com.pjm.cours.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.R
import com.pjm.cours.data.model.Message
import com.pjm.cours.databinding.ActivityChatBinding
import com.pjm.cours.util.Constants
import com.pjm.cours.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    val viewModel: ChatViewModel by viewModels()
    private val adapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUiState()
        setAdapter()
        setLayout()
        setObserver()
    }

    private fun initUiState() {
        viewModel.setPostId(intent.getStringExtra(Constants.POST_ID) ?: "")
    }

    private fun setAdapter() {
        binding.recyclerViewChat.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val insertedPosition = positionStart + itemCount - 1
                binding.recyclerViewChat.scrollToPosition(insertedPosition)
            }
        })
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
    }

    private fun setLayout() {
        binding.btnSendMessage.setOnClickListener {
            val timestamp = System.currentTimeMillis()
            viewModel.sendMessage(
                Message(
                    binding.etMessage.text.toString(),
                    FirebaseAuth.getInstance().currentUser?.email ?: "",
                    timestamp
                )
            )
            binding.etMessage.text.clear()
        }
    }

    private fun setObserver() {
        viewModel.messageList.observe(this) { messageList ->
            adapter.submitList(messageList)
        }
        viewModel.isError.observe(this, EventObserver { isError ->
            if (isError) {
                Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show()
            }
        })
    }

}