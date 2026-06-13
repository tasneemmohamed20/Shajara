package com.example.moodlegovapp.data.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class CommentStripperInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val rawJson = response.body?.string() ?: ""

        // Regex to find and remove // inline comments
        // Note: This matches '//' up to the end of the line, ignoring URLs (http:// or https://)
        val cleanJson = rawJson.replace(Regex("(?<!https:|http:)//.*"), "")

        val newBody = cleanJson.toResponseBody(response.body?.contentType())
        return response.newBuilder().body(newBody).build()
    }
}