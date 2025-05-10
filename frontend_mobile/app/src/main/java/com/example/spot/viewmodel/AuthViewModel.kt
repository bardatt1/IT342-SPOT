package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.JwtResponse
import com.example.spot.model.Student
import com.example.spot.repository.AuthRepository
import com.example.spot.repository.StudentRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel for authentication following MVI pattern
 */
class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val studentRepository = StudentRepository()
    private val TAG = "AuthViewModel"
    
    // UI States
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState
    
    private val _emailCheckState = MutableStateFlow<EmailCheckState>(EmailCheckState.Idle)
    val emailCheckState: StateFlow<EmailCheckState> = _emailCheckState
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState
    
    // State for password change
    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState
    
    // Flag to indicate if the user is using a temporary password
    private val _isTemporaryPassword = MutableStateFlow<Boolean>(false)
    val isTemporaryPassword: StateFlow<Boolean> = _isTemporaryPassword
    

    
    // Events
    fun loginUser(studentPhysicalId: String, password: String) {
        _loginState.value = AuthState.Loading
        
        viewModelScope.launch {
            Log.d(TAG, "Attempting login for student ID: $studentPhysicalId")
            
            when (val result = authRepository.login(studentPhysicalId, password)) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Login successful for student ID: $studentPhysicalId")
                    
                    // Check if using temporary password
                    checkTemporaryPassword(password)
                    
                    _loginState.value = AuthState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Login failed: ${result.message}")
                    _loginState.value = AuthState.Error(result.message)
                }
                else -> {
                    Log.e(TAG, "Unknown error during login")
                    _loginState.value = AuthState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Register a new student account
     */
    fun registerStudent(
        firstName: String,
        middleName: String?,
        lastName: String,
        year: String,
        program: String,
        email: String,
        studentPhysicalId: String,
        password: String
    ) {
        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            Log.d(TAG, "Registering student: $email, ID: $studentPhysicalId")
            
            when (val result = authRepository.registerStudent(
                firstName, middleName, lastName, year, program, email, studentPhysicalId, password
            )) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Student registration successful")
                    _registerState.value = RegisterState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Student registration failed: ${result.message}")
                    _registerState.value = RegisterState.Error(result.message)
                }
                else -> {
                    Log.e(TAG, "Unknown error during student registration")
                    _registerState.value = RegisterState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    fun checkEmailInUse(email: String) {
        _emailCheckState.value = EmailCheckState.Loading
        
        viewModelScope.launch {
            when (val result = authRepository.checkEmailInUse(email)) {
                is NetworkResult.Success -> {
                    _emailCheckState.value = EmailCheckState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _emailCheckState.value = EmailCheckState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = AuthState.Idle
        }
    }
    
    /**
     * Reset all states to idle
     */
    fun resetStates() {
        _loginState.value = AuthState.Idle
        _emailCheckState.value = EmailCheckState.Idle
        _registerState.value = RegisterState.Idle
        _passwordChangeState.value = PasswordChangeState.Idle
    }
    

    
    /**
     * Change the password for a user
     */
    fun changePassword(studentPhysicalId: String, newPassword: String) {
        _passwordChangeState.value = PasswordChangeState.Loading
        
        viewModelScope.launch {
            Log.d(TAG, "Changing password for student ID: $studentPhysicalId")
            
            try {
                // Get the user ID from TokenManager instead of looking up by physical ID
                val userId = TokenManager.getUserId().first()
                
                if (userId == null) {
                    Log.e(TAG, "User ID not found in TokenManager")
                    _passwordChangeState.value = PasswordChangeState.Error("User ID not found. Please log in again.")
                    return@launch
                }
                
                Log.d(TAG, "Using user ID from token: $userId")
                
                // Now call the password change with the stored user ID
                when (val result = authRepository.changePassword(userId, newPassword)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Password change successful")
                        _passwordChangeState.value = PasswordChangeState.Success
                        _isTemporaryPassword.value = false
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Password change failed: ${result.message}")
                        _passwordChangeState.value = PasswordChangeState.Error(result.message)
                    }
                    else -> {
                        Log.e(TAG, "Unknown error during password change")
                        _passwordChangeState.value = PasswordChangeState.Error("Unknown error occurred")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password", e)
                _passwordChangeState.value = PasswordChangeState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Check if user is using a temporary password
     * This is determined by checking if the password matches the default pattern
     */
    fun checkTemporaryPassword(password: String) {
        // For demo purposes, we consider "temporary" as the default temp password
        val isTemporary = password == "temporary"
        _isTemporaryPassword.value = isTemporary
        Log.d(TAG, "Is using temporary password: $isTemporary")
    }
}

// States for Auth UI
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: JwtResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class EmailCheckState {
    object Idle : EmailCheckState()
    object Loading : EmailCheckState()
    data class Success(val isInUse: Boolean) : EmailCheckState()
    data class Error(val message: String) : EmailCheckState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val data: Student) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

// States for password change
sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}


