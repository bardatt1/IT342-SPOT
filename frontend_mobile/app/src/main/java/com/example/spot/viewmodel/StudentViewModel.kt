package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Student
import com.example.spot.model.StudentUpdateRequest
import com.example.spot.repository.StudentRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import com.example.spot.util.NotificationLogger
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
                else -> {
                    _studentState.value = StudentState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Update student profile information
     */
    fun updateProfile(
        studentId: Long,
        firstName: String,
        middleName: String?,
        lastName: String,
        studentYear: String,
        studentProgram: String,
        studentEmail: String,
        studentPhysicalId: String,
        newPassword: String? = null,
        currentPassword: String? = null
    ) {
        _profileUpdateState.value = ProfileUpdateState.Loading
        viewModelScope.launch {
            val studentUpdateRequest = StudentUpdateRequest(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                year = studentYear,
                program = studentProgram,
                email = studentEmail,
                studentPhysicalId = studentPhysicalId,
                password = newPassword,
                currentPassword = currentPassword
            )
            
            when (val result = studentRepository.updateStudent(studentId, studentUpdateRequest)) {
                is NetworkResult.Success -> {
                    _profileUpdateState.value = ProfileUpdateState.Success(result.data)
                    // Refresh the student state with updated data
                    _studentState.value = StudentState.Success(result.data)
                    
                    // Log profile update in notifications
                    val fieldsUpdated = mutableListOf<String>()
                    
                    // Determine which fields were updated by comparing with current state
                    val currentStudentState = _studentState.value
                    if (currentStudentState is StudentState.Success) {
                        val currentStudent = currentStudentState.student
                        
                        // Check which fields were updated
                        if (firstName != currentStudent.firstName) fieldsUpdated.add("First Name")
                        if (middleName != currentStudent.middleName) fieldsUpdated.add("Middle Name")
                        if (lastName != currentStudent.lastName) fieldsUpdated.add("Last Name")
                        if (studentYear != currentStudent.year) fieldsUpdated.add("Year")
                        if (studentProgram != currentStudent.program) fieldsUpdated.add("Program")
                        if (studentEmail != currentStudent.email) fieldsUpdated.add("Email")
                        if (studentPhysicalId != currentStudent.studentPhysicalId) fieldsUpdated.add("Student ID")
                        if (newPassword != null) fieldsUpdated.add("Password")
                    }
                    
                    // If we couldn't determine specific fields, just log generic update
                    if (fieldsUpdated.isEmpty()) {
                        NotificationLogger.logProfileUpdate("Profile information", studentId)
                    } else {
                        // Log specific fields that were updated
                        val updatedFieldsStr = fieldsUpdated.joinToString(", ")
                        NotificationLogger.logProfileUpdate(updatedFieldsStr, studentId)
                    }
                    
                    // If password was changed, log it separately
                    if (newPassword != null) {
                        NotificationLogger.logPasswordChange(studentId)
                    }
                }
                is NetworkResult.Error -> {
                    _profileUpdateState.value = ProfileUpdateState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    // This branch should not be reached as we're setting the loading state before the API call
                    _profileUpdateState.value = ProfileUpdateState.Loading
                }
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
