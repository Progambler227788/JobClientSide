package com.talhaatif.jobportalclient.model

data class ChatRoom(
    val chatRoomId: String = "",
    val lastMessage: String = "",
    val lastMessageTime: com.google.firebase.Timestamp? = null,
    val messages: String = "", // Firestore path to messages subcollection
    val participants: List<String> = emptyList()
)
