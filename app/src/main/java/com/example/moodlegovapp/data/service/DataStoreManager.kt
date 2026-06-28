package com.example.moodlegovapp.data.service

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gov_moodle_secure_prefs"
)

class DataStoreManager private constructor(
    context: Context, @PublishedApi internal val dataStore: DataStore<Preferences>
) {

    private val aead: Aead
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        AeadConfig.register()
        val keysetHandle =
            AndroidKeysetManager.Builder().withSharedPref(context, "tink_keyset", "tink_prefs")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://gov_moodle_master_key").build().keysetHandle

        aead = keysetHandle.getPrimitive(Aead::class.java)
    }

    companion object {
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_PRIVATE_TOKEN = stringPreferencesKey("private_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")

        val KEY_OFFLINE_MODE_ENABLED = stringPreferencesKey("offline_mode_enabled")
        val KEY_SYNC_WIFI_ONLY = stringPreferencesKey("sync_wifi_only")
        val KEY_DISABLED_COURSE_DOWNLOADS = stringPreferencesKey("disabled_course_downloads")
        val KEY_LAST_SYNC_AT = stringPreferencesKey("last_sync_at")

        @Volatile
        private var instance: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager = instance ?: synchronized(this) {
            instance ?: DataStoreManager(
                context.applicationContext, context.applicationContext.dataStore
            ).also {
                instance = it
            }
        }
    }

    private fun encrypt(value: String): String {
        val encrypted = aead.encrypt(value.toByteArray(), null)
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(encryptedValue: String?): String? {
        if (encryptedValue == null) return null
        return try {
            val decoded = Base64.decode(encryptedValue, Base64.DEFAULT)
            String(aead.decrypt(decoded, null))
        } catch (e: Exception) {
            Log.e("DataStoreManager", "Failed to decrypt value", e)
            null
        }
    }

    val languageState: StateFlow<String?> =
        dataStore.data.map { prefs -> decrypt(prefs[KEY_LANGUAGE]) }.stateIn(
                scope = applicationScope, started = SharingStarted.Eagerly, initialValue = null
            )

    val authTokenState: StateFlow<String?> =
        dataStore.data.map { prefs -> decrypt(prefs[KEY_TOKEN]) }.stateIn(
                scope = applicationScope, started = SharingStarted.Eagerly, initialValue = null
            )

    val userIdState: StateFlow<String?> =
        dataStore.data.map { prefs -> decrypt(prefs[KEY_USER_ID]) }.stateIn(
                scope = applicationScope, started = SharingStarted.Eagerly, initialValue = null
            )

    val offlineModeEnabledState: StateFlow<Boolean> = dataStore.data.map { prefs ->
            decrypt(prefs[KEY_OFFLINE_MODE_ENABLED])?.toBooleanStrictOrNull() ?: true
        }.stateIn(
            scope = applicationScope, started = SharingStarted.Eagerly, initialValue = true
        )

    val syncWifiOnlyState: StateFlow<Boolean> = dataStore.data.map { prefs ->
            decrypt(prefs[KEY_SYNC_WIFI_ONLY])?.toBooleanStrictOrNull() ?: false
        }.stateIn(
            scope = applicationScope, started = SharingStarted.Eagerly, initialValue = false
        )

    val lastSyncAtState: StateFlow<Long?> = dataStore.data.map { prefs ->
            decrypt(prefs[KEY_LAST_SYNC_AT])?.toLongOrNull()
        }.stateIn(
            scope = applicationScope, started = SharingStarted.Eagerly, initialValue = null
        )

    suspend fun isCourseDownloadDisabled(courseId: Int): Boolean {
        val csv = get<String>(KEY_DISABLED_COURSE_DOWNLOADS) ?: return false
        return csv.split(",").mapNotNull { it.toIntOrNull() }.contains(courseId)
    }

    suspend fun markSyncedNow() {
        save(KEY_LAST_SYNC_AT, System.currentTimeMillis())
    }

    suspend fun <T> save(key: Preferences.Key<String>, value: T) {
        val encryptedValue = encrypt(value.toString())
        dataStore.edit { prefs ->
            prefs[key] = encryptedValue
        }
    }

    suspend inline fun <reified T> get(key: Preferences.Key<String>): T? {
        val encryptedValue = dataStore.data.firstOrNull()?.get(key)
        val decryptedString = decrypt(encryptedValue) ?: return null

        return when (T::class) {
            String::class -> decryptedString as T
            Int::class -> decryptedString.toIntOrNull() as? T
            Boolean::class -> decryptedString.toBooleanStrictOrNull() as? T
            Float::class -> decryptedString.toFloatOrNull() as? T
            Long::class -> decryptedString.toLongOrNull() as? T
            else -> throw IllegalArgumentException("Type ${T::class.simpleName} is not supported")
        }
    }

    suspend fun delete(key: Preferences.Key<String>) {
        dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}