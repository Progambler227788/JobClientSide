package com.talhaatif.jobportalclient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.talhaatif.jobportalclient.databinding.FragmentProfileBinding
import com.talhaatif.jobportalclient.firebase.Util
import com.talhaatif.jobportalclient.firebase.Variables
import java.util.UUID

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var cvUri: Uri? = null

    private val industryRolesMap = mapOf(
        "Software" to listOf("Developer", "Tester", "Project Manager"),
        "Finance" to listOf("Accountant", "Financial Analyst", "Investment Banker"),
        "Healthcare" to listOf("Doctor", "Nurse", "Pharmacist")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupIndustryDropdown() {
        val industries = industryRolesMap.keys.toList()
        val industryAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, industries)
        binding.actIndustry.setAdapter(industryAdapter)

        binding.actIndustry.setOnItemClickListener { parent, _, position, _ ->
            val selectedIndustry = parent.getItemAtPosition(position) as String
            updateRoleDropdown(selectedIndustry)
        }
    }

    private fun setupRoleDropdown() {
        binding.actRole.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.actIndustry.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please select an industry first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateRoleDropdown(selectedIndustry: String) {
        val roles = industryRolesMap[selectedIndustry]
        val roleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roles ?: emptyList()
        )
        binding.actRole.setAdapter(roleAdapter)
        binding.actRole.setText("") // Clear previous selection
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Variables.db

        binding.logo.setOnClickListener {
            pickImage()
        }

        setupIndustryDropdown()
        setupRoleDropdown()

        binding.logout.setOnClickListener {
            val variable = Util()
            variable.saveLocalData(requireContext(), "uid", "")
            variable.saveLocalData(requireContext(), "auth", "false")
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.updateProfile.setOnClickListener {
            updateUserProfile()
        }

        binding.updateCV.setOnClickListener {
            uploadResume()
        }

        binding.downloadCV.setOnClickListener {
            downloadCv()
        }

        fetchUserDetails()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private val PICK_PDF_REQUEST = 2

    private fun pickPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.logo.setImageURI(imageUri)
        }
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            cvUri = data.data
            uploadResume()
        }
    }

    private fun updateUserProfile() {
        val userId = Variables.auth.currentUser?.uid ?: return
        val username = binding.username.text.toString()
        val phoneNumber = binding.number.text.toString()
        val industry = binding.actIndustry.text.toString()
        val role = binding.actRole.text.toString()

        val userMap = mapOf(
            "username" to username,
            "number" to phoneNumber,
            "industry" to industry,
            "role" to role
        )

        firestore.collection("users").document(userId)
            .update(userMap)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Profile updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage(
                    "Failed to update profile: ${e.message}",
                    requireContext()
                )
            }

        // Update profile image if new one is selected
        if (imageUri != null) {
            uploadProfileImage()
        }
    }

    private fun uploadProfileImage() {
        val userId = Variables.auth.currentUser?.uid ?: return
        if (imageUri != null) {
            val fileName = "UserProfiles/${userId}_${UUID.randomUUID()}.jpg"
            val fileRef = Variables.storageRef.child(fileName)
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        updateProfileImageInFirestore(imageUrl)
                    }
                }
                .addOnFailureListener { exception ->
                    Variables.displayErrorMessage(
                        "Failed to upload image: ${exception.message}",
                        requireContext()
                    )
                }
        } else {
            Variables.displayErrorMessage("No image selected", requireContext())
        }
    }

    private fun updateProfileImageInFirestore(imageUrl: String) {
        val userId = Variables.auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("profilePic", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Profile image updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage(
                    "Failed to update profile image: ${e.message}",
                    requireContext()
                )
            }
    }

    private fun uploadResume() {
        val userId = Variables.auth.currentUser?.uid ?: return

        // Fetch the current CV URL
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val existingCvUrl = document.getString("cv")

                if (cvUri != null) {
                    // If a CV exists, delete it first
                    if (!existingCvUrl.isNullOrEmpty()) {
                        Variables.deleteFile(requireContext(), existingCvUrl) { success ->
                            if (success) {
                                uploadNewCv(userId)
                            } else {
                                Variables.displayErrorMessage(
                                    "Failed to delete previous CV",
                                    requireContext()
                                )
                            }
                        }
                    } else {
                        uploadNewCv(userId)
                    }
                } else {
                    pickPdf() // Prompt user to select a CV if not already selected
                }
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage(
                    "Failed to fetch user CV details: ${e.message}",
                    requireContext()
                )
            }
    }

    private fun uploadNewCv(userId: String) {
        val fileName = "UsersCvs/${userId}_${UUID.randomUUID()}.pdf"
        val fileRef = Variables.storageRef.child(fileName)

        fileRef.putFile(cvUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    updateResumeInFirestore(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Variables.displayErrorMessage(
                    "Failed to upload CV: ${exception.message}",
                    requireContext()
                )
            }
    }

    private fun updateResumeInFirestore(newCvUrl: String) {
        val userId = Variables.auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("cv", newCvUrl)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Resume uploaded successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage(
                    "Failed to update resume: ${e.message}",
                    requireContext()
                )
            }
    }

    private fun downloadCv() {
        val userId = Variables.auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val cvUrl = document.getString("cv")
                if (!cvUrl.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))
                    startActivity(intent)
                } else {
                    Variables.displayErrorMessage("No CV found to download", requireContext())
                }
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage(
                    "Failed to fetch CV details: ${e.message}",
                    requireContext()
                )


            }

    }
    private fun fetchUserDetails() {
        val userId = Variables.auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("username")
                    val phoneNumber = document.getString("number")
                    val profileImageUrl = document.getString("profilePic")
                    val cvUrl = document.getString("cv")
                    val industry = document.getString("industry")
                    val role = document.getString("role")


                    binding.username.setText(username)
                    binding.number.setText(phoneNumber)
                    binding.actIndustry.setText(industry)
                    binding.actRole.setText(role)

                    // Set default image if profile image URL is null
                    if (profileImageUrl != "") {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .apply(RequestOptions.placeholderOf(R.drawable.cartoon_happy_eyes).error(R.drawable.cartoon_happy_eyes))
                            .into(binding.logo)
                    } else {
                        binding.logo.setImageResource(R.drawable.cartoon_happy_eyes) // Use your default placeholder image
                    }

                    // Set CV button state or provide a default state
                    if (cvUrl != "") {
                        // You might want to enable a "Download CV" button or show an indicator
                    }
                }
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage("Failed to fetch user details: ${e.message}", requireContext())
            }
    }

}
