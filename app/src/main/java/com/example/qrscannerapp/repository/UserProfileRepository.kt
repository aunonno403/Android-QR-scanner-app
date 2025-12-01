package com.example.qrscannerapp.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.qrscannerapp.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class UserProfileRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    companion object {
        private const val TAG = "UserProfileRepository"
    }

    // Get user profile from Firebase
    fun getUserProfile(onResult: (UserProfile?, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot get profile: User not authenticated")
            onResult(null, "User not authenticated")
            return
        }

        Log.d(TAG, "Fetching profile for user: $userId")

        database.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profile = snapshot.getValue(UserProfile::class.java)
                    if (profile != null) {
                        Log.d(TAG, "✅ Profile loaded successfully for user: $userId")
                    } else {
                        Log.d(TAG, "⚠️ No profile found for user: $userId")
                    }
                    onResult(profile, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Failed to fetch profile: ${error.message}")
                    onResult(null, error.message)
                }
            })
    }

    // Create or update user profile
    fun updateProfile(profile: UserProfile, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot update profile: User not authenticated")
            onComplete(false, "User not authenticated")
            return
        }

        Log.d(TAG, "Updating profile for user: $userId")

        database.child("users").child(userId)
            .setValue(profile)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Profile updated successfully for user: $userId")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to update profile", exception)
                onComplete(false, exception.message)
            }
    }

    // Convert Bitmap to Base64 string with compression
    fun convertImageToBase64(bitmap: Bitmap, maxSizeKB: Int = 200): String {
        Log.d(TAG, "Converting bitmap to Base64 (target size: $maxSizeKB KB)")

        val outputStream = ByteArrayOutputStream()
        var quality = 100
        var base64String: String

        do {
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
            val sizeKB = base64String.length / 1024

            Log.d(TAG, "Compression quality: $quality%, size: $sizeKB KB")

            if (sizeKB <= maxSizeKB) break
            quality -= 10
        } while (quality > 10)

        val finalSizeKB = base64String.length / 1024
        Log.d(TAG, "✅ Base64 conversion complete. Final size: $finalSizeKB KB")

        return base64String
    }

    // Convert Base64 string back to Bitmap
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            if (base64String.isEmpty()) {
                Log.w(TAG, "Empty Base64 string provided")
                return null
            }

            Log.d(TAG, "Decoding Base64 to bitmap (size: ${base64String.length / 1024} KB)")
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                Log.d(TAG, "✅ Successfully decoded bitmap: ${bitmap.width}x${bitmap.height}")
            } else {
                Log.e(TAG, "❌ Failed to decode bitmap from Base64")
            }

            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception decoding Base64: ${e.message}", e)
            null
        }
    }

    // Increment total scans count
    fun incrementScanCount(onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "User not authenticated")
            return
        }

        val userRef = database.child("users").child(userId).child("totalScans")

        userRef.get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            userRef.setValue(currentCount + 1)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Scan count incremented to ${currentCount + 1}")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "❌ Failed to increment scan count", exception)
                    onComplete(false, exception.message)
                }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "❌ Failed to read scan count", exception)
            onComplete(false, exception.message)
        }
    }
}

