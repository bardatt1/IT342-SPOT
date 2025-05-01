package com.example.spot.repository

import android.util.Log
import com.example.spot.model.ApiResponse
import com.example.spot.model.Attendance
import com.example.spot.model.LogAttendanceRequest
import com.example.spot.model.StudentAttendance
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Repository for handling attendance-related data operations
 */
class AttendanceRepository {
    private val TAG = "AttendanceRepository"
    private val apiService = RetrofitClient.apiService

    /**
     * Get student attendance statistics for a specific section
     */
    suspend fun getStudentAttendanceStats(sectionId: Long): NetworkResult<StudentAttendance> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = TokenManager.getUserId().first() ?: throw IllegalStateException("User ID not found")
                val response = apiService.getStudentAttendanceStatsInSection(sectionId, userId)
                
                // Add debug logging to see what the API is returning
                Log.d(TAG, "Student attendance API response: result=${response.result}, message=${response.message}, data=${response.data}")
                
                if (response.result.equals("SUCCESS", ignoreCase = true) && response.data != null) {
                    Log.d(TAG, "Returning success result with data: ${response.data}")
                    NetworkResult.Success(response.data)
                } else {
                    Log.d(TAG, "Returning error result with message: ${response.message}")
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting attendance stats", e)
                NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    /**
     * Get student attendance in a section
     */
    suspend fun getStudentAttendanceInSection(sectionId: Long, studentId: Long): NetworkResult<StudentAttendance> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStudentAttendanceStatsInSection(sectionId, studentId)
                
                if (response.result == "success" && response.data != null) {
                    return@withContext NetworkResult.Success(response.data)
                } else {
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> {
                        Log.e(TAG, "Authentication error: Unauthorized", e)
                        NetworkResult.Error("Authentication error: Please log out and log back in")
                    }
                    400 -> {
                        Log.e(TAG, "Bad request error", e)
                        NetworkResult.Error("Bad request: Please try again")
                    }
                    403 -> {
                        Log.e(TAG, "Get student attendance stats error: Access Denied", e)
                        NetworkResult.Error("Access Denied: You are not authorized to view this attendance data")
                    }
                    500 -> {
                        Log.e(TAG, "Get student attendance stats error: Server Error", e)
                        NetworkResult.Error("Server Error: Please try again later")
                    }
                    else -> {
                        Log.e(TAG, "Get student attendance stats error (HTTP ${e.code()})", e)
                        NetworkResult.Error("Network error: " + (e.localizedMessage ?: "Unknown error"))
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error while fetching attendance", e)
                NetworkResult.Error("Network error: Unable to connect to server")
            } catch (e: Exception) {
                Log.e(TAG, "Get student attendance stats error", e)
                NetworkResult.Error("Error: " + (e.localizedMessage ?: "Unknown error"))
            }
        }
    }

    /**
     * Log student attendance for a section via QR code scan
     */
    suspend fun logAttendance(sectionId: Long): NetworkResult<Attendance> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.logAttendance(LogAttendanceRequest(sectionId))
                
                if (response.result == "success" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Log attendance error", e)
                NetworkResult.Error("Network error: " + (e.localizedMessage ?: "Unknown error"))
            }
        }
    }

    /**
     * Get attendance records for a student
     */
    suspend fun getAttendanceRecordsByStudentId(studentId: Long): NetworkResult<List<Attendance>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAttendanceByStudentId(studentId)
                
                if (response.result == "success" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Get attendance records error", e)
                NetworkResult.Error("Network error: " + (e.localizedMessage ?: "Unknown error"))
            }
        }
    }
}
