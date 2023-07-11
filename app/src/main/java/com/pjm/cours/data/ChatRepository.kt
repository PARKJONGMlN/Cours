package com.pjm.cours.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.ChatDataSource
import com.pjm.cours.data.remote.ImageUriDataSource
import com.pjm.cours.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatRemoteDataSource: ChatDataSource,
    private val imageUriRemoteDataSource: ImageUriDataSource,
    preferenceManager: PreferenceManager
) {
    private val userId = preferenceManager.getString(Constants.USER_ID, "")
    private lateinit var upDateChatPreviewCallback: (String, Message) -> (Unit)

    fun setOnNewMessageCallback(callback: (String, Message) -> (Unit)) {
        upDateChatPreviewCallback = callback
    }

    suspend fun getDefaultChatPreviewList() = withContext(Dispatchers.IO) {
        val snapShot = chatRemoteDataSource.getUserChatIdList(userId)
        val deferredList = snapShot.children.map { chatRoomSnapshot ->
            async {
                val postId = chatRoomSnapshot.key ?: ""
                val postSnapshot = chatRemoteDataSource.getPostInfo(postId)
                val post = parsePost(postSnapshot)
                val title = post.title

                val hostImageUri = post.hostUser.profileUri
                val imageDownLoadUri = imageUriRemoteDataSource.getImageDownLoadUri(hostImageUri).toString()

                val messageSnapshot = chatRemoteDataSource.getLastMessage(postId)
                val message = parseMessage(messageSnapshot)

                addNewMessageEventListener(postId, valueEventListener(postId))
                ChatPreview(
                    postId = postId,
                    hostImageUri = imageDownLoadUri,
                    postTitle = title,
                    lastMessage = message.text,
                    unReadMessageCount = "0",
                    messageDate = message.timestamp.toString()
                )
            }
        }
        val chatPreviewList = deferredList.awaitAll()
        chatPreviewList.sortedByDescending { it.messageDate }
    }

    private fun valueEventListener(postId: String) = object : ValueEventListener {
        var isFirst = true
        override fun onDataChange(snapshot: DataSnapshot) {
            if (isFirst) {
                isFirst = false
            } else {
                upDateChatPreviewCallback(
                    postId,
                    parseMessage(snapshot)
                )
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