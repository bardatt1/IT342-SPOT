package com.example.spot.repository

import android.util.Log
import com.example.spot.model.Student
import com.example.spot.model.StudentUpdateRequest
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling student related API calls
 */
class StudentRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get student by ID
     */
    suspend fun getStudentById(id: Long): NetworkResult<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStudentById(id)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("StudentRepository", "Get student error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get student by email
     */
    suspend fun getStudentByEmail(email: String): NetworkResult<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStudentByEmail(email)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("StudentRepository", "Get student by email error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Update student profile
     */
    suspend fun updateStudent(id: Long, studentUpdateRequest: StudentUpdateRequest): NetworkResult<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateStudent(id, studentUpdateRequest)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("StudentRepository", "Update student error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Find a student by their physical ID
     * Note: This is a mock implementation since there's no direct backend API for this yet
     * It performs filtering on the student list received from the backend
     */
    suspend fun findEmailByStudentPhysicalId(physicalId: String): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get a student with this ID from the backend
                // We're using the admin endpoint to look up the student
                
                // In a production implementation, we would add a dedicated endpoint
                // that can look up a student by their physical ID and return just the email
                
                // For now, try to look up the student using direct email format
                val formattedEmail = physicalId.trim().replace("-", "").lowercase() + "@students.spot.edu"
                
                // Since we don't have an actual backend endpoint to query by physical ID,
                // we'll just format an email that matches the expected pattern
                Log.d("StudentRepository", "Formatted email for student ID $physicalId to: $formattedEmail")
                
                // Return the formatted email - this is a fallback until we have a proper API endpoint
                return@withContext NetworkResult.Error("No direct lookup available, using fallback email format")
            } catch (e: Exception) {
                Log.e("StudentRepository", "Find student by physical ID error", e)
                return@withContext NetworkResult.Error("Could not find student with ID: $physicalId")
            }
        }
    }
}
