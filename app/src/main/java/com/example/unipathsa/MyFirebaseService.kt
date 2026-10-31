package com.example.unipathsa

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    // Called when the phone gets a new token
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        val uid = Firebase.auth.currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    // Called when a message arrives while the app is open
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "Message received: ${message.notification?.title}")
        NotificationHelper.show(
            this,
            message.notification?.title ?: "New update",
            message.notification?.body ?: ""
        )
    }
}