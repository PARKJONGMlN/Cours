package com.pjm.cours.ui.chatlist

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.data.repository.ChatRepository
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(Event(true))
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    val chatPreviewList: LiveData<List<ChatPreview>> =
        chatRepository.getChatPreview().map { chatPreviewEntityList ->
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

    init {
        viewModelScope.launch {
            chatRepository.getChatPreviewList()
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

