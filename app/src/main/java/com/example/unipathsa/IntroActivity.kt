package com.example.unipathsa

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class IntroActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "IntroActivity"
        private const val PREFS_NAME = "settings"
        private const val KEY_HAS_SEEN_INTRO = "hasSeenIntro"
    }

    private lateinit var vpIntroPages: ViewPager2
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var btnGetStarted: MaterialButton
    private lateinit var tvLoginLink: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        Log.d(TAG, "onCreate initialized")

        auth = FirebaseAuth.getInstance()

        bindViews()
        setupViewPager()
        setupListeners()
    }

    // Connect views
    private fun bindViews() {
        vpIntroPages = findViewById(R.id.vpIntroPages)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        btnGetStarted = findViewById(R.id.btnGetStarted)
        tvLoginLink = findViewById(R.id.tvLoginLink)
    }

    // Setup ViewPager2 pages and dot indicators
    private fun setupViewPager() {
        val pages = listOf(
            IntroPageData(
                title = "Automatic APS Calculator",
                subtitle = "Calculate your NSC APS score instantly and see which degree programmes you qualify for.",
                topBadge = "APS 32",
                bottomBadge = "✔ Qualifying Score"
            ),
            IntroPageData(
                title = "Explore Courses & Bursaries",
                subtitle = "Discover higher education programmes across South Africa and apply for bursaries.",
                topBadge = "8 Eligible",
                bottomBadge = "BSc Computer Science"
            ),
            IntroPageData(
                title = "Track Your Applications",
                subtitle = "Manage all your university applications, document uploads and progress steps in one place.",
                topBadge = "In Progress",
                bottomBadge = "Closes 30 Sep 2025"
            )
        )

        vpIntroPages.adapter = IntroPagerAdapter(pages)

        vpIntroPages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDotIndicators(position)
            }
        })
    }

    // Updates dot indicator widths and opacity based on active page
    private fun updateDotIndicators(position: Int) {
        val white = getColor(R.color.white)
        val translucent = getColor(R.color.divider)

        val activeWidth = (24 * resources.displayMetrics.density).toInt()
        val inactiveWidth = (8 * resources.displayMetrics.density).toInt()

        val dots = listOf(dot1, dot2, dot3)
        for (i in dots.indices) {
            val dot = dots[i]
            val params = dot.layoutParams
            if (i == position) {
                params.width = activeWidth
                dot.backgroundTintList = ColorStateList.valueOf(white)
            } else {
                params.width = inactiveWidth
                dot.backgroundTintList = ColorStateList.valueOf(translucent)
            }
            dot.layoutParams = params
        }
    }

    // Setup button click listeners
    private fun setupListeners() {
        btnGetStarted.setOnClickListener {
            markIntroSeenAndNavigate(
                if (auth.currentUser != null) HomeActivity::class.java else SignUpActivity::class.java
            )
        }

        tvLoginLink.setOnClickListener {
            markIntroSeenAndNavigate(
                if (auth.currentUser != null) HomeActivity::class.java else LoginActivity::class.java
            )
        }
    }

    // Stores hasSeenIntro flag in SharedPreferences and navigates to target activity
    private fun markIntroSeenAndNavigate(targetClass: Class<*>) {
        Log.d(TAG, "Setting hasSeenIntro = true and navigating to ${targetClass.simpleName}")
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SEEN_INTRO, true)
            .apply()

        startActivity(Intent(this, targetClass))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    data class IntroPageData(
        val title: String,
        val subtitle: String,
        val topBadge: String,
        val bottomBadge: String
    )

    private inner class IntroPagerAdapter(
        private val list: List<IntroPageData>
    ) : RecyclerView.Adapter<IntroPagerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvIntroTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvIntroSubtitle)
            val tvTopBadge: TextView = view.findViewById(R.id.tvIntroTopBadgeText)
            val tvBottomBadge: TextView = view.findViewById(R.id.tvIntroBottomBadgeText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_intro_page, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvTitle.text = item.title
            holder.tvSubtitle.text = item.subtitle
            holder.tvTopBadge.text = item.topBadge
            holder.tvBottomBadge.text = item.bottomBadge
        }

        override fun getItemCount(): Int = list.size
    }
}