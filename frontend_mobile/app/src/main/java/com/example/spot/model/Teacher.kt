package com.example.spot.model

/**
 * Teacher data model matching the backend TeacherDto
 */
data class Teacher(
    val id: Long,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val email: String,
    val teacherPhysicalId: String,
    val googleLinked: Boolean,
    val assignedSectionIds: List<Long>
) {
    // Computed property for full name
    val name: String
        get() = if (middleName.isNullOrEmpty()) {
            "$firstName $lastName"
        } else {
            "$firstName $middleName $lastName"
        }
}
