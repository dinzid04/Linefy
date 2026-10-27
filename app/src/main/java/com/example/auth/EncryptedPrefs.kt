package com.example.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Provides SharedPreferences for storing auth tokens and user preferences.
 */
object EncryptedPrefs {

    fun open(
        context: Context,
        fileName: String,
        legacyPlainName: String? = null
    ): SharedPreferences {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        if (legacyPlainName != null && legacyPlainName != fileName) {
            migrateFromLegacyIfAny(appCtx, legacyPlainName, prefs)
        }
        return prefs
    }

    private fun migrateFromLegacyIfAny(
        context: Context,
        legacyName: String,
        target: SharedPreferences
    ) {
        val legacy = context.getSharedPreferences(legacyName, Context.MODE_PRIVATE)
        val entries = legacy.all
        if (entries.isEmpty()) return
        val editor = target.edit()
        for ((key, value) in entries) {
            if (target.contains(key)) continue
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    (value as? Set<String>)?.let { editor.putStringSet(key, it) }
                }
            }
        }
        editor.apply()
        legacy.edit().clear().apply()
    }
}
