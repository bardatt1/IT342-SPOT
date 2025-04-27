package com.example.spot.repository

import android.util.Log
import com.example.spot.model.ApiResponse
import com.example.spot.model.CreateStudentRequest
import com.example.spot.model.JwtResponse
import com.example.spot.model.LoginRequest
import com.example.spot.model.Student
import com.example.spot.model.StudentUpdateRequest
import com.example.spot.model.Teacher
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository for handling authentication related API calls
 */
class AuthRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): NetworkResult<JwtResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Attempting login for: $email")
                
                // Create the login request
                val loginRequest = LoginRequest(email, password)
                Log.d("AuthRepository", "Login request created: $loginRequest")
                
                // Make the API call
                val response = try {
                    apiService.login(loginRequest)
                } catch (e: HttpException) {
                    // Handle HTTP error responses
                    if (e.code() == 401) {
                        Log.e("AuthRepository", "Authentication failed (401): Bad credentials")
                        return@withContext NetworkResult.Error("Invalid email or password")
                    } else {
                        Log.e("AuthRepository", "HTTP error during login: ${e.code()}")
                        return@withContext NetworkResult.Error("Server error: ${e.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Login error", e)
                    return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
                }
                
                // Handle successful response
                if (response.result == "SUCCESS" && response.data != null) {
                    Log.d("AuthRepository", "Login successful for: $email")
                    // Save authentication data
                    TokenManager.saveAuthData(
                        token = response.data.accessToken,
                        userId = response.data.id,
                        userType = response.data.userType,
                        email = response.data.email,
                        name = response.data.name
                    )
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    Log.e("AuthRepository", "Login failed: ${response.message}")
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected login error", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Login with Google OAuth
     */
    suspend fun loginWithGoogle(email: String, googleId: String): NetworkResult<JwtResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Initiating Google login for: $email")
                
                // First check if the email exists in the system
                val emailCheckResponse = try {
                    apiService.checkEmailInUse(email)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Failed to check email existence", e)
                    return@withContext NetworkResult.Error("Failed to verify email account: ${e.localizedMessage}")
                }
                
                if (emailCheckResponse.result == "SUCCESS") {
                    val emailExists = emailCheckResponse.data ?: false
                    
                    if (emailExists) {
                        Log.d("AuthRepository", "Email exists, attempting regular login with Google credentials")
                        // Email exists, try regular login with placeholder password
                        val loginResponse = try {
                            apiService.login(LoginRequest(email, "google-oauth-placeholder"))
                        } catch (e: HttpException) {
                            if (e.code() == 401) {
                                // Try binding the Google account and then login again
                                Log.d("AuthRepository", "Login failed, trying to bind Google account")
                                return@withContext bindAndLogin(email, googleId)
                            } else {
                                Log.e("AuthRepository", "HTTP error during Google login: ${e.code()}", e)
                                return@withContext NetworkResult.Error("Server error: ${e.message()}")
                            }
                        } catch (e: Exception) {
                            Log.e("AuthRepository", "Error during Google login", e)
                            return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
                        }
                        
                        // Process successful response
                        if (loginResponse.result == "SUCCESS" && loginResponse.data != null) {
                            Log.d("AuthRepository", "Google login successful")
                            TokenManager.saveAuthData(
                                token = loginResponse.data.accessToken,
                                userId = loginResponse.data.id,
                                userType = loginResponse.data.userType,
                                email = loginResponse.data.email,
                                name = loginResponse.data.name
                            )
                            return@withContext NetworkResult.Success(loginResponse.data)
                        } else {
                            Log.e("AuthRepository", "Google login failed with error: ${loginResponse.message}")
                            return@withContext NetworkResult.Error(loginResponse.message)
                        }
                    } else {
                        // Email doesn't exist, need to register first
                        Log.d("AuthRepository", "Email not found in system, registration required")
                        return@withContext NetworkResult.Error("Account not found. Please register first.")
                    }
                } else {
                    Log.e("AuthRepository", "Email check failed: ${emailCheckResponse.message}")
                    return@withContext NetworkResult.Error(emailCheckResponse.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error during Google login", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Helper method to bind a Google account and then login
     */
    private suspend fun bindAndLogin(email: String, googleId: String): NetworkResult<JwtResponse> {
        try {
            Log.d("AuthRepository", "Attempting to bind Google account for: $email")
            val bindResponse = apiService.bindGoogleAccount(email, googleId)
            
            if (bindResponse.result == "SUCCESS" && bindResponse.data == true) {
                Log.d("AuthRepository", "Binding successful, attempting login")
                // Binding successful, try login again
                val loginResponse = try {
                    apiService.login(LoginRequest(email, "google-oauth-placeholder"))
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Login failed after binding", e)
                    return NetworkResult.Error("Login failed after binding Google account: ${e.localizedMessage}")
                }
                
                if (loginResponse.result == "SUCCESS" && loginResponse.data != null) {
                    Log.d("AuthRepository", "Login successful after binding")
                    TokenManager.saveAuthData(
                        token = loginResponse.data.accessToken,
                        userId = loginResponse.data.id,
                        userType = loginResponse.data.userType,
                        email = loginResponse.data.email,
                        name = loginResponse.data.name
                    )
                    return NetworkResult.Success(loginResponse.data)
                } else {
                    Log.e("AuthRepository", "Login failed after binding: ${loginResponse.message}")
                    return NetworkResult.Error(loginResponse.message)
                }
            } else {
                Log.e("AuthRepository", "Failed to bind Google account: ${bindResponse.message}")
                return NetworkResult.Error("Failed to bind Google account: ${bindResponse.message}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during bind and login", e)
            return NetworkResult.Error("Error during bind and login: ${e.localizedMessage}")
        }
    }
    
    /**
     * Bind Google account to existing user account
     */
    suspend fun bindGoogleAccount(email: String, googleId: String): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Binding Google account for email: $email, googleId: $googleId")
                val response = apiService.bindGoogleAccount(email, googleId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    Log.d("AuthRepository", "Google account bound successfully")
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    Log.e("AuthRepository", "Failed to bind Google account: ${response.message}")
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Bind Google account error", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Bind Google account to student account
     */
    suspend fun bindStudentGoogleAccount(studentId: Long, googleId: String): NetworkResult<Student?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Binding Google account for student ID: $studentId, googleId: $googleId")
                val response = apiService.bindStudentGoogleAccount(studentId, googleId)
                
                if (response.result == "SUCCESS") {
                    Log.d("AuthRepository", "Google account bound successfully to student")
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    Log.e("AuthRepository", "Failed to bind Google account to student: ${response.message}")
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Bind student Google account error", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Bind Google account to teacher account
     */
    suspend fun bindTeacherGoogleAccount(teacherId: Long, googleId: String): NetworkResult<Teacher?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Binding Google account for teacher ID: $teacherId, googleId: $googleId")
                val response = apiService.bindTeacherGoogleAccount(teacherId, googleId)
                
                if (response.result == "SUCCESS") {
                    Log.d("AuthRepository", "Google account bound successfully to teacher")
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    Log.e("AuthRepository", "Failed to bind Google account to teacher: ${response.message}")
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Bind teacher Google account error", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Register a new student
     */
    suspend fun registerStudent(
        firstName: String,
        middleName: String?,
        lastName: String,
        year: String,
        program: String,
        email: String,
        studentPhysicalId: String,
        password: String
    ): NetworkResult<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateStudentRequest(
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    year = year,
                    program = program,
                    email = email,
                    studentPhysicalId = studentPhysicalId,
                    password = password
                )
                
                val response = apiService.registerStudent(request)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Student registration error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Check if email is already in use
     */
    suspend fun checkEmailInUse(email: String): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkEmailInUse(email)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Check email error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Logout user by clearing auth data
     */
    suspend fun logout() {
        TokenManager.clearAuthData()
    }
    
    /**
     * Update student information including password
     */
    suspend fun updateStudent(studentId: Long, updateRequest: StudentUpdateRequest): NetworkResult<Student?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Updating student: $studentId")
                val response = apiService.updateStudent(studentId, updateRequest)
                
                if (response.result == "SUCCESS") {
                    Log.d("AuthRepository", "Student updated successfully")
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    Log.e("AuthRepository", "Failed to update student: ${response.message}")
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error updating student", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Change student password
     */
    suspend fun changePassword(studentId: Long, newPassword: String): NetworkResult<Student?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Changing password for student: $studentId")
                val updateRequest = StudentUpdateRequest(password = newPassword)
                
                val response = apiService.updateStudent(studentId, updateRequest)
                
                if (response.result == "SUCCESS") {
                    Log.d("AuthRepository", "Password changed successfully")
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    Log.e("AuthRepository", "Failed to change password: ${response.message}")
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error changing password", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
