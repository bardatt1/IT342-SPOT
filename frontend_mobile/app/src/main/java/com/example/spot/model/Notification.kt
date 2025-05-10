package com.example.spot.model

import java.time.LocalDateTime

/**
 * Represents a notification or activity log in the system
 */
data class Notification(
    val id: Long,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEntityId: Long?,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
)

/**
 * Types of notifications that can be logged in the system
 */
enum class NotificationType {
    SEAT_PLAN,       // Related to seat selection or changes
    ATTENDANCE,      // Related to attendance logging
    ENROLLMENT,      // Related to course enrollments
    PROFILE_UPDATE,  // Related to profile edits
    COURSE,          // Related to course information
    SECTION,         // Related to section information
    SCHEDULE,        // Related to schedule changes
    SYSTEM           // System notifications
}
