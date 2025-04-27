package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.JwtResponse
import com.example.spot.model.Student
import com.example.spot.model.Teacher
import com.example.spot.repository.AuthRepository
import com.example.spot.util.NetworkResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel for authentication following MVI pattern
 */
class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val TAG = "AuthViewModel"
    
    // UI States
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState
    
    private val _bindGoogleState = MutableStateFlow<AuthState>(AuthState.Idle)
    val bindGoogleState: StateFlow<AuthState> = _bindGoogleState
    
    private val _emailCheckState = MutableStateFlow<EmailCheckState>(EmailCheckState.Idle)
    val emailCheckState: StateFlow<EmailCheckState> = _emailCheckState
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState
    
    // State for student Google account binding
    private val _bindStudentGoogleState = MutableStateFlow<BindOAuthState>(BindOAuthState.Idle)
    val bindStudentGoogleState: StateFlow<BindOAuthState> = _bindStudentGoogleState
    
    // State for password change
    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState
    
    // Flag to indicate if the user is using a temporary password
    private val _isTemporaryPassword = MutableStateFlow<Boolean>(false)
    val isTemporaryPassword: StateFlow<Boolean> = _isTemporaryPassword
    
    // Flag to signal that Google account binding should be shown
    private val _showBindGooglePrompt = MutableStateFlow<Boolean>(false)
    val showBindGooglePrompt: StateFlow<Boolean> = _showBindGooglePrompt
    
    // Events
    fun loginUser(email: String, password: String) {
        _loginState.value = AuthState.Loading
        
        viewModelScope.launch {
            Log.d(TAG, "Attempting login for: $email")
            
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Login successful for: $email")
                    
                    // Check if using temporary password
                    checkTemporaryPassword(password)
                    
                    val userData = result.data
                    // Show Google binding prompt if account isn't linked yet and not using temp password
                    if (!userData.googleLinked && !_isTemporaryPassword.value) {
                        Log.d(TAG, "User doesn't have Google account linked, showing binding prompt")
                        _showBindGooglePrompt.value = true
                    } else {
                        _showBindGooglePrompt.value = false
                    }
                    
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
     * Handle Google Sign-In Authentication
     */
    fun handleGoogleSignIn(account: GoogleSignInAccount) {
        _googleSignInState.value = GoogleSignInState.Loading
        
        viewModelScope.launch {
            Log.d(TAG, "Handling Google sign-in for: ${account.email}")
            if (account.id.isNullOrEmpty() || account.email.isNullOrEmpty()) {
                Log.e(TAG, "Invalid Google account data: id or email is null/empty")
                _googleSignInState.value = GoogleSignInState.Error("Invalid Google account data")
                return@launch
            }
            
            // Try signing in with the Google account directly
            performGoogleLogin(account.email!!, account.id!!)
        }
    }
    
    /**
     * Perform login with Google credentials
     */
    fun performGoogleLogin(email: String, googleId: String) {
        _googleSignInState.value = GoogleSignInState.Loading
        
        viewModelScope.launch {
            Log.d(TAG, "Attempting Google login with email: $email, googleId: $googleId")
            
            when (val result = authRepository.loginWithGoogle(email, googleId)) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Google login successful for $email")
                    _loginState.value = AuthState.Success(result.data)
                    _googleSignInState.value = GoogleSignInState.Success(email = email, googleId = googleId)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Google login failed: ${result.message}")
                    // If we have a specific "account not bound" error, we might want to show a different UI
                    // But for now, we'll just show the error message
                    _googleSignInState.value = GoogleSignInState.Error(result.message)
                }
                else -> {
                    Log.e(TAG, "Unknown error during Google login")
                    _googleSignInState.value = GoogleSignInState.Error("Unknown error occurred")
                }
            }
        }
    }
    
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
            when (val result = authRepository.registerStudent(
                firstName, middleName, lastName, year, program, email, studentPhysicalId, password
            )) {
                is NetworkResult.Success -> {
                    _registerState.value = RegisterState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _registerState.value = RegisterState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun bindGoogleAccount(email: String, googleId: String) {
        _bindGoogleState.value = AuthState.Loading
        
        viewModelScope.launch {
            when (val result = authRepository.bindGoogleAccount(email, googleId)) {
                is NetworkResult.Success -> {
                    _bindGoogleState.value = AuthState.Success(null) // Just indicate success, no data needed
                }
                is NetworkResult.Error -> {
                    _bindGoogleState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun bindStudentGoogleAccount(studentId: Long, googleId: String) {
        _bindStudentGoogleState.value = BindOAuthState.Loading
        
        viewModelScope.launch {
            when (val result = authRepository.bindStudentGoogleAccount(studentId, googleId)) {
                is NetworkResult.Success -> {
                    Log.d("AuthViewModel", "Google account bound to student successfully")
                    _bindStudentGoogleState.value = BindOAuthState.Success
                }
                is NetworkResult.Error -> {
                    Log.e("AuthViewModel", "Failed to bind Google account to student: ${result.message}")
                    _bindStudentGoogleState.value = BindOAuthState.Error(result.message)
                }
                else -> {
                    Log.e("AuthViewModel", "Unknown result state during student Google account binding")
                    _bindStudentGoogleState.value = BindOAuthState.Error("Unknown error occurred")
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
            _googleSignInState.value = GoogleSignInState.Idle
        }
    }
    
    /**
     * Reset all states to idle
     */
    fun resetStates() {
        _loginState.value = AuthState.Idle
        _bindGoogleState.value = AuthState.Idle
        _emailCheckState.value = EmailCheckState.Idle
        _registerState.value = RegisterState.Idle
        _googleSignInState.value = GoogleSignInState.Idle
        _bindStudentGoogleState.value = BindOAuthState.Idle
        _passwordChangeState.value = PasswordChangeState.Idle
    }
    
    /**
     * Reset Google binding prompt state
     */
    fun resetBindGooglePrompt() {
        _showBindGooglePrompt.value = false
    }
    
    /**
     * Change student password
     */
    fun changePassword(studentId: Long, newPassword: String) {
        _passwordChangeState.value = PasswordChangeState.Loading
        
        viewModelScope.launch {
            when (val result = authRepository.changePassword(studentId, newPassword)) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Password changed successfully")
                    _passwordChangeState.value = PasswordChangeState.Success
                    _isTemporaryPassword.value = false
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to change password: ${result.message}")
                    _passwordChangeState.value = PasswordChangeState.Error(result.message)
                }
                else -> {
                    Log.e(TAG, "Unknown result state during password change")
                    _passwordChangeState.value = PasswordChangeState.Error("Unknown error occurred")
                }
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
    data class Success(val data: JwtResponse?) : AuthState()
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

// States for Google Sign-In
sealed class GoogleSignInState {
    object Idle : GoogleSignInState()
    object Loading : GoogleSignInState()
    data class Success(val email: String, val googleId: String) : GoogleSignInState()
    object AccountBound : GoogleSignInState()
    object Cancelled : GoogleSignInState()
    data class Error(val message: String) : GoogleSignInState()
}

// States for OAuth binding
sealed class BindOAuthState {
    object Idle : BindOAuthState()
    object Loading : BindOAuthState()
    object Success : BindOAuthState()
    data class Error(val message: String) : BindOAuthState()
}

// States for password change
sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}
