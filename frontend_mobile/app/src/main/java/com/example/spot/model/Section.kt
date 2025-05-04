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
    val schedule: String? = null,
    val schedules: List<SectionSchedule>? = null
) {
    // Computed property to ensure schedule information is always available
    fun getScheduleDisplay(): String {
        // If schedules list is available and not empty, format it
        if (schedules?.isNotEmpty() == true) {
            return schedules.joinToString(separator = "\n") { schedule ->
                val startTime12Hour = convertTo12HourFormat(schedule.startTime)
                val endTime12Hour = convertTo12HourFormat(schedule.endTime)
                "${schedule.day}, $startTime12Hour - $endTime12Hour"
            }
        }
        
        // If legacy schedule string is available, use it
        if (schedule != null && schedule.isNotEmpty()) {
            return schedule
        }
        
        // If no schedule information is available
        return "No schedule information available"
    }
    
    // Convert 24-hour time format to 12-hour format with AM/PM
    private fun convertTo12HourFormat(time24Hour: String): String {
        try {
            // Handle possible formats like "HH:mm" or "HH:mm:ss"
            val parts = time24Hour.split(":")
            if (parts.isEmpty()) return time24Hour
            
            val hour = parts[0].toIntOrNull() ?: return time24Hour
            val minute = if (parts.size > 1) parts[1] else "00"
            
            val amPm = if (hour >= 12) "PM" else "AM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            
            return String.format("%d:%s %s", hour12, minute, amPm)
        } catch (e: Exception) {
            // In case of any parsing error, return the original format
            return time24Hour
        }
    }
    
    // Check if this section has real schedule data
    fun hasRealScheduleData(): Boolean {
        return schedules?.isNotEmpty() == true || (schedule != null && schedule.isNotEmpty())
    }
}

/**
 * SectionSchedule data model for displaying detailed schedule information
 */
data class SectionSchedule(
    val day: String,
    val startTime: String,
    val endTime: String
)
