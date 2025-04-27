package com.example.spot.repository

import android.util.Log
import com.example.spot.model.EnrollRequest
import com.example.spot.model.Enrollment
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling enrollment related API calls
 */
class EnrollmentRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get all enrollments for a student
     */
    suspend fun getEnrollmentsByStudentId(studentId: Long): NetworkResult<List<Enrollment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEnrollmentsByStudentId(studentId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Get enrollments error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Check if a student is enrolled in a section
     */
    suspend fun isStudentEnrolled(studentId: Long, sectionId: Long): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.isStudentEnrolled(studentId, sectionId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Check enrollment error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Enroll a student in a section using an enrollment key
     */
    suspend fun enrollStudent(enrollmentKey: String): NetworkResult<Enrollment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.enrollStudent(EnrollRequest(enrollmentKey))
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Enroll student error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
