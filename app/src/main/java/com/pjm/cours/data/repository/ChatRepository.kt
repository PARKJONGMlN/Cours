package com.pjm.cours.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.data.local.dao.ChatPreviewDao
import com.pjm.cours.data.local.dao.MessageDao
import com.pjm.cours.data.local.entities.ChatPreviewEntity
import com.pjm.cours.data.local.entities.MessageEntity
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.*
import com.pjm.cours.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatRemoteDataSource: ChatDataSource,
    private val imageUriRemoteDataSource: ImageUriDataSource,
    preferenceManager: PreferenceManager,
    private val messageDao: MessageDao,
    private val chatPreviewDao: ChatPreviewDao,
    private val apiClient: ApiClient,
) {
    private val userId = preferenceManager.getString(Constants.USER_ID, "")

    suspend fun sendMessage(postId: String, message: Message): ApiResponse<Map<String, String>> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            apiClient.sendMessage(postId = postId, idToken, message = message)
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    fun getChatPreviewList(): Flow<List<ChatPreviewEntity>> {
        return chatPreviewDao.getPreviewList()
    }

    fun getMessages(postId: String): Flow<List<MessageEntity>> {
        chatRemoteDataSource.getMessageUpdates(postId) { message, messageId ->
            val messageEntity = MessageEntity(
                postId = postId,
                messageId = messageId,
                sender = message.senderEmail,
                sendDate = message.timestamp.toString(),
                text = message.text
            )
            CoroutineScope(Dispatchers.IO).launch {
                messageDao.insert(messageEntity)
            }
        }
        return messageDao.getMessageListByPostId(postId)
    }

    suspend fun upDateChatPreviewList() = withContext(Dispatchers.IO) {
        val snapShot = chatRemoteDataSource.getUserChatIdList(userId)
        snapShot.children.map { chatRoomSnapshot ->
            async {
                val postId = chatRoomSnapshot.key ?: ""
                val postSnapshot = chatRemoteDataSource.getPostInfo(postId)
                val post = parsePost(postSnapshot)
                val title = post.title
                val hostImageUri = post.hostUser.profileUri
                val imageDownLoadUri =
                    imageUriRemoteDataSource.getImageDownLoadUri(hostImageUri).toString()

                val messageSnapshot = chatRemoteDataSource.getLastMessage(postId)
                val message = parseMessage(messageSnapshot)
                addNewMessageEventListener(postId, valueEventListener(postId))

                val chatPreview = ChatPreview(
                    postId = postId,
                    hostImageUri = imageDownLoadUri,
                    postTitle = title,
                    lastMessage = message.text,
                    unReadMessageCount = "0",
                    messageDate = message.timestamp.toString()
                )

                chatPreviewDao.insert(
                    ChatPreviewEntity(
                        postId = chatPreview.postId,
                        hostImageUri = chatPreview.hostImageUri,
                        postTitle = chatPreview.postTitle,
                        sendDate = chatPreview.messageDate,
                        lastMessage = chatPreview.lastMessage,
                        unReadMessageCount = chatPreview.unReadMessageCount,
                    )
                )
            }
        }
    }

    private fun valueEventListener(postId: String) = object : ValueEventListener {
        var isFirst = true
        override fun onDataChange(snapshot: DataSnapshot) {
            if (isFirst) {
                isFirst = false
            } else {
                val message = parseMessage(snapshot)
                CoroutineScope(Dispatchers.IO).launch {
                    chatPreviewDao.update(
                        postId = postId,
                        sendDate = message.timestamp.toString(),
                        lastMessage = message.text
                    )
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }

    }

    private fun addNewMessageEventListener(postId: String, listener: ValueEventListener) {
        chatRemoteDataSource.addNewMessageEventListener(postId, listener)
    }

    private fun parseMessage(messageSnapshot: DataSnapshot): Message {
        return messageSnapshot.children.firstOrNull()?.getValue(Message::class.java) ?: Message()
    }

    private fun parsePost(postSnapshot: DataSnapshot): Post {
        return postSnapshot.getValue(Post::class.java) ?: Post()
    }

}