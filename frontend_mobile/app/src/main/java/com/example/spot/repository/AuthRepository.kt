package com.example.spot.repository

import android.util.Log
import com.example.spot.model.ApiResponse
import com.example.spot.model.CreateStudentRequest
import com.example.spot.model.JwtResponse
import com.example.spot.model.LoginRequest
import com.example.spot.model.Student
import com.example.spot.model.StudentIdLoginRequest
import com.example.spot.model.StudentUpdateRequest
import com.example.spot.model.Teacher
import com.example.spot.network.RetrofitClient
import com.example.spot.repository.StudentRepository
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
    private val studentRepository = StudentRepository()
    
    /**
     * Login user with student physical ID and password
     */
    suspend fun login(studentPhysicalId: String, password: String): NetworkResult<JwtResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Attempting login with student ID: $studentPhysicalId")
                
                // Determine if input is an email or a student ID
                val isEmail = studentPhysicalId.contains("@")
                
                if (isEmail) {
                    // If it's already an email, use it directly
                    return@withContext loginWithEmail(studentPhysicalId, password)
                } else {
                    // For student IDs, use the dedicated endpoint
                    return@withContext loginWithStudentId(studentPhysicalId, password)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected login error", e)
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Login with student physical ID and password using the dedicated endpoint
     */
    private suspend fun loginWithStudentId(studentId: String, password: String): NetworkResult<JwtResponse> {
        try {
            Log.d("AuthRepository", "Attempting login with student ID endpoint: $studentId")
            
            // Create the student ID login request
            val loginRequest = StudentIdLoginRequest(studentId, password)
            Log.d("AuthRepository", "StudentID login request created: $loginRequest")
            
            // Make the API call to the special student ID endpoint
            val response = try {
                apiService.loginWithStudentId(loginRequest)
            } catch (e: HttpException) {
                // Handle HTTP error responses
                if (e.code() == 401) {
                    Log.e("AuthRepository", "Authentication failed (401): Bad credentials for student ID")
                    return NetworkResult.Error("Invalid student ID or password")
                } else {
                    Log.e("AuthRepository", "HTTP error during student ID login: ${e.code()}")
                    return NetworkResult.Error("Server error: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Student ID login error", e)
                return NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
            
            // Handle successful response
            if (response.result == "SUCCESS" && response.data != null) {
                Log.d("AuthRepository", "Login successful for student ID: $studentId")
                // Save authentication data
                TokenManager.saveAuthData(
                    token = response.data.accessToken,
                    userId = response.data.id,
                    userType = response.data.userType,
                    email = response.data.email,
                    name = response.data.name
                )
                return NetworkResult.Success(response.data)
            } else {
                Log.e("AuthRepository", "Login failed: ${response.message}")
                return NetworkResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected login error", e)
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }
    
    /**
     * Login with email and password
     */
    private suspend fun loginWithEmail(email: String, password: String): NetworkResult<JwtResponse> {
        try {
            Log.d("AuthRepository", "Attempting login with email: $email")
            
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
                    return NetworkResult.Error("Invalid student ID or password")
                } else {
                    Log.e("AuthRepository", "HTTP error during login: ${e.code()}")
                    return NetworkResult.Error("Server error: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Login error", e)
                return NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
            
            // Handle successful response
            if (response.result == "SUCCESS" && response.data != null) {
                Log.d("AuthRepository", "Login successful for email: $email")
                // Save authentication data
                TokenManager.saveAuthData(
                    token = response.data.accessToken,
                    userId = response.data.id,
                    userType = response.data.userType,
                    email = response.data.email,
                    name = response.data.name
                )
                return NetworkResult.Success(response.data)
            } else {
                Log.e("AuthRepository", "Login failed: ${response.message}")
                return NetworkResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected login error", e)
            return NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }
    
    /**
     * Login with Google OAuth using mobile app-specific flow
     */
    suspend fun loginWithGoogle(email: String, googleId: String): NetworkResult<JwtResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Initiating direct Google OAuth login for: $email with ID: $googleId")
                
                // APPROACH 1: Try the dedicated Google OAuth endpoint first (direct OAuth flow)
                try {
                    Log.d("AuthRepository", "Attempting to use direct OAuth endpoint")
                    val oauthResponse = apiService.googleOauthLogin(
                        registrationType = "student", // Default to student, can be changed based on app logic
                        email = email,
                        googleId = googleId
                    )
                    
                    if (oauthResponse.result == "SUCCESS" && oauthResponse.data != null) {
                        Log.d("AuthRepository", "Direct OAuth login successful")
                        TokenManager.saveAuthData(
                            token = oauthResponse.data.accessToken,
                            userId = oauthResponse.data.id,
                            userType = oauthResponse.data.userType,
                            email = oauthResponse.data.email,
                            name = oauthResponse.data.name
                        )
                        return@withContext NetworkResult.Success(oauthResponse.data)
                    }
                    
                    // If we get here, the direct OAuth didn't work but didn't throw an exception
                    Log.w("AuthRepository", "Direct OAuth returned unsuccessful result: ${oauthResponse.message}")
                    // Fall through to try binding approach
                } catch (e: Exception) {
                    // Log the error but continue to try the binding approach
                    Log.w("AuthRepository", "Direct OAuth endpoint failed, trying binding approach", e)
                    // Don't return here, continue to binding approach
                }

                // APPROACH 2: Try to bind the Google account and then log in
                try {
                    Log.d("AuthRepository", "Trying to bind Google account: $email with ID: $googleId")
                    val bindResponse = apiService.bindGoogleAccount(email, googleId)
                    
                    if (bindResponse.result == "SUCCESS") {
                        Log.d("AuthRepository", "Google account binding successful, attempting login")
                        
                        // Now try to login with the bound account
                        val loginResponse = apiService.login(LoginRequest(email, "google-oauth-placeholder"))
                        
                        if (loginResponse.result == "SUCCESS" && loginResponse.data != null) {
                            Log.d("AuthRepository", "Login after binding successful")
                            TokenManager.saveAuthData(
                                token = loginResponse.data.accessToken,
                                userId = loginResponse.data.id,
                                userType = loginResponse.data.userType,
                                email = loginResponse.data.email,
                                name = loginResponse.data.name
                            )
                            return@withContext NetworkResult.Success(loginResponse.data)
                        } else {
                            Log.e("AuthRepository", "Login after binding failed: ${loginResponse.message}")
                            return@withContext NetworkResult.Error("Binding succeeded but login failed: ${loginResponse.message}")
                        }
                    } else {
                        Log.e("AuthRepository", "Google account binding failed: ${bindResponse.message}")
                        return@withContext NetworkResult.Error("Google account binding failed: ${bindResponse.message}")
                    }
                } catch (e: HttpException) {
                    // If we get a 401 error, the user might not exist or credentials are wrong
                    if (e.code() == 401 || e.code() == 404) {
                        Log.e("AuthRepository", "User not found or authentication failed during binding")
                        return@withContext NetworkResult.Error("Account not found or authentication failed. Please register first.")
                    } else {
                        Log.e("AuthRepository", "HTTP error during binding: ${e.code()}", e)
                        return@withContext NetworkResult.Error("Server error (${e.code()}): ${e.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Unexpected error during Google binding", e)
                    return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Overall failure in Google login process", e)
                return@withContext NetworkResult.Error("Failed to complete Google Sign-In: ${e.localizedMessage}")
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
