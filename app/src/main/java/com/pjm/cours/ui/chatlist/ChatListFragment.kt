package com.pjm.cours.ui.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentChatListBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : BaseFragment<FragmentChatListBinding>(R.layout.fragment_chat_list) {

    private val viewModel: ChatListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        setChatList()
    }

    private fun setChatList() {
        val adapter = ChatListAdapter { chatPreview ->
            Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra(Constants.POST_ID, chatPreview.postId)
                startActivity(this)
            }
        }
        binding.recyclerViewChatPreview.adapter = adapter
        binding.recyclerViewChatPreview.itemAnimator = null
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatPreviewList.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { chatPreviewList ->
                adapter.submitList(chatPreviewList)
            }
        }
    }

}