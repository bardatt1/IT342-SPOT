package com.example.spot.repository

import android.util.Log
import com.example.spot.model.PickSeatRequest
import com.example.spot.model.Seat
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                Log.e("SeatRepository", "Get seats error", e)
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
     * Pick a seat in a section
     */
    suspend fun pickSeat(studentId: Long, sectionId: Long, row: Int, column: Int): NetworkResult<Seat> {
        return withContext(Dispatchers.IO) {
            try {
                val seatRequest = PickSeatRequest(studentId, sectionId, row, column)
                val response = apiService.pickSeat(seatRequest)
                
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
