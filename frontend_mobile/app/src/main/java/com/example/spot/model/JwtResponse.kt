package com.example.spot.model

/**
 * JWT response model matching the backend JwtResponse
 */
data class JwtResponse(
    val accessToken: String,
    val tokenType: String,
    val userType: String,
    val id: Long,
    val email: String,
    val name: String,
    val googleLinked: Boolean,
    val googleId: String? = null
)
