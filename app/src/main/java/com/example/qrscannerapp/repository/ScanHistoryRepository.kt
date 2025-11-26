package com.example.qrscannerapp.repository

import android.util.Log
import com.example.qrscannerapp.models.ScanHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ScanHistoryRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "ScanHistoryRepository"
    }

    // Save scan to Firebase
    fun saveScan(content: String, type: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot save scan: User not authenticated")
            onComplete(false, "User not authenticated")
            return
        }

        Log.d(TAG, "Attempting to save scan for user: $userId, type: $type")

        val scanId = database.child("scan_history").child(userId).push().key
        if (scanId == null) {
            Log.e(TAG, "Failed to generate scan ID")
            onComplete(false, "Failed to generate scan ID")
            return
        }

        val displayText = if (content.length > 50) "${content.take(50)}..." else content

        val scan = ScanHistory(
            id = scanId,
            userId = userId,
            content = content,
            type = type,
            timestamp = System.currentTimeMillis(),
            displayText = displayText
        )

        Log.d(TAG, "Saving scan with ID: $scanId to path: scan_history/$userId/$scanId")

        database.child("scan_history")
            .child(userId)
            .child(scanId)
            .setValue(scan)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Successfully saved scan to Firebase: $scanId")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to save scan to Firebase", exception)
                onComplete(false, exception.message)
            }
    }

    // Get all scans for current user
    fun getUserScans(onResult: (List<ScanHistory>?, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot get scans: User not authenticated")
            onResult(null, "User not authenticated")
            return
        }

        Log.d(TAG, "Fetching scans for user: $userId from path: scan_history/$userId")

        database.child("scan_history")
            .child(userId)
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val scans = mutableListOf<ScanHistory>()
                    for (childSnapshot in snapshot.children) {
                        val scan = childSnapshot.getValue(ScanHistory::class.java)
                        scan?.let { scans.add(it) }
                    }
                    // Sort by timestamp descending (newest first)
                    scans.sortByDescending { it.timestamp }
                    Log.d(TAG, "✅ Successfully fetched ${scans.size} scans from Firebase")
                    onResult(scans, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Failed to fetch scans from Firebase: ${error.message}")
                    onResult(null, error.message)
                }
            })
    }

    // Delete a specific scan
    fun deleteScan(scanId: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot delete scan: User not authenticated")
            onComplete(false, "User not authenticated")
            return
        }

        Log.d(TAG, "Deleting scan: $scanId for user: $userId")

        database.child("scan_history")
            .child(userId)
            .child(scanId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Successfully deleted scan: $scanId")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to delete scan: $scanId", exception)
                onComplete(false, exception.message)
            }
    }

    // Delete all scans for current user
    fun deleteAllScans(onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot delete all scans: User not authenticated")
            onComplete(false, "User not authenticated")
            return
        }

        Log.d(TAG, "Deleting all scans for user: $userId")

        database.child("scan_history")
            .child(userId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Successfully deleted all scans")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to delete all scans", exception)
                onComplete(false, exception.message)
            }
    }
}

