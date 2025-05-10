package com.example.spot.repository

import android.util.Log
import com.example.spot.model.PickSeatRequest
import com.example.spot.model.Seat
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository for handling seat related API calls
 */
class SeatRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get all seats for a section
     */
    suspend fun getSeatsBySectionId(sectionId: Long): NetworkResult<List<Seat>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSeatsBySectionId(sectionId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SeatRepository", "Get seats error (Ask Gemini)", e)
                
                // Check if it's the specific authorization error we're handling
                if (e is HttpException && e.code() == 400) {
                    return@withContext NetworkResult.Error("You don't have permission to view seats in this section. Make sure you're enrolled and try again.")
                }
                
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get seat for a specific student in a section
     */
    suspend fun getSeatByStudentAndSectionId(studentId: Long, sectionId: Long): NetworkResult<Seat> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSeatByStudentAndSectionId(studentId, sectionId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SeatRepository", "Get student seat error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get all seats for a section using the more permissive endpoint
     * This endpoint allows students to see seats taken by other students in the section
     */
    suspend fun getAllSeatsForSection(sectionId: Long): NetworkResult<List<Seat>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllSeatsForSection(sectionId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SeatRepository", "Get all seats error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Pick a seat in a section
     */
    suspend fun pickSeat(pickSeatRequest: PickSeatRequest): NetworkResult<Seat> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.pickSeat(pickSeatRequest)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("SeatRepository", "Pick seat error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
