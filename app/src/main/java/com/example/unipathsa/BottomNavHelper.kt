package com.example.unipathsa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {

    fun setup(activity: AppCompatActivity, currentItemId: Int) {
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNav) ?: return

        bottomNav.selectedItemId = currentItemId

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true

            val targetActivity = when (item.itemId) {
                R.id.nav_home -> HomeActivity::class.java
                R.id.nav_institutions -> InstitutionsActivity::class.java
                R.id.nav_explore -> ExploreActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                else -> return@setOnItemSelectedListener false
            }

            activity.startActivity(Intent(activity, targetActivity))
            activity.overridePendingTransition(0, 0)
            activity.finish()
            true
        }
    }
}