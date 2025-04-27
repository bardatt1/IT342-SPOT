package com.example.spot.repository

import android.util.Log
import com.example.spot.model.Student
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
}
