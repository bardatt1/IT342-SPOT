package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Enrollment
import com.example.spot.repository.EnrollmentRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for enrollment operations following MVI pattern
 */
class EnrollmentViewModel : ViewModel() {
    
    private val enrollmentRepository = EnrollmentRepository()
    
    // UI States
    private val _enrollmentsState = MutableStateFlow<EnrollmentsState>(EnrollmentsState.Idle)
    val enrollmentsState: StateFlow<EnrollmentsState> = _enrollmentsState
    
    private val _enrollmentStatus = MutableStateFlow<EnrollmentStatusState>(EnrollmentStatusState.Idle)
    val enrollmentStatus: StateFlow<EnrollmentStatusState> = _enrollmentStatus
    
    private val _enrollAction = MutableStateFlow<EnrollActionState>(EnrollActionState.Idle)
    val enrollAction: StateFlow<EnrollActionState> = _enrollAction
    
    // Events
    fun loadStudentEnrollments() {
        _enrollmentsState.value = EnrollmentsState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _enrollmentsState.value = EnrollmentsState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            when (val result = enrollmentRepository.getEnrollmentsByStudentId(userId)) {
                is NetworkResult.Success -> {
                    _enrollmentsState.value = EnrollmentsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _enrollmentsState.value = EnrollmentsState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun checkEnrollmentStatus(sectionId: Long) {
        _enrollmentStatus.value = EnrollmentStatusState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _enrollmentStatus.value = EnrollmentStatusState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            when (val result = enrollmentRepository.isStudentEnrolled(userId, sectionId)) {
                is NetworkResult.Success -> {
                    _enrollmentStatus.value = EnrollmentStatusState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _enrollmentStatus.value = EnrollmentStatusState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun enrollInSection(enrollmentKey: String) {
        _enrollAction.value = EnrollActionState.Loading
        
        viewModelScope.launch {
            when (val result = enrollmentRepository.enrollStudent(enrollmentKey)) {
                is NetworkResult.Success -> {
                    _enrollAction.value = EnrollActionState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _enrollAction.value = EnrollActionState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetStates() {
        _enrollmentsState.value = EnrollmentsState.Idle
        _enrollmentStatus.value = EnrollmentStatusState.Idle
        _enrollAction.value = EnrollActionState.Idle
    }
}

// States for Enrollment UI
sealed class EnrollmentsState {
    object Idle : EnrollmentsState()
    object Loading : EnrollmentsState()
    data class Success(val enrollments: List<Enrollment>) : EnrollmentsState()
    data class Error(val message: String) : EnrollmentsState()
}

sealed class EnrollmentStatusState {
    object Idle : EnrollmentStatusState()
    object Loading : EnrollmentStatusState()
    data class Success(val isEnrolled: Boolean) : EnrollmentStatusState()
    data class Error(val message: String) : EnrollmentStatusState()
}

sealed class EnrollActionState {
    object Idle : EnrollActionState()
    object Loading : EnrollActionState()
    data class Success(val enrollment: Enrollment) : EnrollActionState()
    data class Error(val message: String) : EnrollActionState()
}
