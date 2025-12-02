package com.example.qrscannerapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.qrscannerapp.models.UserProfile
import com.example.qrscannerapp.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var displayNameEdit: EditText
    private lateinit var emailText: TextView
    private lateinit var changePhotoText: TextView
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var repository: UserProfileRepository

    private var selectedBitmap: Bitmap? = null
    private var currentProfile: UserProfile? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    profileImageView.setImageBitmap(selectedBitmap)
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        repository = UserProfileRepository()

        profileImageView = findViewById(R.id.profileImageView)
        displayNameEdit = findViewById(R.id.displayNameEdit)
        emailText = findViewById(R.id.emailText)
        changePhotoText = findViewById(R.id.changePhotoText)
        saveButton = findViewById(R.id.saveButton)
        logoutButton = findViewById(R.id.logoutButton)
        progressBar = findViewById(R.id.progressBar)

        profileImageView.setOnClickListener {
            openImagePicker()
        }

        changePhotoText.setOnClickListener {
            openImagePicker()
        }

        saveButton.setOnClickListener {
            saveProfile()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        loadProfile()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePicker.launch(intent)
    }

    private fun loadProfile() {
        showLoading(true)

        repository.getUserProfile { profile, error ->
            showLoading(false)

            if (error != null) {
                Toast.makeText(this, "Error loading profile: $error", Toast.LENGTH_SHORT).show()
                // Set defaults for new users
                setDefaultProfile()
                return@getUserProfile
            }

            if (profile != null) {
                currentProfile = profile
                displayProfile(profile)
            } else {
                // New user - create default profile
                setDefaultProfile()
            }
        }
    }

    private fun setDefaultProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        emailText.text = user?.email ?: "No email"
        displayNameEdit.setText(user?.displayName ?: "")
    }

    private fun displayProfile(profile: UserProfile) {
        displayNameEdit.setText(profile.displayName)
        emailText.text = profile.email

        // Load Base64 image if exists
        if (profile.photoBase64.isNotEmpty()) {
            val bitmap = repository.base64ToBitmap(profile.photoBase64)
            if (bitmap != null) {
                profileImageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "Failed to load profile picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val displayName = displayNameEdit.text.toString().trim()

        if (displayName.isEmpty()) {
            displayNameEdit.error = "Display name cannot be empty"
            displayNameEdit.requestFocus()
            return
        }

        showLoading(true)
        saveButton.isEnabled = false

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: run {
            showLoading(false)
            saveButton.isEnabled = true
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert bitmap to Base64 if image was selected
        var photoBase64 = currentProfile?.photoBase64 ?: ""

        selectedBitmap?.let { bitmap ->
            Toast.makeText(this, "Compressing image...", Toast.LENGTH_SHORT).show()
            photoBase64 = repository.convertImageToBase64(bitmap)
        }

        val profile = UserProfile(
            uid = userId,
            displayName = displayName,
            email = user.email ?: "",
            photoBase64 = photoBase64,
            createdAt = currentProfile?.createdAt ?: System.currentTimeMillis(),
            totalScans = currentProfile?.totalScans ?: 0
        )

        repository.updateProfile(profile) { success, error ->
            showLoading(false)
            saveButton.isEnabled = true

            if (success) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update: ${error ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        saveButton.isEnabled = !loading
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Reset app mode to require mode selection on next launch
        AppMode.isOffline = false
        val prefs = getSharedPreferences("app_mode_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to MainActivity and clear back stack
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

