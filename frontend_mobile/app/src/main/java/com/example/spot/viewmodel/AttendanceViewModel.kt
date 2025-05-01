package com.example.spot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.StudentAttendance
import com.example.spot.repository.AttendanceRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for attendance operations following MVI pattern
 */
class AttendanceViewModel : ViewModel() {
    private val TAG = "AttendanceViewModel"
    private val attendanceRepository = AttendanceRepository()
    
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
                    }
                    is NetworkResult.Error -> {
                        _attendanceState.value = AttendanceState.Error(result.message)
                    }
                    else -> {}
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
            when (val result = attendanceRepository.getStudentAttendanceStats(sectionId)) {
                is NetworkResult.Success<StudentAttendance> -> {
                    // Process attendance data to include time details for late calculation
                    val attendanceData = processAttendanceData(result.data)
                    _sectionAttendanceState.value = SectionAttendanceState.Success(attendanceData)
                }
                is NetworkResult.Error -> {
                    _sectionAttendanceState.value = SectionAttendanceState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Process attendance data to include time details for late calculation
     */
    private fun processAttendanceData(studentAttendance: StudentAttendance): StudentAttendance {
        // Create a map for detailed attendance with time information
        val attendanceDetails = mutableMapOf<String, StudentAttendance.AttendanceDetail>()
        
        // Mock time data for demonstration purposes
        // In a real app, this would come from the backend API
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val classStartTime = LocalTime.parse("08:00", timeFormatter)
        
        // Create detailed attendance data for each date
        studentAttendance.attendanceByDate.forEach { (date, present) ->
            // Mock different arrival times based on whether present or not
            val arrivalTime = if (present) {
                // Generate a random arrival time between 7:45 and 8:30
                val minutes = (Math.random() * 45 - 15).toInt()
                classStartTime.plusMinutes(minutes.toLong())
            } else null
            
            attendanceDetails[date] = StudentAttendance.AttendanceDetail(
                date = date,
                present = present,
                startTime = arrivalTime,
                scheduleStartTime = classStartTime
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
