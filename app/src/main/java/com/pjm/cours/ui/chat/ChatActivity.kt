package com.pjm.cours.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.R
import com.pjm.cours.databinding.ActivityChatBinding
import com.pjm.cours.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    val viewModel: ChatViewModel by viewModels()
    private val adapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        initUiState()
        setLayout()
    }

    private fun initUiState() {
        viewModel.setPostId(intent.getStringExtra(Constants.POST_ID) ?: "")
    }

    private fun setLayout() {
        setChatList()
        setErrorMessage()
        setTextClear()
    }

    private fun setChatList() {
        setAdapter()
        lifecycleScope.launch {
            viewModel.messageList.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { messageList ->
                    if (messageList.isNotEmpty()) {
                        adapter.submitList(messageList)
                    }
                }
        }
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

    private fun setTextClear() {
        lifecycleScope.launch {
            viewModel.cleatTextEvent.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.etMessage.text.clear()
                }
        }
    }

    private fun setErrorMessage() {
        lifecycleScope.launch {
            viewModel.isError.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isError ->
                    if (isError) {
                        Toast.makeText(
                            this@ChatActivity,
                            getString(R.string.error_message),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

}