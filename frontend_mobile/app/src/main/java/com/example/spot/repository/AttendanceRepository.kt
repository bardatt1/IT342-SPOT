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
                Log.d(TAG, "Fetching attendance stats for user ID: $userId in section: $sectionId")
                
                val response = apiService.getStudentAttendanceStatsInSection(sectionId, userId)
                
                // Enhanced logging to debug the API response
                Log.d(TAG, "Student attendance API response: result=${response.result}, message=${response.message}")
                Log.d(TAG, "Attendance data received: ${response.data?.attendanceByDate?.size ?: 0} dates")
                
                if (response.result.equals("SUCCESS", ignoreCase = true) && response.data != null) {
                    val attendanceData = response.data
                    
                    // Validate the received data before returning
                    if (attendanceData.attendanceByDate.isEmpty()) {
                        Log.w(TAG, "Received empty attendance data from server")
                    } else {
                        Log.d(TAG, "Attendance dates: ${attendanceData.attendanceByDate.keys.joinToString(", ")}")
                    }
                    
                    NetworkResult.Success(attendanceData)
                } else {
                    Log.d(TAG, "Returning error result with message: ${response.message}")
                    NetworkResult.Error(response.message)
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> {
                        Log.e(TAG, "Authentication error: Unauthorized", e)
                        NetworkResult.Error("Authentication error: Please log out and log back in")
                    }
                    403 -> {
                        Log.e(TAG, "Access denied when fetching attendance", e)
                        NetworkResult.Error("Access denied: You don't have permission to view this attendance data")
                    }
                    500 -> {
                        Log.e(TAG, "Server error when fetching attendance", e)
                        NetworkResult.Error("Server error: Please try again later")
                    }
                    else -> {
                        Log.e(TAG, "HTTP error ${e.code()} when fetching attendance", e)
                        NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network connection error", e)
                NetworkResult.Error("Network error: Unable to connect to server")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting attendance stats", e)
                NetworkResult.Error("Error: ${e.localizedMessage ?: "Unknown error"}")
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
                
                if (response.result.equals("SUCCESS", ignoreCase = true) && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: HttpException) {
                // Extract response body for error details if available
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "Log attendance error (HTTP ${e.code()}): $errorBody", e)
                
                return@withContext when (e.code()) {
                    400 -> {
                        // Check for known error patterns directly in the error body
                        val lowerErrorBody = errorBody?.lowercase() ?: ""
                        
                        // Special check for duplicate attendance patterns
                        if (lowerErrorBody.contains("already recorded") || 
                            lowerErrorBody.contains("already marked") || 
                            lowerErrorBody.contains("duplicate")) {
                            // For duplicate attendance, return a special format that can be detected by UI
                            Log.i(TAG, "Duplicate attendance detected: $errorBody")
                            NetworkResult.Error("[DUPLICATE_ATTENDANCE] Attendance already recorded for today")
                        } else {
                            NetworkResult.Error("Invalid request: Please try again")
                        }
                    }
                    401 -> NetworkResult.Error("Authentication error: Please log in again")
                    403 -> NetworkResult.Error("Access denied: You don't have permission")
                    500 -> NetworkResult.Error("Server error: Please try again later")
                    else -> NetworkResult.Error("Network error: HTTP ${e.code()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Log attendance network error", e)
                return@withContext NetworkResult.Error("Network error: Unable to connect to server")
            } catch (e: Exception) {
                Log.e(TAG, "Log attendance error (Ask Gemini)", e)
                return@withContext NetworkResult.Error("Error: " + (e.localizedMessage ?: "Unknown error"))
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
