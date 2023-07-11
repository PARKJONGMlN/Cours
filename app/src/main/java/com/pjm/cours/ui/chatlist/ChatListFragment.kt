package com.pjm.cours.ui.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.pjm.cours.CoursApplication
import com.pjm.cours.R
import com.pjm.cours.data.ChatRepository
import com.pjm.cours.data.remote.ChatDataSource
import com.pjm.cours.data.remote.ImageUriDataSource
import com.pjm.cours.databinding.FragmentChatListBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.util.Constants

class ChatListFragment : BaseFragment<FragmentChatListBinding>(R.layout.fragment_chat_list) {

    private lateinit var adapter: ChatListAdapter

    private val viewModel: ChatListViewModel by viewModels {
        ChatListViewModel.provideFactory(
            ChatRepository(
                ChatDataSource(),
                ImageUriDataSource(),
                CoursApplication.preferencesManager
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setObserver()
    }

    private fun setAdapter() {
        adapter = ChatListAdapter { chatPreview ->
            Intent(requireContext(), ChatActivity::class.java).apply {
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