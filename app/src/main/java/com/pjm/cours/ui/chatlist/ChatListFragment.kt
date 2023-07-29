package com.pjm.cours.ui.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navController = findNavController()
        val navGraph = navController.graph
        navGraph.setStartDestination(R.id.chatListFragment)
        navController.graph = navGraph
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            viewModel.upDateUser(token)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        setChatList()
    }

    private fun setChatList() {
        viewModel.upDateChatPreviewList()
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