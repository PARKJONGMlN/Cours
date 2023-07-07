package com.pjm.cours.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.Message
import com.pjm.cours.util.Event

class ChatViewModel : ViewModel() {

    val email = FirebaseAuth.getInstance().currentUser?.email

    private val _postId = MutableLiveData<Event<String>>()
    val postId: LiveData<Event<String>> = _postId

    private val _newMessage = MutableLiveData<Event<Message>>()
    val newMessage: LiveData<Event<Message>> = _newMessage

    private val _isSendComplete = MutableLiveData<Event<Boolean>>()
    val isSendComplete: LiveData<Event<Boolean>> = _isSendComplete

    private val database = FirebaseDatabase.getInstance(BuildConfig.BASE_URL)
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var messageListener: ChildEventListener

    fun setPostId(postId: String) {
        _postId.value = Event(postId)
        chatRoomRef = database.getReference("chat").child(postId).child("messages")
        messageListener = chatRoomRef.orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val newMessage = dataSnapshot.getValue(Message::class.java) ?: return
                    _newMessage.value = Event(newMessage)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun sendMessage(message: Message) {
        try {
            val id = chatRoomRef.push().key
            chatRoomRef.child(id!!).setValue(message)
                .addOnCompleteListener {
                    _isSendComplete.value = Event(true)
                }
                .addOnFailureListener {
                    _isSendComplete.value = Event(false)
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        fun provideFactory() = viewModelFactory {
            initializer {
                ChatViewModel()
            }
        }
    }
}