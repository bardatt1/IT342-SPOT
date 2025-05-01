package com.example.spot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Enrollment
import com.example.spot.model.Schedule
import com.example.spot.model.Section
import com.example.spot.repository.EnrollmentRepository
import com.example.spot.repository.ScheduleRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import com.example.spot.viewmodel.EnrollmentsState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * ViewModel for the Classes screen
 */
class ClassesViewModel : ViewModel() {
    private val enrollmentRepository = EnrollmentRepository()
    private val scheduleRepository = ScheduleRepository()
    private val TAG = "ClassesViewModel"

    // UI States
    private val _enrollmentsState = MutableStateFlow<EnrollmentsState>(Loading)
    val enrollmentsState: StateFlow<EnrollmentsState> = _enrollmentsState
    
    // Enrollment states for adding new classes
    private val _enrollState = MutableStateFlow<EnrollState>(EnrollState.Idle)
    val enrollState: StateFlow<EnrollState> = _enrollState
    
    // Input field state for enrollment key
    private val _enrollmentKey = MutableStateFlow("")
    val enrollmentKey: StateFlow<String> = _enrollmentKey

    init {
        loadEnrollments()
    }

    /**
     * Update enrollment key text field
     */
    fun updateEnrollmentKey(key: String) {
        _enrollmentKey.value = key
    }

    /**
     * Enroll student in a new class using the enrollment key
     */
    fun enrollInClass() {
        val key = _enrollmentKey.value.trim()
        
        if (key.isEmpty()) {
            _enrollState.value = EnrollState.Error("Please enter an enrollment key")
            return
        }
        
        _enrollState.value = EnrollState.Loading
        
        viewModelScope.launch {
            when (val result = enrollmentRepository.enrollStudent(key)) {
                is NetworkResult.Success -> {
                    _enrollState.value = EnrollState.Success(result.data)
                    _enrollmentKey.value = "" // Clear the input field
                    
                    // Refresh enrollments list after successful enrollment
                    loadEnrollments()
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to enroll student: ${result.message}")
                    _enrollState.value = EnrollState.Error(result.message)
                }
                else -> {
                    _enrollState.value = EnrollState.Error("Unknown error occurred")
                }
            }
        }
    }

    /**
     * Reset enrollment state to idle (used after showing success/error messages)
     */
    fun resetEnrollState() {
        _enrollState.value = EnrollState.Idle
    }

    /**
     * Load enrollments for the current logged-in student
     */
    fun loadEnrollments() {
        _enrollmentsState.value = Loading
        
        viewModelScope.launch {
            // Get user ID from TokenManager flow
            val userId = TokenManager.getUserId().firstOrNull() ?: 0L
            
            if (userId <= 0) {
                _enrollmentsState.value = Error("User not logged in")
                return@launch
            }
            
            Log.d(TAG, "Loading enrollments for student ID: $userId")
            
            when (val result = enrollmentRepository.getEnrollmentsByStudentId(userId)) {
                is NetworkResult.Success -> {
                    val enrollments = result.data
                    
                    if (enrollments.isEmpty()) {
                        _enrollmentsState.value = Empty
                    } else {
                        // Enhance each enrollment with schedule information
                        val enhancedEnrollments = enhanceEnrollmentsWithSchedules(enrollments)
                        _enrollmentsState.value = Success(enhancedEnrollments)
                    }
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to load enrollments: ${result.message}")
                    _enrollmentsState.value = Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _enrollmentsState.value = Loading
                }
            }
        }
    }
    
    /**
     * Enhance enrollment objects with schedule information
     */
    private suspend fun enhanceEnrollmentsWithSchedules(enrollments: List<Enrollment>): List<Enrollment> {
        val enhancedEnrollments = mutableListOf<Enrollment>()
        
        for (enrollment in enrollments) {
            try {
                val section = enrollment.section
                
                // Fetch schedules for this section
                val scheduleResult = scheduleRepository.getSchedulesBySectionId(section.id)
                
                if (scheduleResult is NetworkResult.Success && scheduleResult.data.isNotEmpty()) {
                    val schedules = scheduleResult.data
                    Log.d(TAG, "Found ${schedules.size} schedules for section ${section.id}")
                    
                    // For debugging
                    schedules.forEach { schedule ->
                        Log.d(TAG, "Schedule: day=${schedule.dayOfWeek}, time=${schedule.timeStart}-${schedule.timeEnd}, room=${schedule.room}")
                    }
                    
                    val formattedSchedule = formatSchedulesToString(schedules)
                    Log.d(TAG, "Formatted schedule: $formattedSchedule")
                    
                    // Create a brand new section instead of trying to copy and modify the existing one
                    val enhancedSection = Section(
                        id = section.id,
                        course = section.course,
                        teacher = section.teacher,
                        sectionName = section.sectionName,
                        enrollmentKey = section.enrollmentKey,
                        enrollmentOpen = section.enrollmentOpen,
                        enrollmentCount = section.enrollmentCount,
                        schedule = formattedSchedule
                    )
                    
                    // Create new enrollment with the enhanced section
                    val enhancedEnrollment = Enrollment(
                        id = enrollment.id,
                        section = enhancedSection,
                        student = enrollment.student,
                        enrolledAt = enrollment.enrolledAt
                    )
                    
                    enhancedEnrollments.add(enhancedEnrollment)
                } else {
                    if (scheduleResult is NetworkResult.Error) {
                        Log.e(TAG, "Error fetching schedules: ${scheduleResult.message}")
                    } else {
                        Log.d(TAG, "No schedules found for section ${section.id}")
                    }
                    enhancedEnrollments.add(enrollment)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing enrollment: ${e.message}", e)
                enhancedEnrollments.add(enrollment)
            }
        }
        
        return enhancedEnrollments
    }
    
    /**
     * Format a list of schedules into a readable string
     */
    private fun formatSchedulesToString(schedules: List<Schedule>): String {
        if (schedules.isEmpty()) return "No schedule available"
        
        return schedules.joinToString(", ") { schedule ->
            val day = when (schedule.dayOfWeek) {
                1 -> "Mon"
                2 -> "Tue"
                3 -> "Wed"
                4 -> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                7 -> "Sun"
                else -> "???"
            }
            
            val startTime = formatTime(schedule.timeStart)
            val endTime = formatTime(schedule.timeEnd)
            
            "$day $startTime-$endTime | ${schedule.room} (${schedule.scheduleType})"
        }
    }
    
    /**
     * Format time string from 24-hour to 12-hour format
     */
    private fun formatTime(timeString: String): String {
        // Handle empty or null string
        if (timeString.isNullOrBlank()) return ""
        
        try {
            // Parse time from the format received from backend (HH:mm:ss)
            val parts = timeString.split(":")
            if (parts.size < 2) return timeString
            
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            
            val amPm = if (hour < 12) "AM" else "PM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            
            return String.format("%d:%02d%s", hour12, minute, amPm)
        } catch (e: Exception) {
            // Return original if any parsing error occurs
            return timeString
        }
    }

    /**
     * Refresh enrollments (called after errors or user action)
     */
    fun refreshEnrollments() {
        loadEnrollments()
    }
}

/**
 * States for enrollment form UI
 */
sealed class EnrollState {
    object Idle : EnrollState()
    object Loading : EnrollState()
    data class Success(val enrollment: Enrollment) : EnrollState()
    data class Error(val message: String) : EnrollState()
}
