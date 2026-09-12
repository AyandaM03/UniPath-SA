package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    // Tracks whether the password is shown as plain text or masked
    private var isPasswordVisible = false

    // FirebaseAuth handles account creation
    private lateinit var auth: FirebaseAuth

    // View references
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var spinnerGrade: Spinner
    private lateinit var btnCreateAccount: Button
    private lateinit var tvLogIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        bindViews()
        setupGradeDropdown()
        setupListeners()
    }

    // Connects each XML view to a Kotlin variable
    private fun bindViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        spinnerGrade = findViewById(R.id.spinnerGrade)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvLogIn = findViewById(R.id.tvLogIn)
    }

    // Fills the grade Spinner (dropdown) with a fixed list of options
    private fun setupGradeDropdown() {
        val grades = arrayOf(
            "Select your grade",
            "Grade 10",
            "Grade 11",
            "Grade 12",
            "Matriculant"
        )
        // ArrayAdapter turns a plain list into something the Spinner can display
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGrade.adapter = adapter
    }

    private fun setupListeners() {

        // --- Password show/hide toggle (same pattern as Login) ---
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or InputType.TYPE_CLASS_TEXT
            } else {
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // --- Create Account button ---
        btnCreateAccount.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val grade = spinnerGrade.selectedItem.toString()

            // Basic validation before contacting Firebase
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

            // Ask Firebase to create a brand-new account with this email/password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    // Account created successfully — update their display name with Full Name
                    val user = authResult.user
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    user?.updateProfile(profileUpdates)

                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    // TODO: save "grade" somewhere useful (e.g. Firestore) if needed later
                    // TODO: navigate to your Dashboard/Home activity here
                }
                .addOnFailureListener { e ->
                    // Runs if account creation failed (email already used, weak password, etc.)
                    Toast.makeText(this, "Sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // --- "Log In" link at the bottom ---
        tvLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // closes SignUp so back button doesn't loop between the two
        }
    }
}