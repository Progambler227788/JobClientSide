package com.talhaatif.jobportalclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.talhaatif.jobportalclient.adapter.ViewPagerAdapter
import com.talhaatif.jobportalclient.firebase.Variables
import com.talhaatif.jobportalclient.databinding.ActivityApplyJobsBinding

class ApplyJobsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplyJobsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = Variables.db
        auth = Variables.auth

        // Retrieve the job ID passed from the adapter
        val jobId = intent.getStringExtra("jid")

        // Check if the user has uploaded a CV
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val cv = document.getString("cv")
                        if (cv.isNullOrEmpty()) {
                            Toast.makeText(this, "Please upload your CV before applying.", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity if no CV is found
                        } else {
                            populateJobDetails(jobId)
                            binding.applyButton.setOnClickListener {
                                applyForJob(jobId, uid, cv)
                            }
                        }
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateJobDetails(jobId: String?) {
        // Populate job details based on jobId
        jobId?.let {
            firestore.collection("jobs").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val jobTitle = document.getString("jobTitle")
                        val jobCompany = document.getString("jobCompany")
                        val jobDescription = document.getString("jobDescription")
                        val jobSalaryStart = document.getString("jobSalaryStartRange")
                        val jobSalaryEnd = document.getString("jobSalaryEndRange")
                        val location = document.getString("jobLocation")
                        val logo = document.getString("jobImage")

                        // Populate other fields as needed

                        // Update UI with job details
                        binding.jobRole.text = jobTitle
                        binding.jobCompany.text = jobCompany

                        binding.salaryRange.text = "$${jobSalaryStart} - $${jobSalaryEnd}"
                        binding.locationName.text = location

                        val companyOverview = "We are a leading company in the tech industry, known for innovation and a vibrant workplace. Our team is dedicated to pushing the boundaries of technology to deliver cutting-edge solutions."
                        val missionVision = "Our mission is to create groundbreaking products that shape the future of technology. Our vision is to empower businesses and individuals to achieve their full potential through our solutions."
                        val totalEmployees = "500+ Employees worldwide."


                        setupViewPager(jobDescription ?: "No description available", companyOverview, missionVision, totalEmployees)
                        // Load the company logo if it's available
                        Glide.with(binding.jobIcon)
                            .load(logo)
                            .placeholder(R.drawable.cartoon_happy_eyes)
                            .into(binding.jobIcon)

                        // Set other fields as needed
                    } else {
                        Toast.makeText(this, "Job not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch job details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupViewPager(jobDescription: String, companyOverview: String, missionVision: String, totalEmployees: String) {
        val adapter = ViewPagerAdapter(supportFragmentManager, jobDescription, companyOverview, missionVision, totalEmployees)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun applyForJob(jobId: String?, uid: String, cv: String) {
        val appliedJob = hashMapOf(
            "jid" to jobId,
            "jobResume" to cv,
            "jobStatus" to "pending"
        )

        // Update the user's appliedJobs array
        firestore.collection("users").document(uid)
            .update("appliedJobs", FieldValue.arrayUnion(appliedJob))
            .addOnSuccessListener {
                // Update the job document's jobAppliedUsers field
                firestore.collection("jobs").document(jobId!!)
                    .update("jobAppliedUsers", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Job application submitted.", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after applying
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update job document: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update user document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
