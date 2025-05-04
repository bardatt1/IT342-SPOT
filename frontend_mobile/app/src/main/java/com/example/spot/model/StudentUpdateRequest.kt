package com.example.spot.model

/**
 * Request model for updating student information
 */
data class StudentUpdateRequest(
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val year: String? = null,
    val program: String? = null,
    val email: String? = null,
    val studentPhysicalId: String? = null,
    val password: String? = null,
    val currentPassword: String? = null
)
