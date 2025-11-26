package com.example.qrscannerapp.models

data class ScanHistory(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val type: String = "", // "URL", "TEXT", "EMAIL", "PHONE", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val displayText: String = "" // For showing in history list
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", 0L, "")
}

