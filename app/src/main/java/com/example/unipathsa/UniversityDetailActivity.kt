package com.example.unipathsa

import android.content.Intent
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UniversityDetailActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "UniDetailActivity"
    }

    private lateinit var ivHeroImage: ImageView
    private lateinit var vBottomGradientOverlay: View
    private lateinit var llFallbackHeader: LinearLayout
    private lateinit var tvFallbackHeaderTitle: TextView
    private lateinit var ivFallbackHeaderLogo: ImageView

    private lateinit var ivUniLogoLarge: ImageView
    private lateinit var tvFallbackLogoInitial: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: View

    private lateinit var tvName: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvFreeBadge: TextView
    private lateinit var tvUserAps: TextView
    private lateinit var tvTypeShort: TextView
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
        ivFallbackHeaderLogo = findViewById(R.id.ivFallbackHeaderLogo)

        ivUniLogoLarge = findViewById(R.id.ivUniLogoLarge)
        tvFallbackLogoInitial = findViewById(R.id.tvFallbackLogoInitial)
        btnSave = findViewById(R.id.btnSaveUni)
        btnBack = findViewById(R.id.btnBack)

        tvName = findViewById(R.id.tvUniName)
        tvLocation = findViewById(R.id.tvUniLocation)
        tvFreeBadge = findViewById(R.id.tvFreeToApplyBadge)
        tvUserAps = findViewById(R.id.tvUserApsDisplay)
        tvTypeShort = findViewById(R.id.tvUniTypeShort)
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

        tvFreeBadge.visibility = if (uni.freeToApply) View.VISIBLE else View.GONE
        tvLongDescription.text = if (uni.longDescription.isNotBlank()) uni.longDescription else uni.description
        tvRequirements.text = if (uni.requirements.isNotBlank()) uni.requirements else "NSC Bachelor Pass with minimum required marks depending on faculty."
        tvImportantDates.text = if (uni.importantDates.isNotBlank()) "Closing Date: " + uni.importantDates else "Closing Date: 30 September"

        val initial = if (uni.name.isNotBlank()) uni.name.trim().take(1).uppercase() else "U"
        tvFallbackLogoInitial.text = initial
        tvFallbackHeaderTitle.text = uni.name

        // Load Hero Campus Image
        val heroUrl = uni.getBestHeroUrl()
        if (heroUrl.isNotBlank()) {
            Log.d(TAG, "Loading hero image for ${uni.name} from $heroUrl")
            ivHeroImage.visibility = View.VISIBLE
            vBottomGradientOverlay.visibility = View.VISIBLE
            llFallbackHeader.visibility = View.GONE

            Glide.with(this)
                .load(heroUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "Failed to load hero image for ${uni.name}", e)
                        showFallbackHeader(uni)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "Successfully loaded hero image for ${uni.name}")
                        return false
                    }
                })
                .into(ivHeroImage)
        } else {
            Log.d(TAG, "No hero image URL found for ${uni.name}, displaying fallback navy header")
            showFallbackHeader(uni)
        }

        // Load Logo Image
        val logoUrl = uni.getBestLogoUrl()
        if (logoUrl.isNotBlank()) {
            Log.d(TAG, "Loading logo for ${uni.name} from $logoUrl")
            ivUniLogoLarge.visibility = View.VISIBLE
            tvFallbackLogoInitial.visibility = View.GONE

            Glide.with(this)
                .load(logoUrl)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "Failed to load logo for ${uni.name}", e)
                        ivUniLogoLarge.visibility = View.GONE
                        tvFallbackLogoInitial.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "Successfully loaded logo for ${uni.name}")
                        ivUniLogoLarge.visibility = View.VISIBLE
                        tvFallbackLogoInitial.visibility = View.GONE
                        return false
                    }
                })
                .into(ivUniLogoLarge)
        } else {
            Log.d(TAG, "No logo URL found for ${uni.name}, displaying initial '$initial'")
            ivUniLogoLarge.visibility = View.GONE
            tvFallbackLogoInitial.visibility = View.VISIBLE
        }
    }

    // Show fallback navy-blue gradient header when hero image is missing or fails
    private fun showFallbackHeader(uni: University) {
        ivHeroImage.visibility = View.GONE
        vBottomGradientOverlay.visibility = View.GONE
        llFallbackHeader.visibility = View.VISIBLE
        tvFallbackHeaderTitle.text = uni.name
    }

    // Read user's calculated APS score from profile
    private fun loadUserApsScore() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val aps = doc.getLong("aps") ?: 0L
                    tvUserAps.text = aps.toString()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to read user APS score", e)
            }
    }

    // Check bookmark state in Firestore
    private fun checkSavedStatus(uni: University) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("savedItems").document(uni.id)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    isSaved = true
                    updateSaveButtonUI()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking saved status", e)
            }
    }

    // Update Save button icon and text
    private fun updateSaveButtonUI() {
        btnSave.text = if (isSaved) "Saved" else "Save"
        btnSave.setIconResource(if (isSaved) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
    }

    // Attach button listeners
    private fun setupListeners(uni: University) {
        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Please log in to save items", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val itemRef = db.collection("users").document(uid).collection("savedItems").document(uni.id)

            if (isSaved) {
                itemRef.delete().addOnSuccessListener {
                    isSaved = false
                    updateSaveButtonUI()
                    Toast.makeText(this, "${uni.name} removed from saved items", Toast.LENGTH_SHORT).show()
                }
            } else {
                val savedData = hashMapOf(
                    "id" to uni.id,
                    "itemType" to "university",
                    "title" to uni.name,
                    "subtitle" to uni.location,
                    "details" to uni.type,
                    "savedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                itemRef.set(savedData, SetOptions.merge()).addOnSuccessListener {
                    isSaved = true
                    updateSaveButtonUI()
                    Toast.makeText(this, "${uni.name} saved to your profile!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnApplyNow.setOnClickListener {
            val intent = Intent(this, ApplicationsActivity::class.java)
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
                Log.d(TAG, "Opening website URL: $url")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch website browser intent", e)
                Toast.makeText(this, "Could not open website", Toast.LENGTH_SHORT).show()
            }
        }
    }
}