package com.example.spot.model

/**
 * Generic API response wrapper used by the backend
 */
data class ApiResponse<T>(
    val result: String,
    val message: String,
    val data: T?
)
