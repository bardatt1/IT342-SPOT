package com.example.spot.repository

import android.util.Log
import com.example.spot.model.Attendance
import com.example.spot.model.LogAttendanceRequest
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling attendance related API calls
 */
class AttendanceRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Log student attendance for a section via QR code scan
     */
    suspend fun logAttendance(sectionId: Long): NetworkResult<Attendance> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.logAttendance(LogAttendanceRequest(sectionId))
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AttendanceRepository", "Log attendance error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get attendance records for a student
     */
    suspend fun getAttendanceByStudentId(studentId: Long): NetworkResult<List<Attendance>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAttendanceByStudentId(studentId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("AttendanceRepository", "Get attendance records error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
