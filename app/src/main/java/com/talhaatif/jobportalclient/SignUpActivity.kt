package com.talhaatif.jobportalclient

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.talhaatif.jobportalclient.databinding.ActivitySignUpBinding
import com.talhaatif.jobportalclient.firebase.Variables

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var progressDialog: ProgressDialog

    // Sample data for industries and roles
    private val industryRolesMap = mapOf(
        "Software" to listOf("Developer", "Tester", "Project Manager"),
        "Finance" to listOf("Accountant", "Financial Analyst", "Investment Banker"),
        "Healthcare" to listOf("Doctor", "Nurse", "Pharmacist")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")

        setupIndustryDropdown()
        setupRoleDropdown()

        binding.signup.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.cpassword.text.toString().trim()
            val industry = binding.actIndustry.text.toString().trim()
            val role = binding.actRole.text.toString().trim()
            val number = binding.number.text.toString().trim()
            val location = binding.actLocation.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword, industry, role, number,location)) {
                signUpUser(username, email, password, industry, role, number,location)
            }
        }

        binding.loginTv.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupIndustryDropdown() {
        val industries = industryRolesMap.keys.toList()
        val industryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, industries)
        binding.actIndustry.setAdapter(industryAdapter)
        binding.actIndustry.threshold = 1

        binding.actIndustry.setOnItemClickListener { _, _, position, _ ->
            val selectedIndustry = industries[position]
            setupRoleDropdown(selectedIndustry)
            binding.role.visibility = View.VISIBLE
        }
    }

    private fun setupRoleDropdown(selectedIndustry: String? = null) {
        val roles = industryRolesMap[selectedIndustry] ?: emptyList()
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.actRole.setAdapter(roleAdapter)
        binding.actRole.threshold = 1
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String, industry: String, role: String, number: String, location: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username is required"
            return false
        } else {
            binding.usernameLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Invalid email format"
            return false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            return false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return false
        } else {
            binding.passwordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.cpasswordLayout.error = "Confirm password is required"
            return false
        } else if (confirmPassword != password) {
            binding.cpasswordLayout.error = "Passwords do not match"
            return false
        } else {
            binding.cpasswordLayout.error = null
        }

        if (industry.isEmpty()) {
            binding.industry.error = "Industry is required"
            return false
        } else {
            binding.industry.error = null
        }

        if (location.isEmpty()) {
            binding.location.error = "Location is required"
            return false
        } else {
            binding.location.error = null
        }

        if (role.isEmpty()) {
            binding.role.error = "Role is required"
            return false
        } else {
            binding.role.error = null
        }

        if (number.isEmpty()) {
            binding.numberLayout.error = "Phone number is required"
            return false
        } else {
            binding.numberLayout.error = null
        }

        return true
    }

    private fun signUpUser(username: String, email: String, password: String, industry: String, role: String, number: String, location:String) {
        progressDialog.show()

        Variables.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = Variables.auth.currentUser?.uid
                    if (uid != null) {
                        val user = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "useremail" to email,
                            "industry" to industry,
                            "role" to role,
                            "number" to number,
                            "location" to location,
                            "cv" to "",
                            "experience" to "",
                            "currentJob" to "",
                            "profilePic" to "",
                            "appliedJobs" to listOf(
                                hashMapOf(
                                    "jid" to "",
                                    "jobResume" to "",
                                    "jobStatus" to ""
                                )
                            )

                        )

                        Variables.db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Variables.displayErrorMessage(" ${e.message}", this)
                                progressDialog.dismiss()
                            }
                    }
                } else {
                    progressDialog.dismiss()
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Variables.displayErrorMessage("This email address is already in use.", this)
                    } catch (e: Exception) {
                        Variables.displayErrorMessage("Authentication failed: ${e.message}", this)
                    }
                }
            }
    }
}
