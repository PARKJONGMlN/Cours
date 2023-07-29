package com.pjm.cours.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.data.repository.ChatRepository
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val chatPreviewList: StateFlow<List<ChatPreview>> = transFormChatPreviewList().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun transFormChatPreviewList() =
        chatRepository.getChatPreviewList().map { chatPreviewEntityList ->
            chatPreviewEntityList.map { chatPreviewEntity ->
                ChatPreview(
                    postId = chatPreviewEntity.postId,
                    hostImageUri = chatPreviewEntity.hostImageUri,
                    postTitle = chatPreviewEntity.postTitle,
                    messageDate = chatPreviewEntity.sendDate,
                    lastMessage = chatPreviewEntity.lastMessage,
                    unReadMessageCount = chatPreviewEntity.unReadMessageCount,
                )
            }
        }

    fun upDateChatPreviewList(){
        viewModelScope.launch {
            chatRepository.upDateChatPreviewList()
        }
    }

    fun upDateUser(fcmToken: String) {
        viewModelScope.launch {
            userRepository.setUserFcmToken(fcmToken)
        }
    }

}

