package com.pjm.cours.ui.chatlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pjm.cours.BuildConfig
import com.pjm.cours.CoursApplication
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.Post
import com.pjm.cours.util.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatListViewModel: ViewModel() {

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
            val userId = CoursApplication.preferencesManager.getString(Constants.USER_ID, "")
            val userChatRoomsRef =
                FirebaseDatabase.getInstance(BuildConfig.BASE_URL).getReference("member_meeting")
                    .child(userId)
            val database = FirebaseDatabase.getInstance(BuildConfig.BASE_URL)
            val snapshot = userChatRoomsRef.get().await()
            val children = snapshot.children
            val deferredList = children.map { chatRoomSnapshot ->
                async {
                    val postId = chatRoomSnapshot.key ?: ""
                    database.getReference("post").child(postId)
                    val postInfoSnapshot = database.getReference("post").child(postId).get().await()
                    val postInfo = postInfoSnapshot.getValue(Post::class.java)
                    val postTitle = postInfo?.title.toString()

                    var hostImageUri = postInfo?.hostUser?.profileUri.toString()
                    val storageRef = FirebaseStorage.getInstance().reference.child(hostImageUri)
                    val downloadUrl = storageRef.downloadUrl.await()
                    hostImageUri = downloadUrl.toString()


                    val chatRoomRef = database.getReference("chat").child(postId)
                        .child("messages")
                    val messageSnapshot = chatRoomRef.orderByKey().limitToLast(1).get().await()
                    val message =
                        messageSnapshot.children.firstOrNull()?.getValue(Message::class.java)
                            ?: Message()

                    var isFirst = true
                    chatRoomRef.orderByKey().limitToLast(1)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val newMessage =
                                    snapshot.children.firstOrNull()?.getValue(Message::class.java)
                                        ?: Message()
                                if (isFirst){
                                    isFirst = false
                                } else {
                                    Log.d("isFirst", "onDataChange: ${isFirst}")
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
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                    ChatPreview(
                        postId = postId,
                        hostImageUri = hostImageUri,
                        postTitle = postTitle,
                        lastMessage = message.text,
                        unReadMessageCount = "0",
                        messageDate = message.timestamp.toString()
                    )
                }
            }
            val chatPreviewList = deferredList.awaitAll()
            val sortedChatPreviewList = chatPreviewList.sortedByDescending { it.messageDate }

            _chatListUiState.value =
                ChatListUiState(
                    isDefaultListSetting = true,
                    isLoading = false,
                    defaultChatPreviewList = sortedChatPreviewList
                )
        }
    }

    companion object {

        fun provideFactory() = viewModelFactory {
            initializer {
                ChatListViewModel()
            }
        }
    }
}

