package com.example.unipathsa

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object NavigationHelper {

    private const val TAG = "NavigationHelper"

    /**
     * Wires up side navigation drawer for any main activity screen.
     * @param activity current activity
     * @param activeNavItemId menu item resource ID to highlight (e.g. R.id.navHome)
     */
    fun setupDrawer(activity: AppCompatActivity, activeNavItemId: Int = 0) {
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
        val btnMenu = activity.findViewById<ImageView>(R.id.btnMenu)

        if (drawerLayout == null) {
            Log.d(TAG, "DrawerLayout R.id.drawerLayout not found in ${activity.javaClass.simpleName}")
            return
        }

        // Attach hamburger click listener
        btnMenu?.setOnClickListener {
            Log.d(TAG, "Hamburger icon clicked in ${activity.javaClass.simpleName}")
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Drawer state listener for logging
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                Log.d(TAG, "Drawer opened in ${activity.javaClass.simpleName}")
            }
        })

        // Populate header with user profile from Firebase
        loadDrawerHeaderData(activity)

        // Highlight active navigation item
        highlightActiveNavItem(activity, activeNavItemId)

        // Wire menu items
        setupMenuClickListeners(activity, drawerLayout)
    }

    // Fetches full name and grade from Firestore users/{uid} for the header
    private fun loadDrawerHeaderData(activity: AppCompatActivity) {
        val tvName = activity.findViewById<TextView>(R.id.tvDrawerUserName) ?: return
        val tvGrade = activity.findViewById<TextView>(R.id.tvDrawerUserGrade)
        val tvAvatar = activity.findViewById<TextView>(R.id.tvDrawerAvatar)
        val tvNotifBadge = activity.findViewById<TextView>(R.id.tvDrawerNotifBadge)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            tvName.text = "Student"
            tvGrade?.text = "Matriculant / High School"
            tvAvatar?.text = "S"
            return
        }

        val displayName = if (!user.displayName.isNullOrBlank()) user.displayName!! else "Student"
        tvName.text = displayName
        tvAvatar?.text = displayName.trim().take(1).uppercase()

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val fullName = doc.getString("fullName") ?: doc.getString("name") ?: displayName
                    val grade = doc.getString("grade") ?: doc.getString("school") ?: "Matriculant"

                    tvName.text = fullName
                    tvGrade?.text = grade
                    if (fullName.isNotBlank()) {
                        tvAvatar?.text = fullName.trim().take(1).uppercase()
                    }
                    Log.d(TAG, "Loaded drawer profile for $fullName ($grade)")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load drawer profile from Firestore", e)
            }

        // Unread notifications badge
        db.collection("users").document(user.uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot?.size() ?: 0
                if (count > 0 && tvNotifBadge != null) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                } else {
                    tvNotifBadge?.visibility = View.GONE
                }
            }
    }

    // Highlights active menu item in the drawer
    private fun highlightActiveNavItem(activity: AppCompatActivity, activeNavItemId: Int) {
        if (activeNavItemId == 0) return

        val navIds = intArrayOf(
            R.id.navHome, R.id.navInstitutions, R.id.navExplore, R.id.navApsCalc,
            R.id.navApplications, R.id.navSaved, R.id.navDocuments, R.id.navNotifications,
            R.id.navProfile, R.id.navSettings
        )

        val whiteColor = ContextCompat.getColor(activity, R.color.white)

        val highlightBg = GradientDrawable().apply {
            setColor(Color.parseColor("#33FFFFFF")) // translucent white
            cornerRadius = 16f
            setStroke(2, Color.parseColor("#4C8DFF")) // bright blue border indicator
        }

        for (id in navIds) {
            val item = activity.findViewById<View>(id) ?: continue
            if (id == activeNavItemId) {
                if (item is TextView) {
                    item.setTextColor(whiteColor)
                }
                item.background = highlightBg
            } else {
                if (item is TextView) {
                    item.setTextColor(Color.parseColor("#E2E8F0"))
                }
                item.background = null
            }
        }
    }

    // Wire up drawer navigation item clicks
    private fun setupMenuClickListeners(activity: AppCompatActivity, drawerLayout: DrawerLayout) {
        val clickMap = mapOf(
            R.id.navHome to HomeActivity::class.java,
            R.id.navInstitutions to InstitutionsActivity::class.java,
            R.id.navExplore to ExploreActivity::class.java,
            R.id.navApsCalc to ApsCalculatorActivity::class.java,
            R.id.navApplications to ApplicationsActivity::class.java,
            R.id.navSaved to SavedActivity::class.java,
            R.id.navDocuments to DocumentsActivity::class.java,
            R.id.navNotifications to NotificationsActivity::class.java,
            R.id.navProfile to ProfileActivity::class.java,
            R.id.navSettings to SettingsActivity::class.java
        )

        for ((viewId, targetClass) in clickMap) {
            activity.findViewById<View>(viewId)?.setOnClickListener {
                Log.d(TAG, "Drawer item clicked: ${targetClass.simpleName}")
                drawerLayout.closeDrawer(GravityCompat.START)
                if (activity::class.java != targetClass) {
                    val intent = Intent(activity, targetClass)
                    activity.startActivity(intent)
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    if (targetClass != HomeActivity::class.java && activity !is HomeActivity) {
                        activity.finish()
                    }
                }
            }
        }

        // Log Out click
        activity.findViewById<TextView>(R.id.navLogout)?.setOnClickListener {
            Log.d(TAG, "Log Out clicked in drawer")
            drawerLayout.closeDrawer(GravityCompat.START)

            try {
                LoginManager.getInstance().logOut()
            } catch (e: Exception) {
                Log.w(TAG, "Error logging out of Facebook SDK", e)
            }

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            activity.finish()
        }
    }
}