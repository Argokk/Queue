package com.example.queues.auth

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!this::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun saveToken(token: String) {
        if (this::prefs.isInitialized) {
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }
    }

    fun getToken(): String? {
        return if (this::prefs.isInitialized) {
            prefs.getString(KEY_TOKEN, null)
        } else {
            null
        }
    }

    fun clearToken() {
        if (this::prefs.isInitialized) {
            prefs.edit().remove(KEY_TOKEN).apply()
        }
    }
}
