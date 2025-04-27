package com.example.spot.repository

import android.util.Log
import com.example.spot.model.Section
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling section related API calls
 */
class SectionRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get sections by course ID
     */
    suspend fun getSectionsByCourseId(courseId: Long): NetworkResult<List<Section>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSectionsByCourseId(courseId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get sections error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get section by ID
     */
    suspend fun getSectionById(id: Long): NetworkResult<Section> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSectionById(id)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get section by ID error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get section by enrollment key
     */
    suspend fun getSectionByEnrollmentKey(enrollmentKey: String): NetworkResult<Section> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSectionByEnrollmentKey(enrollmentKey)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message ?: "Error fetching section")
                }
            } catch (e: Exception) {
                Log.e("SectionRepository", "Get section by enrollment key error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
