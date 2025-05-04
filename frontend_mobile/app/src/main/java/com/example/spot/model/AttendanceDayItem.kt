package com.example.spot.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents a day item in the attendance calendar
 */
data class AttendanceDayItem(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val attendanceStatus: AttendanceStatus,
    val isSelected: Boolean = false,
    val detail: StudentAttendance.AttendanceDetail? = null
) {
    // Convenience properties to access date components
    val day: Int get() = date.dayOfMonth
    val month: Int get() = date.monthValue
    val year: Int get() = date.year
    val isToday: Boolean get() = date.equals(LocalDate.now())
    
    // For backward compatibility
    val isInCurrentMonth: Boolean get() = isCurrentMonth
    val status: AttendanceStatus get() = attendanceStatus
}

/**
 * Enum representing possible attendance statuses
 */
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    NO_CLASS,
    LATE,
    UPCOMING,
    NOT_RECORDED,
    NOT_APPLICABLE
}
