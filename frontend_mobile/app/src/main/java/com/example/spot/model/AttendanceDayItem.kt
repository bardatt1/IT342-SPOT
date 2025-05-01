package com.example.spot.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents a day item in the attendance calendar
 */
data class AttendanceDayItem(
    val date: LocalDate,
    val status: AttendanceStatus,
    val startTime: LocalTime? = null,
    val scheduleStartTime: LocalTime? = null
)

/**
 * Enum representing possible attendance statuses
 */
enum class AttendanceStatus {
    PRESENT,
    LATE,
    ABSENT,
    NO_CLASS
}
