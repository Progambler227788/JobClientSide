package com.talhaatif.jobportalclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.talhaatif.jobportalclient.firebase.Variables

class MyFirebaseMessagingService {  //  : FirebaseMessagingService() {

//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        // Handle the incoming message here
//        val title = remoteMessage.notification?.title
//        val body = remoteMessage.notification?.body
//
//        if (title != null && body != null) {
//            sendNotification(title, body)
//        }
//    }
//
//    private fun sendNotification(title: String, message: String) {
//        val notificationId = 1001
//        val channelId = "job_notifications_channel"
//
//        // Check for notification permission on Android 13 and higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // Consider prompting the user to grant the permission if not granted
//            Log.w("FCM", "Notification permission not granted.")
//            return
//        }
//
//        // Create the notification intent
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//
//        // Use FLAG_IMMUTABLE for PendingIntent
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Create the notification
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.cartoon_happy_eyes)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//
//        // Display the notification
//        with(NotificationManagerCompat.from(this)) {
//            // Android O requires a Notification Channel
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    channelId,
//                    "Job Notifications",
//                    NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description = "Channel for job application status notifications"
//                }
//                createNotificationChannel(channel)
//            }
//
//            notify(notificationId, builder.build())
//        }
//    }
//
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.d("FCM", "New token: $token")
//        val uid = Variables.auth.uid
//        if (uid != null) {
//            Variables.db.collection("users")
//                .document(uid)
//                .update("fcmToken", token)
//                .addOnSuccessListener {
//                    Log.d("FCM", "FCM token updated successfully.")
//                }
//                .addOnFailureListener { e ->
//                    Log.w("FCM", "Error updating FCM token", e)
//                }
//        }
//    }

}
