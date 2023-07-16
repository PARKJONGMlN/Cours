package com.pjm.cours.ui.chat

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.ChatItem
import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.MyChat
import com.pjm.cours.data.model.OtherChat
import com.pjm.cours.data.repository.ChatRepository
import com.pjm.cours.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val email = FirebaseAuth.getInstance().currentUser?.email

    private val _postId = MutableLiveData<Event<String>>()
    val postId: LiveData<Event<String>> = _postId

    private val _newMessage = MutableLiveData<Event<Message>>()
    val newMessage: LiveData<Event<Message>> = _newMessage

    private val _isSendComplete = MutableLiveData<Event<Boolean>>()
    val isSendComplete: LiveData<Event<Boolean>> = _isSendComplete

    private val _isError = MutableLiveData<Event<Boolean>>()
    val isError: LiveData<Event<Boolean>> = _isError

    private val database = FirebaseDatabase.getInstance(BuildConfig.BASE_URL)
    private lateinit var chatRoomRef: DatabaseReference

    lateinit var messageList: LiveData<List<ChatItem>>

    private fun getMessages(postId: String) {
        messageList = chatRepository.getMessages(postId).map { messageEntityList ->
            messageEntityList.map { messageEntity ->
                if (messageEntity.sender == email) {
                    MyChat(
                        postId = messageEntity.postId,
                        messageId = messageEntity.messageId,
                        sender = messageEntity.sender,
                        sendDate = messageEntity.sendDate,
                        text = messageEntity.text
                    )
                } else {
                    OtherChat(
                        postId = messageEntity.postId,
                        messageId = messageEntity.messageId,
                        sender = messageEntity.sender,
                        sendDate = messageEntity.sendDate,
                        text = messageEntity.text
                    )
                }
            }.sortedBy { it.sendDate }
        }
    }


    fun setPostId(postId: String) {
        _postId.value = Event(postId)
        chatRoomRef = database.getReference("chat").child(postId).child("messages")
        viewModelScope.launch {
            getMessages(postId)
        }
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(postId.value?.peekContent() ?: "", message)
            } catch (e: Exception){
                _isError.value = Event(false)

            }

        }
    }
}