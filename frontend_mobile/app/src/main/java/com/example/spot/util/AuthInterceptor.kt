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
    private val TAG = "AuthInterceptor"
    
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
                Log.e(TAG, "Error getting token", e)
                null
            }
        }
        
        // If no token, proceed with original request but log this as a potential issue
        if (token.isNullOrBlank()) {
            Log.w(TAG, "No authentication token available for request to: ${originalRequest.url.encodedPath}")
            return chain.proceed(originalRequest)
        }
        
        // Log that we're adding a token (without showing the actual token)
        Log.d(TAG, "Adding authentication token to request: ${originalRequest.url.encodedPath}")
        
        // Add token to request
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        // Proceed with the authenticated request
        return chain.proceed(newRequest).also { response ->
            // Log authentication issues
            if (response.code == 401) {
                Log.e(TAG, "Authentication failed (401 Unauthorized) for: ${originalRequest.url.encodedPath}")
                
                // For debugging - get the current user ID
                runBlocking {
                    val userId = TokenManager.getUserId().first()
                    Log.d(TAG, "Current user ID from TokenManager: $userId")
                }
            }
        }
    }
}
