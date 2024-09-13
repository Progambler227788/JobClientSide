package com.talhaatif.jobportalclient

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.talhaatif.jobportalclient.adapter.MessageAdapter
import com.talhaatif.jobportalclient.databinding.ActivityChatScreenBinding
import com.talhaatif.jobportalclient.firebase.Variables
import com.talhaatif.jobportalclient.model.Message
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatScreen : AppCompatActivity() {
    private lateinit var binding: ActivityChatScreenBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messagesList = mutableListOf<Message>()
    private val firestore = Variables.db
    private val storage = Variables.storageRef
    private var mediaRecorder: MediaRecorder? = null
    private var fileName: String = ""
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

        // Listen for text changes in the EditText
        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Change drawable when text is entered
                if (s.isNullOrEmpty()) {
                    binding.buttonSend.setImageResource(R.drawable.ic_voice) // Default icon
                    binding.buttonSend.tag = "Voice"
                } else {
                    binding.buttonSend.setImageResource(R.drawable.ic_send) // Change to send icon
                    binding.buttonSend.tag = "Text"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Send message when button is clicked
        binding.buttonSend.setOnClickListener {
            when (binding.buttonSend.tag) {
                "Text" -> {
                    val messageText = binding.editTextMessage.text.toString().trim()
                    if (messageText.isNotEmpty()) {
                        sendMessage(messageText,"text")
                    }
                }
                "Voice" -> startRecording()
                "Stop" -> stopRecording()
            }
        }
    }
    // Voice Feature
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 200)
            return
        }

        binding.buttonSend.setImageResource(R.drawable.ic_stop)
        binding.buttonSend.tag = "Stop"

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        fileName = "${externalCacheDir?.absolutePath}/VOICE_${timeStamp}.3gp"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(fileName)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        binding.buttonSend.setImageResource(R.drawable.ic_voice)
        binding.buttonSend.tag = "Voice"

        uploadVoiceNote()
    }

    private fun uploadVoiceNote() {
        val file = File(fileName)
        val fileUri = Uri.fromFile(file)
        val fileRef = storage.child("UserChatsVoices/${file.name}")

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val audioUrl = uri.toString()
                    sendMessage(audioUrl, "voice")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload audio", Toast.LENGTH_SHORT).show()
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

    private fun sendMessage(messageText: String, type : String) {
        // Generate unique messageId
        val messageId = firestore.collection("messages").document().id
        val message = Message(
            messageId = messageId,
            messageText = messageText,
            messageTime = Timestamp.now(),
            messageType =  type,
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
