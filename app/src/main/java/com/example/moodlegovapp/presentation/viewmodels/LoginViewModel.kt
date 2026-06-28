package com.example.moodlegovapp.presentation.viewmodels

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.session.AppSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val session: AppSession,
    private val dataStoreManager: com.example.moodlegovapp.data.service.DataStoreManager
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // Step 2 — Credentials state
    // ─────────────────────────────────────────────────────────────

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true

            try {
                val result = session.login(
                    username = _username.value,
                    password = _password.value
                )

                when (result) {
                    is AppResult.Success -> {
                        // session already handled token + profile

                    }

                    is AppResult.Failure -> {
                        _errorMessage.value = result.error.errorDescription
                    }

                    is AppResult.Loading -> {
                        // optional
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Step 1 — Language selection state & helpers
    // ─────────────────────────────────────────────────────────────

    /** Exposed selected language ("English" / "Arabic"), initialised from the live locale. */
    private val _selectedLanguage = MutableStateFlow(tagToLanguage(currentLanguageTag()))
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    /**
     * Non-null while the restart-confirmation dialog is open.
     * Holds the language the user tapped but hasn't confirmed yet.
     */
    private val _pendingLanguage = MutableStateFlow<String?>(null)
    val pendingLanguage: StateFlow<String?> = _pendingLanguage

    /** Called when the user taps a language option on the toggle. */
    fun onLanguageTapped(language: String) {
        if (language != _selectedLanguage.value) {
            _pendingLanguage.value = language
        }
    }

    /** Called when the user dismisses the dialog (Cancel or outside tap). */
    fun onLanguageDialogDismiss() {
        _pendingLanguage.value = null
    }

    /**
     * Called when the user confirms the language change (OK button).
     * Applies the locale via [AppCompatDelegate] and recreates the activity.
     */
    fun applyLanguage(activity: Activity) {
        val pending = _pendingLanguage.value ?: return
        _pendingLanguage.value = null
        val tag = languageToTag(pending)
        
        viewModelScope.launch {
            dataStoreManager.save(com.example.moodlegovapp.data.service.DataStoreManager.KEY_LANGUAGE, tag)
        }

        if (tag == currentLanguageTag()) {
            _selectedLanguage.value = pending
            return
        }
        _selectedLanguage.value = pending
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        activity.recreate()
    }

    /** Keeps toggle state aligned with the locale applied by the system / AppCompat. */
    fun syncSelectedLanguageFromSystem() {
        _selectedLanguage.value = tagToLanguage(currentLanguageTag())
    }

    // ─────────────────────────────────────────────────────────────
    // Private helpers (language ↔ BCP-47 tag conversion)
    // ─────────────────────────────────────────────────────────────

    companion object {
        const val LANGUAGE_ENGLISH = "English"
        const val LANGUAGE_ARABIC = "Arabic"

        /** Returns the BCP-47 tag for the language currently applied via AppCompatDelegate. */
        fun currentLanguageTag(): String {
            val locales = AppCompatDelegate.getApplicationLocales()
            return if (locales.isEmpty) "en" else locales[0]?.language ?: "en"
        }

        /** Maps our internal key ([LANGUAGE_ENGLISH] / [LANGUAGE_ARABIC]) to a BCP-47 tag. */
        fun languageToTag(lang: String): String =
            if (lang == LANGUAGE_ARABIC) "ar" else "en"

        /** Maps a BCP-47 tag back to our internal key. */
        fun tagToLanguage(tag: String): String =
            if (tag == "ar") LANGUAGE_ARABIC else LANGUAGE_ENGLISH
    }
}
