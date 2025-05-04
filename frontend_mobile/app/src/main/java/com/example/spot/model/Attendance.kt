package com.example.spot.model

/**
 * Attendance data model matching the backend AttendanceDto
 */
data class Attendance(
    val id: Long,
    val student: Student,
    val section: Section,
    val date: String, // YYYY-MM-DD format
    val startTime: String?, // Using String instead of LocalTime to match backend JSON format
    val endTime: String?    // Using String instead of LocalTime to match backend JSON format
)
