package com.example.moodlegovapp.core.data.service

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(
        context,
        MasterKey.DEFAULT_MASTER_KEY_ALIAS
    ).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "gov_moodle_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_TOKEN          = "auth_token"
        private const val KEY_PRIVATE_TOKEN  = "private_token"
        private const val KEY_USER_ID        = "user_id"

        @Volatile private var instance: SecureStorage? = null

        fun getInstance(context: Context): SecureStorage =
            instance ?: synchronized(this) {
                instance ?: SecureStorage(context.applicationContext).also { instance = it }
            }
    }

    // ── TOKEN ─────────────────────────────────
    // mirrors iOS: saveToken(_ token: String)
    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    // mirrors iOS: getToken() -> String?
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    // mirrors iOS: deleteToken()
    fun deleteToken() {
        prefs.edit { remove(KEY_TOKEN) }
    }

    fun savePrivateToken(token: String) {
        prefs.edit { putString(KEY_PRIVATE_TOKEN, token) }
    }

    fun getPrivateToken(): String? = prefs.getString(KEY_PRIVATE_TOKEN, null)

    // ── USER ID ───────────────────────────────
    // mirrors iOS: saveUserId(_ userId: Int)
    fun saveUserId(userId: Int) {
        prefs.edit { putInt(KEY_USER_ID, userId) }
    }

    // mirrors iOS: getUserId() -> Int?
    fun getUserId(): Int? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id == -1) null else id
    }

    // ── CLEAR ALL ─────────────────────────────
    // mirrors iOS: clearAll()
    fun clearAll() {
        prefs.edit { clear() }
    }

    // mirrors iOS: var isLoggedIn: Bool
    val isLoggedIn: Boolean get() = getToken() != null
}