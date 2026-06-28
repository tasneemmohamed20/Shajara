package com.example.moodlegovapp.data.network

object NetworkConfig {

    // Real production base
    private const val REAL_BASE = "https://psa.intellectilearn.net"

    // Moodle web service token used for wstoken-based REST calls and for
    // appending ?token= to pluginfile.php / webview URLs.
    const val WS_TOKEN = "05be238ddaec1f05a511f6290a290106"

    // Always use the real Moodle API. No Postman mock server and no local mock fallback.
    const val USE_REMOTE_MOCK = false
    const val ENABLE_LOCAL_FALLBACK = false

    val BASE_URL: String get() = REAL_BASE

    const val CONNECT_TIMEOUT = 30L   // seconds
    const val READ_TIMEOUT    = 30L
    const val WRITE_TIMEOUT   = 30L
}
