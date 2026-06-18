package com.example.moodlegovapp.data.network

object NetworkConfig {

    // Real production base (replace when available)
    private const val REAL_BASE = "https://moodle-test.itcorner.qzz.io"

    // Postman mock server for development
    private const val MOCK_BASE = "https://3b61ee6a-f0f1-4a09-a023-5906659e57eb.mock.pstmn.io"

    // true  → network calls go to Postman mock server (development)
    // false → network calls go to the real Moodle API (production)
    const val USE_REMOTE_MOCK = false

    // When true, failed network calls fall back to local res/raw JSON via MockApiService
    const val ENABLE_LOCAL_FALLBACK = true

    val BASE_URL: String get() = if (USE_REMOTE_MOCK) MOCK_BASE else REAL_BASE

    const val CONNECT_TIMEOUT = 30L   // seconds
    const val READ_TIMEOUT    = 30L
    const val WRITE_TIMEOUT   = 30L
}
