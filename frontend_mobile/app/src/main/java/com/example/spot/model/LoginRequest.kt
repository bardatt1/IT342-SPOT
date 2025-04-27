package com.example.spot.model

/**
 * Login request model for authentication
 */
data class LoginRequest(
    val email: String,
    val password: String
)
