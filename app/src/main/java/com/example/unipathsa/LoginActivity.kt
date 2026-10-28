package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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

class LoginActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "LoginActivity"
    }

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var cbRememberMe: CheckBox
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var tvSignUp: TextView

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed", e)
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate started")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configure Facebook Login CallbackManager
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Facebook login success with token")
                handleFacebookAccessToken(result.accessToken.token)
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook login cancelled by user")
                Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "Facebook login error", error)
                Toast.makeText(this@LoginActivity, "Facebook sign-in failed: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })

        bindViews()
        setupListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun bindViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        tvSignUp = findViewById(R.id.tvSignUp)
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

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d(TAG, "Email/password login successful")
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Email/password login failed", e)
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnFacebook.setOnClickListener {
            Log.d(TAG, "Facebook button clicked: requesting permissions")
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Google Firebase auth successful")
                checkFirstTimeUserProfile(result.user)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Google Firebase auth failed", e)
                handleAuthError(e)
            }
    }

    private fun handleFacebookAccessToken(token: String) {
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Facebook Firebase auth successful")
                checkFirstTimeUserProfile(result.user)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Facebook Firebase auth failed", e)
                handleAuthError(e)
            }
    }

    // Handles user account collisions and auth errors
    private fun handleAuthError(e: Exception) {
        if (e is FirebaseAuthUserCollisionException) {
            AlertDialog.Builder(this)
                .setTitle("Account Exists")
                .setMessage("An account with this email already exists using a different sign-in method (e.g. Email/Password or Google). Please sign in using your existing method.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            Toast.makeText(this, "Authentication failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // On first sign-in, creates users/{uid} document and asks for learner's grade
    private fun checkFirstTimeUserProfile(user: FirebaseUser?) {
        if (user == null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Log.d(TAG, "First time social sign-in for ${user.uid}: asking for grade")
                    promptGradeSelectionAndSaveUser(user)
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking user profile existence", e)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }

    // Prompt dialog for grade selection on social first-time sign in
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
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save user document in Firestore", e)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }
}