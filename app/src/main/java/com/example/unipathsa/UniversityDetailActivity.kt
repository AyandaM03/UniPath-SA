package com.example.unipathsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UniversityDetailActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "UniDetailActivity"
    }

    private lateinit var ivHeroImage: ImageView
    private lateinit var vBottomGradientOverlay: View
    private lateinit var llFallbackHeader: LinearLayout
    private lateinit var tvFallbackHeaderTitle: TextView

    private lateinit var ivUniLogoLarge: ImageView
    private lateinit var tvFallbackLogoInitial: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: View

    private lateinit var tvName: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvFreeBadge: TextView
    private lateinit var tvUserAps: TextView
    private lateinit var tvTypeShort: TextView
    private lateinit var cvMinApsTile: View
    private lateinit var tvMinApsValue: TextView

    // Application Fee Card Views
    private lateinit var tvFeeAmountDisplay: TextView
    private lateinit var tvFeeNoteDisplay: TextView
    private lateinit var tvFeeMetaDisplay: TextView
    private lateinit var btnConfirmFeeSite: MaterialButton

    private lateinit var tvLongDescription: TextView
    private lateinit var tvRequirements: TextView
    private lateinit var tvImportantDates: TextView

    private lateinit var btnVisitWebsite: MaterialButton
    private lateinit var btnApplyNow: MaterialButton

    private var isSaved = false
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentUniversity: University? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_university_detail)
        Log.d(TAG, "onCreate started")

        val university = intent.getSerializableExtra("UNIVERSITY_DATA") as? University
            ?: intent.getSerializableExtra("university") as? University

        if (university == null) {
            Log.e(TAG, "University data missing from intent extras")
            Toast.makeText(this, "Institution details not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUniversity = university
        bindViews()
        displayDetails(university)
        loadUserApsScore()
        checkSavedStatus(university)
        setupListeners(university)
    }

    // Connect XML views
    private fun bindViews() {
        ivHeroImage = findViewById(R.id.ivHeroImage)
        vBottomGradientOverlay = findViewById(R.id.vBottomGradientOverlay)
        llFallbackHeader = findViewById(R.id.llFallbackHeader)
        tvFallbackHeaderTitle = findViewById(R.id.tvFallbackHeaderTitle)

        ivUniLogoLarge = findViewById(R.id.ivUniLogoLarge)
        tvFallbackLogoInitial = findViewById(R.id.tvFallbackLogoInitial)
        btnSave = findViewById(R.id.btnSaveUni)
        btnBack = findViewById(R.id.btnBack)

        tvName = findViewById(R.id.tvUniName)
        tvLocation = findViewById(R.id.tvUniLocation)
        tvFreeBadge = findViewById(R.id.tvFreeToApplyBadge)
        tvUserAps = findViewById(R.id.tvUserApsDisplay)
        tvTypeShort = findViewById(R.id.tvUniTypeShort)
        cvMinApsTile = findViewById(R.id.cvMinApsTile)
        tvMinApsValue = findViewById(R.id.tvMinApsValue)

        tvFeeAmountDisplay = findViewById(R.id.tvFeeAmountDisplay)
        tvFeeNoteDisplay = findViewById(R.id.tvFeeNoteDisplay)
        tvFeeMetaDisplay = findViewById(R.id.tvFeeMetaDisplay)
        btnConfirmFeeSite = findViewById(R.id.btnConfirmFeeSite)

        tvLongDescription = findViewById(R.id.tvLongDescription)
        tvRequirements = findViewById(R.id.tvRequirements)
        tvImportantDates = findViewById(R.id.tvImportantDates)

        btnVisitWebsite = findViewById(R.id.btnVisitWebsite)
        btnApplyNow = findViewById(R.id.btnApplyNow)
    }

    // Populate university details and load images via Glide
    private fun displayDetails(uni: University) {
        tvName.text = uni.name
        val locationStr = buildString {
            if (uni.province.isNotBlank()) append(uni.province) else append(uni.location)
            if (uni.type.isNotBlank()) append(" • ").append(uni.type)
        }
        tvLocation.text = if (locationStr.isNotBlank()) locationStr else "South Africa"
        tvTypeShort.text = if (uni.type.isNotBlank()) uni.type else "Public"

        // Min APS tile
        if (uni.minAps > 0) {
            cvMinApsTile.visibility = View.VISIBLE
            tvMinApsValue.text = "${uni.minAps}+"
        } else {
            cvMinApsTile.visibility = View.GONE
        }

        // Application Fee Section
        tvFeeAmountDisplay.text = uni.getFormattedFeeText()

        if (uni.applicationFeeNote.isNotBlank()) {
            tvFeeNoteDisplay.visibility = View.VISIBLE
            tvFeeNoteDisplay.text = uni.applicationFeeNote
        } else {
            tvFeeNoteDisplay.visibility = View.GONE
        }

        val metaSb = StringBuilder()
        if (uni.applicationFeeYear.isNotBlank()) {
            metaSb.append("Applies to: ${uni.applicationFeeYear}")
        }
        if (uni.feeLastChecked.isNotBlank()) {
            if (metaSb.isNotEmpty()) metaSb.append(" • ")
            metaSb.append("Last checked: ${uni.feeLastChecked}")
        }

        if (metaSb.isNotEmpty()) {
            tvFeeMetaDisplay.visibility = View.VISIBLE
            tvFeeMetaDisplay.text = metaSb.toString()
        } else {
            tvFeeMetaDisplay.visibility = View.GONE
        }

        tvFreeBadge.visibility = if (uni.freeToApply || uni.applicationFee == 0) View.VISIBLE else View.GONE
        tvLongDescription.text = if (uni.longDescription.isNotBlank()) uni.longDescription else uni.description
        tvRequirements.text = if (uni.requirements.isNotBlank()) uni.requirements else "NSC Bachelor Pass with minimum required marks depending on faculty."
        tvImportantDates.text = if (uni.importantDates.isNotBlank()) "Closing Date: " + uni.importantDates else "Closing Date: 30 September"

        val initial = if (uni.name.isNotBlank()) uni.name.trim().take(1).uppercase() else "U"
        tvFallbackLogoInitial.text = initial
        tvFallbackHeaderTitle.text = uni.name

        // Hero Image
        val heroUrl = uni.getBestHeroUrl()
        if (heroUrl.isNotBlank()) {
            ivHeroImage.visibility = View.VISIBLE
            vBottomGradientOverlay.visibility = View.VISIBLE
            llFallbackHeader.visibility = View.GONE

            Glide.with(this)
                .load(heroUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivHeroImage)
        } else {
            showFallbackHeader(uni)
        }

        // Logo Image
        val logoUrl = uni.getBestLogoUrl()
        if (logoUrl.isNotBlank()) {
            ivUniLogoLarge.visibility = View.VISIBLE
            tvFallbackLogoInitial.visibility = View.GONE

            Glide.with(this)
                .load(logoUrl)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivUniLogoLarge)
        } else {
            ivUniLogoLarge.visibility = View.GONE
            tvFallbackLogoInitial.visibility = View.VISIBLE
        }
    }

    private fun showFallbackHeader(uni: University) {
        ivHeroImage.visibility = View.GONE
        vBottomGradientOverlay.visibility = View.GONE
        llFallbackHeader.visibility = View.VISIBLE
        tvFallbackHeaderTitle.text = uni.name
    }

    private fun loadUserApsScore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            tvUserAps.text = "Not calculated yet"
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val aps = (doc.getLong("aps") ?: 0L).toInt()
                    tvUserAps.text = if (aps > 0) aps.toString() else "Not calculated yet"
                } else {
                    tvUserAps.text = "Not calculated yet"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to read user APS score", e)
                tvUserAps.text = "Not calculated yet"
            }
    }

    private fun checkSavedStatus(uni: University) {
        BookmarkHelper.isSaved(uni.id) { saved ->
            isSaved = saved
            updateSaveButtonUI()
        }
    }

    private fun updateSaveButtonUI() {
        btnSave.text = if (isSaved) "Saved" else "Save"
        btnSave.setIconResource(if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline)
    }

    private fun setupListeners(uni: University) {
        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val itemDataMap = mapOf<String, Any>(
                "id" to uni.id,
                "title" to uni.name,
                "name" to uni.name,
                "subtitle" to uni.location,
                "type" to "university"
            )

            BookmarkHelper.toggleBookmark(
                activity = this,
                anchorView = btnSave,
                bookmarkImageView = null,
                itemId = uni.id,
                itemType = "university",
                itemData = itemDataMap
            ) { newSavedState ->
                isSaved = newSavedState
                updateSaveButtonUI()
            }
        }

        btnConfirmFeeSite.setOnClickListener {
            val feeUrl = uni.applicationFeeUrl.ifBlank { uni.websiteUrl }.trim()
            if (feeUrl.isBlank()) {
                Toast.makeText(this, "Official URL not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val url = if (feeUrl.startsWith("http")) feeUrl else "https://$feeUrl"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open URL", Toast.LENGTH_SHORT).show()
            }
        }

        btnApplyNow.setOnClickListener {
            val intent = Intent(this, ApplyActivity::class.java)
            intent.putExtra("UNIVERSITY_DATA", uni)
            startActivity(intent)
        }

        btnVisitWebsite.setOnClickListener {
            val webUrl = uni.websiteUrl.trim()
            if (webUrl.isEmpty()) {
                Toast.makeText(this, "Official website not specified", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val url = if (webUrl.startsWith("http")) webUrl else "https://$webUrl"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open website", Toast.LENGTH_SHORT).show()
            }
        }
    }
}