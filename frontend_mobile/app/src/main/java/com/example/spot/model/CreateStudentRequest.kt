package com.example.spot.model

/**
 * Request model for student registration that matches the backend API requirements
 * Based on the CreateStudentRequest schema in the API documentation
 */
data class CreateStudentRequest(
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val year: String,
    val program: String,
    val email: String,
    val studentPhysicalId: String,
    val password: String
)
