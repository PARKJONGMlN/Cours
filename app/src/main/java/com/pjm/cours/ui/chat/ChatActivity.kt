package com.pjm.cours.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pjm.cours.BuildConfig
import com.pjm.cours.data.model.Message
import com.pjm.cours.data.model.MyChat
import com.pjm.cours.data.model.OtherChat
import com.pjm.cours.databinding.ActivityChatBinding
import com.pjm.cours.util.Constants

class ChatActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var messageListener: ChildEventListener
    private lateinit var binding: ActivityChatBinding
    private val adapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUiState()
        setLayout()
    }

    private fun initUiState() {
        database = FirebaseDatabase.getInstance(BuildConfig.BASE_URL)
        chatRoomRef =
            database.getReference("chat").child(intent.getStringExtra(Constants.POST_ID) ?: "")
                .child("messages")
        receiveMessages()
    }

    private fun setLayout() {
        binding.recyclerViewChat.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.scrollToPosition(adapter.itemCount - 1)

        binding.btnSendMessage.setOnClickListener {
            try {
                val id = chatRoomRef.push().key
                val timestamp = System.currentTimeMillis()
                val message = Message(
                    binding.etMessage.text.toString(),
                    FirebaseAuth.getInstance().currentUser?.email ?: "",
                    timestamp
                )
                chatRoomRef.child(id!!).setValue(message)
                    .addOnCompleteListener {
                        binding.etMessage.text.clear()
                        binding.recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun receiveMessages() {
        messageListener = chatRoomRef.orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val newMessage = dataSnapshot.getValue(Message::class.java) ?: return
                    if (newMessage.senderNickname == FirebaseAuth.getInstance().currentUser?.email) {
                        adapter.submitChat(MyChat(newMessage.text))
                    } else {
                        adapter.submitChat(OtherChat(newMessage.senderNickname, newMessage.text))
                    }
                    binding.recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}