package com.example.spot.model

/**
 * Login request model for authentication
 * Note: We're using the 'email' field to send student_physical_id for backward compatibility with backend
 */
data class LoginRequest(
    val email: String,  // This field will contain student_physical_id value
    val password: String
)
