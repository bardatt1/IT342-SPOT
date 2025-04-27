package com.example.spot.util

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds the authorization JWT token to requests
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding token for login and other non-authenticated endpoints
        if (originalRequest.url.encodedPath.contains("/api/auth/login") ||
            originalRequest.url.encodedPath.contains("/api/auth/check-email")) {
            return chain.proceed(originalRequest)
        }
        
        // Get token from TokenManager - must be done in a blocking way for OkHttp
        val token = runBlocking {
            try {
                TokenManager.getToken().first()
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "Error getting token", e)
                null
            }
        }
        
        // If no token, proceed with original request
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }
        
        // Add token to request
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
}
