package com.example.spot.model

/**
 * Section data model matching the backend SectionDto
 */
data class Section(
    val id: Long,
    val course: Course,
    val teacher: Teacher?,
    val sectionName: String,
    val enrollmentKey: String?,
    val enrollmentOpen: Boolean,
    val enrollmentCount: Int,
    // Additional fields for UI
    val courseName: String = course.courseName,
    val sectionNumber: String = sectionName,
    val instructorName: String = teacher?.name ?: "Not Assigned",
    val schedule: String = course.schedule ?: "No Schedule"
)
