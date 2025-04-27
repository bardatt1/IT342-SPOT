package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Attendance
import com.example.spot.repository.AttendanceRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for attendance operations following MVI pattern
 */
class AttendanceViewModel : ViewModel() {
    
    private val attendanceRepository = AttendanceRepository()
    
    // UI States
    private val _attendanceLogState = MutableStateFlow<AttendanceLogState>(AttendanceLogState.Idle)
    val attendanceLogState: StateFlow<AttendanceLogState> = _attendanceLogState
    
    private val _studentAttendanceState = MutableStateFlow<StudentAttendanceState>(StudentAttendanceState.Idle)
    val studentAttendanceState: StateFlow<StudentAttendanceState> = _studentAttendanceState
    
    // Events
    fun logAttendance(sectionId: Long) {
        _attendanceLogState.value = AttendanceLogState.Loading
        
        viewModelScope.launch {
            when (val result = attendanceRepository.logAttendance(sectionId)) {
                is NetworkResult.Success -> {
                    _attendanceLogState.value = AttendanceLogState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _attendanceLogState.value = AttendanceLogState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun loadStudentAttendance() {
        _studentAttendanceState.value = StudentAttendanceState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _studentAttendanceState.value = StudentAttendanceState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            when (val result = attendanceRepository.getAttendanceByStudentId(userId)) {
                is NetworkResult.Success -> {
                    _studentAttendanceState.value = StudentAttendanceState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _studentAttendanceState.value = StudentAttendanceState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetStates() {
        _attendanceLogState.value = AttendanceLogState.Idle
        _studentAttendanceState.value = StudentAttendanceState.Idle
    }
}

// States for Attendance UI
sealed class AttendanceLogState {
    object Idle : AttendanceLogState()
    object Loading : AttendanceLogState()
    data class Success(val attendance: Attendance) : AttendanceLogState()
    data class Error(val message: String) : AttendanceLogState()
}

sealed class StudentAttendanceState {
    object Idle : StudentAttendanceState()
    object Loading : StudentAttendanceState()
    data class Success(val attendances: List<Attendance>) : StudentAttendanceState()
    data class Error(val message: String) : StudentAttendanceState()
}
