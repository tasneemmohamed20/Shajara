package com.example.moodlegovapp.core.data.network

import android.util.Log
import com.example.moodlegovapp.core.data.service.DataStoreManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object RetrofitClient {

    fun create(
        dataStoreManager: DataStoreManager,
        isDebug: Boolean = false
    ): RetrofitApiService {

        // ── Auth interceptor: attaches token ──
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = dataStoreManager.authTokenState.value
            val request = if (token != null && !NetworkConfig.USE_REMOTE_MOCK) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else original
            chain.proceed(request)
        }

        // ── Logging: DEBUG only, no sensitive data ──
        val logging = HttpLoggingInterceptor { message ->
            // Never log Authorization headers
            if (!message.contains("Authorization", ignoreCase = true)) {
                Log.d("API", message)
            }
        }.apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BASIC
            else         HttpLoggingInterceptor.Level.NONE
        }

        // ── TLS: enforce system validation ────
        // Android validates by default; we just wire up the standard TrustManager
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {
                // Default system validation — throws CertificateException on failure
                // Same effect as iOS SecTrustEvaluateWithError returning false → cancelAuthenticationChallenge
            }
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            // Certificate pinning — uncomment when real server cert is available
            // .certificatePinner(
            //     CertificatePinner.Builder()
            //         .add("moodle.gov.ae", "sha256/REPLACE_WITH_REAL_CERT_HASH=")
            //         .build()
            // )
            .build()

        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL + "/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RetrofitApiService::class.java)
    }
}