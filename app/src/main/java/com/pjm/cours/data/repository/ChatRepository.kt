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
import com.pjm.cours.data.model.NotificationBody
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.model.User
import com.pjm.cours.data.remote.ApiClient
import com.pjm.cours.data.remote.ApiResponse
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ChatDataSource
import com.pjm.cours.data.remote.FcmClient
import com.pjm.cours.data.remote.ImageUriDataSource
import com.pjm.cours.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatRemoteDataSource: ChatDataSource,
    private val imageUriRemoteDataSource: ImageUriDataSource,
    private val preferenceManager: PreferenceManager,
    private val messageDao: MessageDao,
    private val chatPreviewDao: ChatPreviewDao,
    private val apiClient: ApiClient,
    private val fcmClient: FcmClient,
) {
    private val userId = preferenceManager.getString(Constants.USER_ID, "")
    val memberIdList: StateFlow<List<String>> = chatRemoteDataSource.memberListState
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
        chatRemoteDataSource.getMessageUpdates(postId, userId) { message, messageId ->
            val messageEntity = MessageEntity(
                senderUid = message.senderUid,
                postId = postId,
                messageId = messageId,
                sender = message.sender,
                sendDate = message.timestamp.toString(),
                text = message.text,
            )
            CoroutineScope(Dispatchers.IO).launch {
                messageDao.insert(messageEntity)
            }
        }
        return messageDao.getMessageListByPostId(postId)
    }

    suspend fun getUserFcmToken(userId: String): ApiResponse<User> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            apiClient.getUser(userId, idToken)
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    fun getMemberList(postId: String) {
        chatRemoteDataSource.getMeetingMemberListId(postId, userId)
    }

    suspend fun upDateChatPreviewList() = withContext(Dispatchers.IO) {
        val snapShot = chatRemoteDataSource.getUserChatIdList(userId)
        snapShot.children.map { chatRoomSnapshot ->
            async {
                val postId = chatRoomSnapshot.key ?: ""
                addNewMessageEventListener(postId, valueEventListener(postId))
                val postSnapshot = chatRemoteDataSource.getPostInfo(postId)
                val post = parsePost(postSnapshot)
                val title = post.title
                val hostImageUri = post.hostUser.profileUri
                val imageDownLoadUri =
                    imageUriRemoteDataSource.getImageDownLoadUri(hostImageUri).toString()

                chatRemoteDataSource.getLastMessage(postId, userId) { messageSnapshot ->
                    val message = parseMessage(messageSnapshot)

                    val chatPreview = ChatPreview(
                        postId = postId,
                        hostImageUri = imageDownLoadUri,
                        postTitle = title,
                        lastMessage = message.text,
                        unReadMessageCount = "0",
                        messageDate = message.timestamp.toString(),
                    )

                    chatPreviewDao.insert(
                        ChatPreviewEntity(
                            postId = chatPreview.postId,
                            hostImageUri = chatPreview.hostImageUri,
                            postTitle = chatPreview.postTitle,
                            sendDate = chatPreview.messageDate,
                            lastMessage = chatPreview.lastMessage,
                            unReadMessageCount = chatPreview.unReadMessageCount,
                        ),
                    )
                }
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
                        lastMessage = message.text,
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

    suspend fun sendNotification(notification: NotificationBody) {
        fcmClient.sendNotification(notification = notification)
    }

    fun setCurrentRoomId(chatRoomId: String) {
        preferenceManager.setCurrentChatRoomId(Constants.CHAT_ROOM_ID, chatRoomId)
    }

    suspend fun exitChat(chatRoomId: String, currentMemberCount: Int): ApiResponse<Unit> {
        return try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
            val updateMemberCount = currentMemberCount - 1
            chatPreviewDao.deleteByPostId(chatRoomId)
            messageDao.deleteMessagesByPostId(chatRoomId)
            apiClient.updateCurrentMemberCount(
                chatRoomId,
                idToken,
                mapOf("currentMemberCount" to updateMemberCount.toString()),
            )
            apiClient.deleteMemberMeeting(userId, chatRoomId, idToken)
            apiClient.deleteMeetingMember(chatRoomId, userId, idToken)
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }
}
