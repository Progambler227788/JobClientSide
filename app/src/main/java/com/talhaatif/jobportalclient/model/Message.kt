package com.talhaatif.jobportalclient.model

data class Message (
    val messageId: String = "",
    val messageText: String = "",
    val messageTime: com.google.firebase.Timestamp? = null,
    val messageType: String = "text", // This can be "text", "image", etc.
    val receiverId: String = "",
    val senderId: String = ""
)