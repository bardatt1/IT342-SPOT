package com.example.spot.util

import android.util.Log
import com.example.spot.model.Section
import com.example.spot.model.SectionSchedule
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Utility class for handling time-related operations in the app
 */
object TimeUtil {
    private const val TAG = "TimeUtil"

    /**
     * Checks if the current time is within the schedule of a section
     * @param section The section to check schedule for
     * @return Boolean indicating if current time is within section's schedule
     */
    fun isWithinClassSchedule(section: Section): Boolean {
        val now = LocalDateTime.now()
        val currentDayOfWeek = now.dayOfWeek.value
        val currentTime = now.toLocalTime()
        
        Log.d(TAG, "Checking schedule for section ${section.id}")
        Log.d(TAG, "Current day: $currentDayOfWeek, Current time: $currentTime")
        
        // First, check if we have schedules from the API
        section.schedules?.forEach { schedule ->
            try {
                // Extract day value from schedule
                val scheduleDayValue = try {
                    val dayOfWeekField = schedule.javaClass.getDeclaredField("dayOfWeek")
                    dayOfWeekField.isAccessible = true
                    dayOfWeekField.getInt(schedule)
                } catch (e: Exception) {
                    // If we get here, it means the schedule doesn't have dayOfWeek field
                    getDayOfWeekValue(schedule.day)
                }
                
                // Extract time strings
                val startTimeStr = try {
                    val timeStartField = schedule.javaClass.getDeclaredField("timeStart")
                    timeStartField.isAccessible = true
                    timeStartField.get(schedule) as String
                } catch (e: Exception) {
                    schedule.startTime
                }
                
                val endTimeStr = try {
                    val timeEndField = schedule.javaClass.getDeclaredField("timeEnd")
                    timeEndField.isAccessible = true
                    timeEndField.get(schedule) as String
                } catch (e: Exception) {
                    schedule.endTime
                }
                
                val startTime = parseTimeString(startTimeStr)
                val endTime = parseTimeString(endTimeStr)
                
                Log.d(TAG, "Schedule: day=$scheduleDayValue, time=$startTimeStr-$endTimeStr")
                
                // Add a buffer of 10 minutes before class starts
                val bufferedStartTime = startTime.minusMinutes(10)
                
                if (currentDayOfWeek == scheduleDayValue && 
                    (currentTime.isAfter(bufferedStartTime) || currentTime.equals(bufferedStartTime)) &&
                    (currentTime.isBefore(endTime) || currentTime.equals(endTime))) {
                    Log.d(TAG, "Within schedule: day matches and time is within range")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking schedule: ${e.message}")
            }
        }
        
        // If no schedules found in the direct API data, or if the check failed,
        // try parsing the formatted display string
        if (section.schedule != null && section.schedule.isNotEmpty()) {
            Log.d(TAG, "Checking formatted schedule: ${section.schedule}")
            
            // First try to extract any schedule that mentions today's day of week
            val todayNames = getDayNames(currentDayOfWeek)
            val todaySchedules = section.schedule
                .split(",")
                .filter { scheduleItem -> 
                    todayNames.any { dayName -> 
                        scheduleItem.contains(dayName, ignoreCase = true) 
                    }
                }
            
            if (todaySchedules.isNotEmpty()) {
                Log.d(TAG, "Found schedules for today: $todaySchedules")
                
                for (scheduleItem in todaySchedules) {
                    // Extract time portion - look for times in formats like:
                    // "9:00PM-10:00PM" or "7:30AM-10:30AM"
                    val timePattern = Regex("""(\d+:\d+\s*[AP]M)\s*-\s*(\d+:\d+\s*[AP]M)""")
                    val timeMatch = timePattern.find(scheduleItem)
                    
                    if (timeMatch != null) {
                        val startTimeStr = timeMatch.groupValues[1]
                        val endTimeStr = timeMatch.groupValues[2]
                        
                        try {
                            Log.d(TAG, "Parsing time range: $startTimeStr to $endTimeStr")
                            
                            // Parse times using 12-hour format
                            val startTime = parseAmPmTime(startTimeStr)
                            val endTime = parseAmPmTime(endTimeStr)
                            
                            // Add buffer before class starts
                            val bufferedStartTime = startTime.minusMinutes(10)
                            
                            Log.d(TAG, "Parsed times - start: $startTime, end: $endTime, current: $currentTime")
                            
                            if ((currentTime.isAfter(bufferedStartTime) || currentTime.equals(bufferedStartTime)) &&
                                (currentTime.isBefore(endTime) || currentTime.equals(endTime))) {
                                Log.d(TAG, "Within schedule: time is within range")
                                return true
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing time range: ${e.message}")
                        }
                    }
                }
            }
        }
        
        Log.d(TAG, "Not within schedule")
        return false
    }
    
    /**
     * Parse a time string in AM/PM format (e.g. "7:30AM")
     */
    private fun parseAmPmTime(timeStr: String): LocalTime {
        val cleanTimeStr = timeStr.trim().uppercase(Locale.US)
        
        // Handle times like "7:30AM", "9:00 PM", etc.
        val pattern = Regex("""(\d+):(\d+)\s*([AP]M)""")
        val match = pattern.find(cleanTimeStr)
        
        if (match != null) {
            val (hourStr, minuteStr, amPm) = match.destructured
            var hour = hourStr.toInt()
            val minute = minuteStr.toInt()
            
            // Convert to 24-hour format
            if (amPm == "PM" && hour < 12) {
                hour += 12
            } else if (amPm == "AM" && hour == 12) {
                hour = 0
            }
            
            return LocalTime.of(hour, minute)
        }
        
        throw IllegalArgumentException("Unable to parse time: $timeStr")
    }
    
    /**
     * Get possible day name representations for a given day of week value
     */
    private fun getDayNames(dayOfWeek: Int): List<String> {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY.value -> listOf("Mon", "Monday", "M")
            DayOfWeek.TUESDAY.value -> listOf("Tue", "Tuesday", "T")
            DayOfWeek.WEDNESDAY.value -> listOf("Wed", "Wednesday", "W")
            DayOfWeek.THURSDAY.value -> listOf("Thu", "Thursday", "Th")
            DayOfWeek.FRIDAY.value -> listOf("Fri", "Friday", "F")
            DayOfWeek.SATURDAY.value -> listOf("Sat", "Saturday", "S")
            DayOfWeek.SUNDAY.value -> listOf("Sun", "Sunday", "Su")
            else -> emptyList()
        }
    }
    
    /**
     * Converts day of week string to numeric value (1-7, Monday=1, Sunday=7)
     */
    private fun getDayOfWeekValue(day: String): Int {
        val normalizedDay = day.trim().uppercase(Locale.US)
        return when {
            normalizedDay.contains("MON") -> DayOfWeek.MONDAY.value
            normalizedDay.contains("TUE") -> DayOfWeek.TUESDAY.value
            normalizedDay.contains("WED") -> DayOfWeek.WEDNESDAY.value
            normalizedDay.contains("THU") -> DayOfWeek.THURSDAY.value
            normalizedDay.contains("FRI") -> DayOfWeek.FRIDAY.value
            normalizedDay.contains("SAT") -> DayOfWeek.SATURDAY.value
            normalizedDay.contains("SUN") -> DayOfWeek.SUNDAY.value
            else -> -1
        }
    }
    
    /**
     * Parses a time string to LocalTime
     * Supports multiple formats: "HH:mm", "HH:mm:ss", "h:mm a", etc.
     */
    private fun parseTimeString(timeStr: String): LocalTime {
        val time = timeStr.trim()
        
        // Try parsing as 24-hour format first (HH:mm or HH:mm:ss)
        try {
            if (time.contains(":")) {
                val parts = time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt() 
                return LocalTime.of(hour, minute)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed to parse as 24-hour format: $time")
        }
        
        // Check if it's in AM/PM format
        if (time.contains("AM", ignoreCase = true) || time.contains("PM", ignoreCase = true)) {
            try {
                return parseAmPmTime(time)
            } catch (e: Exception) {
                Log.d(TAG, "Failed to parse as AM/PM format: $time - ${e.message}")
            }
        }
        
        // As a fallback, try parsing with DateTimeFormatter
        try {
            return LocalTime.parse(time)
        } catch (e: DateTimeParseException) {
            try {
                return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e2: Exception) {
                Log.e(TAG, "All parsing attempts failed for time: $time", e2)
                throw IllegalArgumentException("Unable to parse time: $time")
            }
        }
    }
}
