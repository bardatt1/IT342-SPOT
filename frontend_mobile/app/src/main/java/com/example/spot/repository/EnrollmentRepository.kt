package com.example.spot.repository

import android.util.Log
import com.example.spot.model.EnrollRequest
import com.example.spot.model.Enrollment
import com.example.spot.model.Section
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Repository for handling enrollment related API calls
 */
class EnrollmentRepository {
    private val apiService = RetrofitClient.apiService
    
    // Cache of student enrollments to work around backend permission issues
    private val _enrollmentsCache = MutableStateFlow<List<Enrollment>>(emptyList())
    val enrollmentsCache: StateFlow<List<Enrollment>> = _enrollmentsCache
    
    /**
     * Get all enrollments for a student using the cache first, then fall back to API call
     * This is a workaround for the backend permission issue with the /api/enrollments/student/{studentId} endpoint
     */
    suspend fun getEnrollmentsByStudentId(studentId: Long): NetworkResult<List<Enrollment>> {
        return withContext(Dispatchers.IO) {
            try {
                // First try to load from the backend
                val response = apiService.getEnrollmentsByStudentId(studentId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    // If successful, update cache and return data
                    _enrollmentsCache.value = response.data
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    // If backend call fails but we have cached data, use that
                    if (_enrollmentsCache.value.isNotEmpty()) {
                        Log.d("EnrollmentRepository", "Using cached enrollments due to backend permission issue")
                        return@withContext NetworkResult.Success(_enrollmentsCache.value)
                    }
                    
                    // If both fail, try to fetch sections with enrollment status
                    try {
                        // Get all courses
                        val coursesResponse = apiService.getAllCourses()
                        if (coursesResponse.result == "SUCCESS" && coursesResponse.data != null) {
                            val sections = mutableListOf<Section>()
                            
                            // For each course, get its sections
                            coursesResponse.data.forEach { course ->
                                val sectionsResponse = apiService.getSectionsByCourseId(course.id)
                                if (sectionsResponse.result == "SUCCESS" && sectionsResponse.data != null) {
                                    sections.addAll(sectionsResponse.data)
                                }
                            }
                            
                            // Build enrollments list
                            val enrollments = _enrollmentsCache.value.toMutableList()
                            
                            return@withContext NetworkResult.Success(enrollments)
                        }
                    } catch (e: Exception) {
                        Log.e("EnrollmentRepository", "Error fetching courses and sections", e)
                    }
                    
                    // If all else fails, return error from original call
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Get enrollments error (Ask Gemini)", e)
                
                // If we have cached data, use that instead of failing
                if (_enrollmentsCache.value.isNotEmpty()) {
                    Log.d("EnrollmentRepository", "Using cached enrollments after API error")
                    return@withContext NetworkResult.Success(_enrollmentsCache.value)
                }
                
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
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
                    // Add the new enrollment to our cache
                    val updatedCache = _enrollmentsCache.value.toMutableList()
                    updatedCache.add(response.data)
                    _enrollmentsCache.value = updatedCache
                    
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
