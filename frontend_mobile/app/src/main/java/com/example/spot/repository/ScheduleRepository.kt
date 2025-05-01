package com.example.spot.repository

import com.example.spot.model.ApiResponse
import com.example.spot.model.Schedule
import com.example.spot.network.ApiService
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import java.io.IOException

/**
 * Repository for handling schedule-related API calls
 */
class ScheduleRepository {
    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * Get schedules for a specific section
     * @param sectionId The ID of the section to get schedules for
     * @return NetworkResult containing a list of Schedule objects if successful
     */
    suspend fun getSchedulesBySectionId(sectionId: Long): NetworkResult<List<Schedule>> {
        return try {
            // Call the API service directly without the token parameter
            val response: ApiResponse<List<Schedule>> = apiService.getSchedulesBySectionId(sectionId)
            
            // Process the ApiResponse directly, not using isSuccessful or body()
            if (response.result == "SUCCESS") {
                NetworkResult.Success(response.data ?: emptyList())
            } else {
                NetworkResult.Error(response.message)
            }
        } catch (e: IOException) {
            NetworkResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            NetworkResult.Error("Unexpected error: ${e.message}")
        }
    }
}
