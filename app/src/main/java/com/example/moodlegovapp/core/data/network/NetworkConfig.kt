package com.example.moodlegovapp.core.data.network

object NetworkConfig {

    // Real production base (replace when available)
    private const val REAL_BASE  = "https://moodle.gov.ae"

    // Mock server provided for development (Postman mock)
    private const val MOCK_BASE  = "https://3b61ee6a-f0f1-4a09-a023-5906659e57eb.mock.pstmn.io"

    // Toggle between mock and real — mirrors iOS useMock
    const val USE_MOCK           = true   // set false when real API is ready

    // When true and USE_MOCK == true → calls the remote Postman mock server (RealApiService)
    // When false and USE_MOCK == true → reads local JSON files (MockApiService)
    const val USE_REMOTE_MOCK    = false

    val BASE_URL: String get() = if (USE_MOCK) MOCK_BASE else REAL_BASE

    const val CONNECT_TIMEOUT    = 30L   // seconds
    const val READ_TIMEOUT       = 30L
    const val WRITE_TIMEOUT      = 30L
}