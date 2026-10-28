package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignUpActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SignUpActivity"
    }

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var spinnerGrade: Spinner
    private lateinit var btnCreateAccount: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var tvLogIn: TextView

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-up failed", e)
            Toast.makeText(this, "Google sign-up failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        Log.d(TAG, "onCreate initialized")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Facebook sign-up success")
                handleFacebookAccessToken(result.accessToken.token)
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook sign-up cancelled")
                Toast.makeText(this@SignUpActivity, "Facebook sign-up cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "Facebook sign-up error", error)
                Toast.makeText(this@SignUpActivity, "Facebook sign-up failed: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })

        bindViews()
        setupGradeDropdown()
        setupListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun bindViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        spinnerGrade = findViewById(R.id.spinnerGrade)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        tvLogIn = findViewById(R.id.tvLogIn)
    }

    private fun setupGradeDropdown() {
        val grades = arrayOf(
            "Select your grade",
            "Grade 10",
            "Grade 11",
            "Grade 12",
            "Matriculant"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGrade.adapter = adapter
    }

    private fun setupListeners() {
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or InputType.TYPE_CLASS_TEXT
            } else {
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnCreateAccount.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val grade = spinnerGrade.selectedItem.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (grade == "Select your grade") {
                Toast.makeText(this, "Please select your grade", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    user?.updateProfile(profileUpdates)

                    // Save user document in Firestore
                    val userMap = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "grade" to grade,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    if (user != null) {
                        db.collection("users").document(user.uid).set(userMap, SetOptions.merge())
                    }

                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnFacebook.setOnClickListener {
            Log.d(TAG, "Facebook sign-up clicked")
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        }

        tvLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Google sign-up successful")
                checkFirstTimeUserProfile(result.user)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Google sign-up failed", e)
                handleAuthError(e)
            }
    }

    private fun handleFacebookAccessToken(token: String) {
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Facebook sign-up successful")
                checkFirstTimeUserProfile(result.user)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Facebook sign-up failed", e)
                handleAuthError(e)
            }
    }

    private fun handleAuthError(e: Exception) {
        if (e is FirebaseAuthUserCollisionException) {
            AlertDialog.Builder(this)
                .setTitle("Account Exists")
                .setMessage("An account with this email already exists using a different sign-in method. Please sign in using your existing method.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            Toast.makeText(this, "Authentication failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkFirstTimeUserProfile(user: FirebaseUser?) {
        if (user == null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    promptGradeSelectionAndSaveUser(user)
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }

    private fun promptGradeSelectionAndSaveUser(user: FirebaseUser) {
        val spinner = Spinner(this)
        val grades = arrayOf("Grade 10", "Grade 11", "Grade 12", "Matriculant")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, grades)
        spinner.adapter = adapter

        val container = android.widget.FrameLayout(this).apply {
            setPadding(48, 24, 48, 16)
            addView(spinner)
        }

        AlertDialog.Builder(this)
            .setTitle("Welcome to UniPath SA!")
            .setMessage("Please select your current grade to personalize your experience:")
            .setView(container)
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                val selectedGrade = spinner.selectedItem.toString()
                saveSocialUserToFirestore(user, selectedGrade)
            }
            .show()
    }

    private fun saveSocialUserToFirestore(user: FirebaseUser, grade: String) {
        val userMap = hashMapOf(
            "fullName" to (user.displayName ?: "Student"),
            "email" to (user.email ?: ""),
            "grade" to grade,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(user.uid)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Created user document in Firestore for ${user.uid}")
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }
}