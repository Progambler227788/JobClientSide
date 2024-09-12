package com.talhaatif.jobportalclient

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.errorprone.annotations.Var
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.talhaatif.jobportalclient.adapter.MessageAdapter
import com.talhaatif.jobportalclient.databinding.ActivityChatScreenBinding
import com.talhaatif.jobportalclient.firebase.Variables
import com.talhaatif.jobportalclient.model.Message

class ChatScreen : AppCompatActivity() {
    private lateinit var binding: ActivityChatScreenBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messagesList = mutableListOf<Message>()
    private val firestore = Variables.db
    private val currentUserId = Variables.auth.currentUser?.uid ?: ""
    private lateinit var receiverId: String
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve receiver ID from intent
        receiverId = intent.getStringExtra("uid") ?: return

        loadUserDetails()

        // Create chatRoomId based on the combination of senderId and receiverId
        chatRoomId = createChatRoomId(currentUserId, receiverId)

        // Set up RecyclerView
        messageAdapter = MessageAdapter(messagesList, currentUserId)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatScreen)
            adapter = messageAdapter
        }


        // Load existing messages
        loadMessages()

        // Send message when button is clicked
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }
    private fun loadUserDetails(){

        val profileURL= intent.getStringExtra("profilePic")
        val username  = intent.getStringExtra("userName")


        binding.userName.text = username
        // here this, and in adapter if u use binding then binding.viewName.context
        Glide.with(this)
            .load(profileURL)
            .placeholder(R.drawable.cartoon_happy_eyes)
            .into(binding.userProfilePic)

    }

    private fun loadMessages() {
        val messagesRef = firestore.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("messageTime", Query.Direction.ASCENDING)

        messagesRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            messagesList.clear()

            snapshots?.let {
                for (doc in it.documents) {
                    val message = doc.toObject(Message::class.java)
                    message?.let { messagesList.add(it) }
                }
            }

            messageAdapter.notifyDataSetChanged()
        }
    }

    private fun sendMessage(messageText: String) {
        // Generate unique messageId
        val messageId = firestore.collection("messages").document().id
        val message = Message(
            messageId = messageId,
            messageText = messageText,
            messageTime = Timestamp.now(),
            messageType = "text",
            receiverId = receiverId,
            senderId = currentUserId
        )

        // Add the message to Firestore
        val chatRoomRef = firestore.collection("chatRooms").document(chatRoomId)

        chatRoomRef.collection("messages").document(messageId).set(message)
            .addOnSuccessListener {
                // Clear input field
                binding.editTextMessage.text.clear()

                // Update ChatRoom with the last message and timestamp
                chatRoomRef.set(
                    mapOf(
                        "lastMessage" to messageText,
                        "lastMessageTime" to Timestamp.now(),
                        "participants" to listOf(currentUserId, receiverId)
                    ),
                    SetOptions.merge() // Merge to keep other fields intact
                )
            }
            .addOnFailureListener { e ->

            }
    }

    private fun createChatRoomId(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) {
            "$senderId-$receiverId"
        } else {
            "$receiverId-$senderId"
        }
    }
}
