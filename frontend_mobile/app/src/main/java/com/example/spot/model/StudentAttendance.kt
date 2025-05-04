package com.example.spot.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime

/**
 * Model representing student attendance statistics for a section
 */
data class StudentAttendance(
    val studentId: Long,
    val studentName: String,
    val sectionId: Long,
    val totalClassDays: Int,
    val daysPresent: Int,
    val attendanceRate: Float,
    @SerializedName("attendanceByDate") private val _attendanceByDate: Map<String, Boolean>,
    val attendanceData: Map<String, AttendanceDetail> = emptyMap()
) {
    // Convert the backend date format (ISO-8601: "yyyy-MM-dd") to a Map<String, Boolean>
    val attendanceByDate: Map<String, Boolean> get() = _attendanceByDate
    
    // Computed properties for the new UI
    val presentCount: Int get() = daysPresent
    val absentCount: Int get() = totalClassDays - daysPresent
    val attendanceDetails: Map<String, AttendanceDetail> get() = attendanceData

    // Computed properties for UI display
    val presentCountForUI: Int get() = attendanceByDate.values.count { it }
    val absentCountForUI: Int get() = attendanceByDate.values.count { !it }
    val attendanceRateForUI: Int get() = if (attendanceByDate.isNotEmpty()) 
                                            (presentCountForUI * 100 / attendanceByDate.size)
                                          else 0

    data class AttendanceDetail(
        val date: String,
        val present: Boolean,
        val startTime: LocalTime? = null,
        val endTime: LocalTime? = null,
        val scheduleStartTime: LocalTime? = null,
        val scheduleEndTime: LocalTime? = null
    )
}
