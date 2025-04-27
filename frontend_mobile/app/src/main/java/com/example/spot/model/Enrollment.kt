package com.example.spot.model

import java.time.LocalDateTime

/**
 * Enrollment data model matching the backend EnrollmentDto
 */
data class Enrollment(
    val id: Long,
    val section: Section,
    val student: Student,
    val enrolledAt: String // ISO-8601 date-time format
)
