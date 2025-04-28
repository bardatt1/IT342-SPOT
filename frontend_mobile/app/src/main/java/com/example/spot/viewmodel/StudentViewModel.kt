package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Student
import com.example.spot.repository.StudentRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for student profile operations following MVI pattern
 */
class StudentViewModel : ViewModel() {
    
    private val studentRepository = StudentRepository()
    
    // UI States
    private val _studentState = MutableStateFlow<StudentState>(StudentState.Idle)
    val studentState: StateFlow<StudentState> = _studentState
    
    // Profile update state
    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState
    
    /**
     * Load student profile data from repository using the ID from TokenManager
     */
    fun loadStudentProfile() {
        _studentState.value = StudentState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _studentState.value = StudentState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            when (val result = studentRepository.getStudentById(userId)) {
                is NetworkResult.Success -> {
                    _studentState.value = StudentState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _studentState.value = StudentState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Update student profile information
     */
    fun updateProfile(
        id: Long,
        firstName: String,
        middleName: String?,
        lastName: String,
        year: String,
        program: String,
        email: String,
        studentPhysicalId: String,
        password: String?
    ) {
        _profileUpdateState.value = ProfileUpdateState.Loading
        
        viewModelScope.launch {
            when (val result = studentRepository.updateStudent(
                id, firstName, middleName, lastName, year, program, email, studentPhysicalId, password
            )) {
                is NetworkResult.Success -> {
                    _profileUpdateState.value = ProfileUpdateState.Success(result.data)
                    // Refresh the student state with updated data
                    _studentState.value = StudentState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _profileUpdateState.value = ProfileUpdateState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Reset all states to Idle
     */
    fun resetStates() {
        _studentState.value = StudentState.Idle
    }
    
    /**
     * Reset update state
     */
    fun resetUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Idle
    }
}

// States for Student Profile UI
sealed class StudentState {
    object Idle : StudentState()
    object Loading : StudentState()
    data class Success(val student: Student) : StudentState()
    data class Error(val message: String) : StudentState()
}

// States for Profile Update
sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    data class Success(val student: Student) : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}
