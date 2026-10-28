package com.example.unipathsa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView
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
        setupListeners()
        setupRecyclerView()
        loadNotificationsFromFirestore()
    }

    private fun bindViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        llEmptyState = findViewById(R.id.llEmptyNotifications)
        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            // Already on Notifications screen
            Toast.makeText(this, "You are on Notifications", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList) { notification ->
            if (!notification.isRead) {
                markNotificationAsRead(notification)
            }
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

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
                var unreadCount = 0

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val notif = NotificationModel.fromFirestore(doc)
                        notificationList.add(notif)
                        if (!notif.isRead) unreadCount++
                    }
                    showEmptyState(false)
                } else {
                    showEmptyState(true)
                }

                if (unreadCount > 0) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
                } else {
                    tvNotifBadge.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load notifications from Firestore", e)
                Toast.makeText(this, "Failed to load notifications: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                showEmptyState(notificationList.isEmpty())
            }
    }

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

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            llEmptyState.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
        }
    }

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

            if (item.createdAt != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                holder.tvNotifTime.text = sdf.format(item.createdAt.toDate())
            } else {
                holder.tvNotifTime.text = "Recent"
            }

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