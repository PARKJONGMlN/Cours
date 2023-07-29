package com.pjm.cours.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.*
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.ChatRepository
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userInfo = MutableStateFlow(User())
    val userInfo = _userInfo.asStateFlow()

    lateinit var messageList: StateFlow<List<ChatItem>>
    private val memberTokenList = MutableStateFlow<List<String>>(emptyList())
    val messageText = MutableStateFlow("")

    private val _postId = MutableStateFlow("")
    val postId = _postId.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    private val _isExitComplete = MutableStateFlow(false)
    val isExitComplete = _isExitComplete.asStateFlow()

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
        viewModelScope.launch {
            val result = userRepository.getUserInfo()
            when (result) {
                is ApiResultSuccess -> {
                    _userInfo.value = result.data
                }
                is ApiResultError -> {
                }
                is ApiResultException -> {
                }
            }
        }

    }

    fun setPostId(postId: String) {
        _postId.value = postId
        getMessages(postId)
        getMemberList(postId)
        chatRepository.setCurrentRoomId(postId)
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
                if (messageEntity.senderUid == uid) {
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
                uid,
                messageText.value,
                userInfo.value.nickname,
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
                                    sender = userInfo.value.nickname,
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

    fun exitChat() {
        viewModelScope.launch {
            val currentMember = memberTokenList.value.size + 1
            val result = chatRepository.exitChat(_postId.value, currentMember)
            when (result) {
                is ApiResultSuccess -> {
                    _isExitComplete.value = true
                }
                is ApiResultError -> {
                }
                is ApiResultException -> {
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.setCurrentRoomId("")
    }

}