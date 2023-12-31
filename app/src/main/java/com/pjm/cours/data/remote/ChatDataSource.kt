package com.pjm.cours.data.remote

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatDataSource @Inject constructor() {

    private val database = FirebaseDatabase.getInstance(BuildConfig.BASE_URL)
    private val userChatRoomsRef = database.getReference("member_meeting")
    private val chatRoomMemberRef = database.getReference("meeting_member")
    private val postRef = database.getReference("post")
    private val messageRef = database.getReference("chat")

    private val _memberListState = MutableStateFlow<List<String>>(emptyList())
    val memberListState: StateFlow<List<String>> = _memberListState

    suspend fun getUserChatIdList(userId: String): DataSnapshot {
        return userChatRoomsRef.child(userId).get().await()
    }

    suspend fun getPostInfo(postId: String): DataSnapshot {
        return postRef.child(postId).get().await()
    }

    suspend fun getLastMessage(
        postId: String,
        userId: String,
        insertLastMessage: suspend (DataSnapshot) -> Unit,
    ) {
        chatRoomMemberRef.child(postId).child(userId).get().await().let { snapshot ->
            val entryTime = snapshot.value.toString()
            try {
                val dataSnapshot = messageRef.child(postId).child("messages")
                    .orderByChild("timestamp")
                    .startAfter(entryTime.toDouble())
                    .limitToLast(1)
                    .get()
                    .await()
                insertLastMessage(dataSnapshot)
            } catch (e: Exception) {
            }
        }
    }

    fun addNewMessageEventListener(postId: String, listener: ValueEventListener) {
        messageRef.child(postId).child("messages").orderByKey().limitToLast(1)
            .addValueEventListener(listener)
    }

    fun getMessageUpdates(postId: String, userId: String, onNewMessage: (Message, String) -> Unit) {
        chatRoomMemberRef.child(postId).child(userId).get().addOnSuccessListener { snapshot ->
            val entryTime = snapshot.value.toString()
            messageRef.child(postId).child("messages").orderByChild("timestamp")
                .startAt(entryTime.toDouble())
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val messageId = snapshot.key ?: ""
                        val message = snapshot.getValue(Message::class.java) ?: return
                        onNewMessage(message, messageId)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    fun getMeetingMemberListId(postId: String, userId: String) {
        chatRoomMemberRef.child(postId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value as? Map<String, Any> ?: mapOf("" to "")
                _memberListState.value = value.keys.filter { it != userId }.toList()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
