package com.example.qrscannerapp.models

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoBase64: String = "",  // Stores profile picture as Base64 string
    val createdAt: Long = System.currentTimeMillis(),
    val totalScans: Int = 0
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", 0L, 0)
}

