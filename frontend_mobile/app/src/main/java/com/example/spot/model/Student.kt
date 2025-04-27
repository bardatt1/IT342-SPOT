package com.example.spot.model

/**
 * Student data model matching the backend StudentDto
 */
data class Student(
    val id: Long,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val year: String,
    val program: String,
    val email: String,
    val studentPhysicalId: String,
    val googleLinked: Boolean
)
