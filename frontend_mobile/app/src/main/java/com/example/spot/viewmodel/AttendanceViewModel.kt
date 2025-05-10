package com.example.spot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.StudentAttendance
import com.example.spot.repository.AttendanceRepository
import com.example.spot.repository.SectionRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import com.example.spot.util.NotificationLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for attendance operations following MVI pattern
 */
class AttendanceViewModel : ViewModel() {
    private val TAG = "AttendanceViewModel"
    private val attendanceRepository = AttendanceRepository()
    private val sectionRepository = SectionRepository()
    
    // State for student attendance check-in
    private val _attendanceState = MutableStateFlow<AttendanceState>(AttendanceState.Idle)
    val attendanceState: StateFlow<AttendanceState> = _attendanceState
    
    // State for section attendance history
    private val _sectionAttendanceState = MutableStateFlow<SectionAttendanceState>(SectionAttendanceState.Idle)
    val sectionAttendanceState: StateFlow<SectionAttendanceState> = _sectionAttendanceState
    
    /**
     * Log attendance for the current student in a section
     */
    fun logAttendance(sectionId: Long) {
        _attendanceState.value = AttendanceState.Loading
        
        viewModelScope.launch {
            try {
                val userIdFlow = TokenManager.getUserId()
                val userId = userIdFlow.first()
                
                if (userId == null) {
                    _attendanceState.value = AttendanceState.Error("User not logged in. Please log in.")
                    return@launch
                }
                
                when (val result = attendanceRepository.logAttendance(sectionId)) {
                    is NetworkResult.Success -> {
                        _attendanceState.value = AttendanceState.Success(result.data)
                        
                        // Get section details for better notification context
                        val sectionResult = sectionRepository.getSectionById(sectionId)
                        val sectionName = if (sectionResult is NetworkResult.Success) {
                            sectionResult.data.sectionName ?: sectionResult.data.course.courseName
                        } else {
                            "Section #$sectionId"
                        }
                        
                        // Format current date for the notification
                        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        
                        // Log the attendance activity
                        NotificationLogger.logAttendanceRecorded(
                            sectionName = sectionName,
                            date = currentDate,
                            sectionId = sectionId
                        )
                    }
                    is NetworkResult.Error -> {
                        _attendanceState.value = AttendanceState.Error(result.message)
                    }
                    else -> {
                        _attendanceState.value = AttendanceState.Error("Unknown error occurred")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging attendance", e)
                _attendanceState.value = AttendanceState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Load attendance records for the current student
     */
    fun loadStudentAttendanceRecords() {
        viewModelScope.launch {
            try {
                val userIdFlow = TokenManager.getUserId()
                val userId = userIdFlow.first()
                
                if (userId == null) {
                    // Handle user not logged in
                    return@launch
                }
                
                // This would be implemented to fetch attendance records
                // attendanceRepository.getAttendanceRecordsByStudentId(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading attendance records", e)
                // Handle error
            }
        }
    }
    
    /**
     * Load attendance statistics for a student in a section
     */
    fun loadSectionAttendanceHistory(sectionId: Long) {
        _sectionAttendanceState.value = SectionAttendanceState.Loading
        
        viewModelScope.launch {
            try {
                when (val result = attendanceRepository.getStudentAttendanceStats(sectionId)) {
                    is NetworkResult.Success<StudentAttendance> -> {
                        // Use real attendance data from API
                        val studentAttendance = result.data
                        Log.d(TAG, "Received attendance data for section $sectionId: ${studentAttendance.attendanceByDate.size} dates")
                        
                        // Process the data to include time information if available
                        val processedData = processAttendanceData(studentAttendance)
                        _sectionAttendanceState.value = SectionAttendanceState.Success(processedData)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error loading attendance: ${result.message}")
                        _sectionAttendanceState.value = SectionAttendanceState.Error(result.message)
                    }
                    else -> {
                        _sectionAttendanceState.value = SectionAttendanceState.Error("Unknown error occurred")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading attendance history", e)
                _sectionAttendanceState.value = SectionAttendanceState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Process attendance data to include time details
     * If the API doesn't provide time information, we won't add mock data
     */
    private fun processAttendanceData(studentAttendance: StudentAttendance): StudentAttendance {
        // Create a map for attendance with time information if available
        val attendanceDetails = mutableMapOf<String, StudentAttendance.AttendanceDetail>()
        
        // Process the attendance data from the API
        studentAttendance.attendanceByDate.forEach { (date, present) ->
            // Convert the date string to a proper attendance detail object
            // We're not adding mock time data anymore as it would be misleading
            attendanceDetails[date] = StudentAttendance.AttendanceDetail(
                date = date,
                present = present,
                // Only use real time data from the API if available in the future
                startTime = null,
                scheduleStartTime = null
            )
        }
        
        // Return a new StudentAttendance with the detailed data
        return studentAttendance.copy(
            attendanceData = attendanceDetails
        )
    }
    
    fun resetStates() {
        _attendanceState.value = AttendanceState.Idle
        _sectionAttendanceState.value = SectionAttendanceState.Idle
    }
}

/**
 * Sealed class to represent attendance state
 */
sealed class AttendanceState {
    object Idle : AttendanceState()
    object Loading : AttendanceState()
    data class Success(val attendance: com.example.spot.model.Attendance) : AttendanceState()
    data class Error(val message: String) : AttendanceState()
}

/**
 * Sealed class to represent section attendance state
 */
sealed class SectionAttendanceState {
    object Idle : SectionAttendanceState()
    object Loading : SectionAttendanceState()
    data class Success(val studentAttendance: StudentAttendance) : SectionAttendanceState()
    data class Error(val message: String) : SectionAttendanceState()
}
