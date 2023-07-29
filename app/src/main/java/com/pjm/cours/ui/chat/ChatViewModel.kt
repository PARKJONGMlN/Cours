package com.pjm.cours.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.*
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val email = FirebaseAuth.getInstance().currentUser?.email
    lateinit var messageList: StateFlow<List<ChatItem>>
    private val memberTokenList = MutableStateFlow<List<String>>(emptyList())
    val messageText = MutableStateFlow("")

    private val _postId = MutableStateFlow("")
    val postId = _postId.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    private val _cleatTextEvent = MutableSharedFlow<Unit>()
    val cleatTextEvent = _cleatTextEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            chatRepository.memberIdList.collect { memberIdList ->
                memberTokenList.value = memberIdList.map { userId ->
                    val result = chatRepository.getUserFcmToken(userId)
                    when (result) {
                        is ApiResultSuccess -> {
                            result.data.fcmToken
                        }
                        is ApiResultError -> {
                            ""
                        }
                        is ApiResultException -> {
                            ""
                        }
                    }
                }
            }
        }
    }

    fun setPostId(postId: String) {
        _postId.value = postId
        getMessages(postId)
        getMemberList(postId)
    }

    private fun getMessages(postId: String) {
        messageList = transFormChatList(postId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun getMemberList(postId: String) {
        chatRepository.getMemberList(postId)
    }

    private fun transFormChatList(postId: String) =
        chatRepository.getMessages(postId).map { messageEntityList ->
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

    fun sendMessage() {
        viewModelScope.launch {
            val message = Message(
                messageText.value,
                email ?: "",
                System.currentTimeMillis()
            )
            _cleatTextEvent.emit(Unit)
            val result = chatRepository.sendMessage(postId.value, message)
            when (result) {
                is ApiResultSuccess -> {
                    for (token in memberTokenList.value) {
                        chatRepository.sendNotification(
                            NotificationBody(
                                token,
                                NotificationBody.NotificationData(
                                    title = message.text,
                                    userId = email?: "",
                                    message = message.text,
                                    chatRoomId = postId.value
                                )
                            )
                        )
                    }
                }
                is ApiResultError -> {
                    _isError.value = true
                }
                is ApiResultException -> {
                    _isError.value = true
                }
            }
        }
    }

}