package com.pjm.cours.ui.chatlist

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.CoursApplication
import com.pjm.cours.data.remote.ChatDataSource
import com.pjm.cours.data.remote.ImageUriDataSource
import com.pjm.cours.data.repository.ChatRepository
import com.pjm.cours.databinding.ActivityChatListBinding
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.util.Constants

class ChatListActivity : AppCompatActivity() {

    private lateinit var adapter: ChatListAdapter
    private lateinit var binding: ActivityChatListBinding

    private val viewModel: ChatListViewModel by viewModels {
        ChatListViewModel.provideFactory(
            ChatRepository(
                ChatDataSource(),
                ImageUriDataSource(),
                CoursApplication.preferencesManager,
                (application as CoursApplication).database.messageDao(),
                CoursApplication.apiContainer.provideApiClient()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAdapter()
        setObserver()
    }

    private fun setAdapter() {
        adapter = ChatListAdapter { chatPreview ->
            Intent(this, ChatActivity::class.java).apply {
                putExtra(Constants.POST_ID, chatPreview.postId)
                startActivity(this)
            }
        }
        binding.recyclerViewChatPreview.adapter = adapter
        binding.recyclerViewChatPreview.itemAnimator = null
    }

    private fun setObserver() {
        viewModel.chatListUiState.observe(this) { chatListUiState ->
            chatListUiState?.let {
                if (chatListUiState.isDefaultListSetting) {
                    adapter.submitFirst(chatListUiState.defaultChatPreviewList)
                } else {
                    adapter.submitItem(chatListUiState.newChatPreviewItem)
                }
            }
        }
    }
}