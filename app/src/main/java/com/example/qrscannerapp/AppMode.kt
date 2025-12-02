package com.example.qrscannerapp

object AppMode {
    @Volatile
    var isOffline: Boolean = false

    private const val PREFS_NAME = "app_mode_prefs"
    private const val KEY_OFFLINE = "key_offline_mode"

    fun load(context: android.content.Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        isOffline = prefs.getBoolean(KEY_OFFLINE, false)
    }

    fun save(context: android.content.Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OFFLINE, isOffline).apply()
    }
}
