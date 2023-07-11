package com.pjm.cours.ui.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.ChatRepository
import com.pjm.cours.data.model.ChatPreview
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    data class ChatListUiState(
        val isLoading: Boolean = false,
        val isDefaultListSetting: Boolean = false,
        val newChatPreviewItem: ChatPreview = ChatPreview(),
        val defaultChatPreviewList: List<ChatPreview> = listOf()
    )

    private val _chatListUiState = MutableLiveData<ChatListUiState>()
    val chatListUiState: LiveData<ChatListUiState> = _chatListUiState

    init {
        setDefaultChatPreviewList()
    }

    private fun setDefaultChatPreviewList() {
        viewModelScope.launch {
            chatRepository.setOnNewMessageCallback { postId, newMessage ->
                _chatListUiState.value = _chatListUiState.value?.copy(
                    isDefaultListSetting = false,
                    newChatPreviewItem = ChatPreview(
                        postId = postId,
                        lastMessage = newMessage.text,
                        unReadMessageCount = "0",
                        messageDate = newMessage.timestamp.toString()
                    )
                )
            }

            val sortedChatPreviewList = chatRepository.getDefaultChatPreviewList()
            _chatListUiState.value =
                ChatListUiState(
                    isDefaultListSetting = true,
                    isLoading = false,
                    defaultChatPreviewList = sortedChatPreviewList
                )
        }
    }

    companion object {

        fun provideFactory(chatRepository: ChatRepository) = viewModelFactory {
            initializer {
                ChatListViewModel(chatRepository)
            }
        }
    }
}

