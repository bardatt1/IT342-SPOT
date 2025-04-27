package com.example.spot.repository

import android.util.Log
import com.example.spot.model.Course
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling course related API calls
 */
class CourseRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get all courses
     */
    suspend fun getAllCourses(): NetworkResult<List<Course>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllCourses()
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("CourseRepository", "Get all courses error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get course by ID
     */
    suspend fun getCourseById(id: Long): NetworkResult<Course> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCourseById(id)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("CourseRepository", "Get course by ID error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get course by course code
     */
    suspend fun getCourseByCourseCode(courseCode: String): NetworkResult<Course> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCourseByCourseCode(courseCode)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("CourseRepository", "Get course by code error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
