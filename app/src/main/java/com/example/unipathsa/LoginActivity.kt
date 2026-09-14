package com.example.unipathsa



import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    // Tracks whether the password is currently shown as plain text or masked with dots
    private var isPasswordVisible = false

    // FirebaseAuth handles all sign-in/sign-up requests to Firebase's servers
    private lateinit var auth: FirebaseAuth

    // Handles the Google Sign-In flow (opens Google's account picker, etc.)
    private lateinit var googleSignInClient: GoogleSignInClient

    // View references — declared here so any function in this class can use them,
    // instead of calling findViewById() over and over
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var cbRememberMe: CheckBox
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvSignUp: TextView

    // This is the modern replacement for the old startActivityForResult().
    // It "launches" the Google sign-in screen and waits for a result to come back,
    // then runs the code inside { } once the user picks an account (or cancels).
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Try to pull the signed-in Google account out of the result
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // getResult() throws an exception if something went wrong (e.g. user cancelled)
            val account = task.getResult(ApiException::class.java)
            // Pass the account's ID token on to Firebase to finish logging in
            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Loads and displays the activity_login.xml layout
        setContentView(R.layout.activity_login)

        // Get a reference to Firebase's authentication system
        auth = FirebaseAuth.getInstance()

        // Set up what info we want back from Google when a user signs in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // default_web_client_id is auto-generated from google-services.json —
            // it identifies THIS Firebase project to Google's servers
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        bindViews()       // grab all the view references
        setupListeners()  // attach all the click/tap behavior
    }

    // Connects each XML view (by its android:id) to a Kotlin variable
    private fun bindViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvSignUp = findViewById(R.id.tvSignUp)
    }

    // All click listeners live here, so onCreate() stays short and readable
    private fun setupListeners() {

        // --- Password show/hide toggle ---
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible // flip the current state

            // Change how the EditText displays its text based on the new state
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or InputType.TYPE_CLASS_TEXT
            } else {
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            }
            // Without this, the cursor can jump to the start of the text after switching modes
            etPassword.setSelection(etPassword.text.length)
        }

        // --- Email/Password login button ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Basic validation before even contacting Firebase
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // stop here, don't call Firebase
            }

            // Ask Firebase to check these credentials against its user database
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Runs if login succeeded
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    // Runs if login failed (wrong password, no such user, etc.)
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // --- Google Sign-In button ---
        btnGoogle.setOnClickListener {
            // Builds the intent (request) that opens Google's account picker screen
            val signInIntent = googleSignInClient.signInIntent
            // Launches it and waits for the result via googleSignInLauncher above
            googleSignInLauncher.launch(signInIntent)
        }

        // --- "Sign Up" link at the bottom ---
        tvSignUp.setOnClickListener {
            // Moves to the Sign Up screen
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // --- "Forgot password?" link ---
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // Need an email address before we can send a reset link
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase handles sending the actual reset email — no backend code needed
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Takes the token we got from Google and hands it to Firebase to finish signing in
    private fun firebaseAuthWithGoogle(idToken: String?) {
        // Wraps the Google token into a format Firebase understands
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Ask Firebase to sign the user in (or create an account) using that Google identity
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firebase auth failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}