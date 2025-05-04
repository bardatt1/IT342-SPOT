package com.example.spot.repository

import android.util.Log
import com.example.spot.model.Schedule
import com.example.spot.model.Section
import com.example.spot.model.SectionSchedule
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling section related API calls
 */
class SectionRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get sections by course ID
     */
    suspend fun getSectionsByCourseId(courseId: Long): NetworkResult<List<Section>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSectionsByCourseId(courseId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get sections error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get section by ID
     */
    suspend fun getSectionById(id: Long): NetworkResult<Section> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSectionById(id)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    // Fetch schedules and populate the section object
                    val section = response.data
                    val schedulesResult = getSchedulesBySectionId(section.id)
                    
                    if (schedulesResult is NetworkResult.Success) {
                        try {
                            val formattedSchedules = convertSchedulesToSectionSchedules(schedulesResult.data)
                            // Create a new section object with schedules - safely
                            // Instead of using copy() which can cause issues with derived properties,
                            // we'll create a new Section object directly and preserve all original properties
                            val updatedSection = Section(
                                id = section.id,
                                course = section.course,
                                teacher = section.teacher,
                                sectionName = section.sectionName,
                                enrollmentKey = section.enrollmentKey,
                                enrollmentOpen = section.enrollmentOpen,
                                enrollmentCount = section.enrollmentCount,
                                schedule = section.schedule,
                                schedules = formattedSchedules
                            )
                            return@withContext NetworkResult.Success(updatedSection)
                        } catch (e: Exception) {
                            Log.e("SectionRepository", "Error creating updated section", e)
                            // If there's an error updating with schedules, return the original section
                            return@withContext NetworkResult.Success(section)
                        }
                    }
                    
                    NetworkResult.Success(section)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get section by ID error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get section by enrollment key
     */
    suspend fun getSectionByEnrollmentKey(enrollmentKey: String): NetworkResult<Section> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSectionByEnrollmentKey(enrollmentKey)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    // Fetch schedules and populate the section object
                    val section = response.data
                    val schedulesResult = getSchedulesBySectionId(section.id)
                    
                    if (schedulesResult is NetworkResult.Success) {
                        try {
                            val formattedSchedules = convertSchedulesToSectionSchedules(schedulesResult.data)
                            // Create a new section object with schedules - safely
                            // Instead of using copy() which can cause issues with derived properties,
                            // we'll create a new Section object directly and preserve all original properties
                            val updatedSection = Section(
                                id = section.id,
                                course = section.course,
                                teacher = section.teacher,
                                sectionName = section.sectionName,
                                enrollmentKey = section.enrollmentKey,
                                enrollmentOpen = section.enrollmentOpen,
                                enrollmentCount = section.enrollmentCount,
                                schedule = section.schedule,
                                schedules = formattedSchedules
                            )
                            return@withContext NetworkResult.Success(updatedSection)
                        } catch (e: Exception) {
                            Log.e("SectionRepository", "Error creating updated section", e)
                            // If there's an error updating with schedules, return the original section
                            return@withContext NetworkResult.Success(section)
                        }
                    }
                    
                    NetworkResult.Success(section)
                } else {
                    NetworkResult.Error(response.message ?: "Error fetching section")
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get section by enrollment key error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get schedules for a specific section
     */
    private suspend fun getSchedulesBySectionId(sectionId: Long): NetworkResult<List<Schedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSchedulesBySectionId(sectionId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    Log.d("SectionRepository", "Fetched ${response.data.size} schedules for section $sectionId")
                    NetworkResult.Success(response.data)
                } else {
                    Log.d("SectionRepository", "Failed to fetch schedules: ${response.message ?: "Unknown error"}")
                    NetworkResult.Error(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get schedules error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Convert backend Schedule objects to SectionSchedule objects for UI display
     */
    private fun convertSchedulesToSectionSchedules(schedules: List<Schedule>): List<SectionSchedule> {
        return schedules.map { schedule ->
            SectionSchedule(
                day = convertDayOfWeekToString(schedule.dayOfWeek),
                startTime = formatTime(schedule.timeStart),
                endTime = formatTime(schedule.timeEnd)
            )
        }
    }
    
    /**
     * Convert numeric day of week to string representation
     */
    private fun convertDayOfWeekToString(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }
    
    /**
     * Format time string for display
     */
    private fun formatTime(time: String): String {
        // If time is already in a good format, return it directly
        if (time.contains(":") && (time.contains("AM") || time.contains("PM"))) {
            return time
        }
        
        // Simple formatting for HH:mm format to add AM/PM
        try {
            val parts = time.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                
                val amPm = if (hour >= 12) "PM" else "AM"
                val hour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                
                return String.format("%d:%02d %s", hour12, minute, amPm)
            }
        } catch (e: Exception) {
            Log.e("SectionRepository", "Error formatting time", e)
        }
        
        // Return original if parsing fails
        return time
    }
}
