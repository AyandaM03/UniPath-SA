package com.example.unipathsa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "NotificationsActivity"
    }

    private lateinit var rvNotifications: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var adapter: NotificationAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationList = mutableListOf<NotificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navNotifications)

        bindViews()
        setupRecyclerView()
        loadNotificationsFromFirestore()
    }

    // Connect XML views to activity variables
    private fun bindViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        llEmptyState = findViewById(R.id.llEmptyNotifications)
    }

    // Setup RecyclerView with LayoutManager and Adapter
    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList) { notification ->
            if (!notification.isRead) {
                markNotificationAsRead(notification)
            }
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    // Reads user notifications from Firestore users/{uid}/notifications ordered newest first
    private fun loadNotificationsFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "User not authenticated - cannot load notifications")
            showEmptyState(true)
            return
        }

        db.collection("users").document(uid).collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                notificationList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        notificationList.add(NotificationModel.fromFirestore(doc))
                    }
                    Log.d(TAG, "Loaded ${notificationList.size} notifications from Firestore")
                    showEmptyState(false)
                } else {
                    Log.d(TAG, "No notifications found in Firestore")
                    showEmptyState(true)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load notifications from Firestore", e)
                Toast.makeText(this, "Failed to load notifications: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                showEmptyState(notificationList.isEmpty())
            }
    }

    // Marks a notification as read (isRead = true) in Firestore
    private fun markNotificationAsRead(notification: NotificationModel) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("notifications").document(notification.id)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d(TAG, "Marked notification ${notification.id} as read")
                loadNotificationsFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error marking notification as read", e)
            }
    }

    // Toggle empty state visibility
    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            llEmptyState.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
        }
    }

    // RecyclerView Adapter for Notification items
    private inner class NotificationAdapter(
        private val list: List<NotificationModel>,
        private val onItemClick: (NotificationModel) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivNotifIcon: ImageView = view.findViewById(R.id.ivNotifIcon)
            val tvNotifTitle: TextView = view.findViewById(R.id.tvNotifTitle)
            val tvNotifMessage: TextView = view.findViewById(R.id.tvNotifMessage)
            val tvNotifTime: TextView = view.findViewById(R.id.tvNotifTime)
            val vUnreadDot: View = view.findViewById(R.id.vUnreadDot)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvNotifTitle.text = item.title
            holder.tvNotifMessage.text = item.message

            // Date formatting
            if (item.createdAt != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                holder.tvNotifTime.text = sdf.format(item.createdAt.toDate())
            } else {
                holder.tvNotifTime.text = "Recent"
            }

            // Unread state styling
            if (item.isRead) {
                holder.vUnreadDot.visibility = View.GONE
                holder.ivNotifIcon.imageTintList = getColorStateList(R.color.text_tertiary)
            } else {
                holder.vUnreadDot.visibility = View.VISIBLE
                holder.ivNotifIcon.imageTintList = getColorStateList(R.color.primary)
            }

            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = list.size
    }
}