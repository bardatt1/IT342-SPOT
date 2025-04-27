package com.example.spot.model

/**
 * Course data model matching the backend CourseDto
 */
data class Course(
    val id: Long,
    val courseName: String,
    val courseDescription: String,
    val courseCode: String,
    val sectionCount: Int,
    val schedule: String? = null
)
