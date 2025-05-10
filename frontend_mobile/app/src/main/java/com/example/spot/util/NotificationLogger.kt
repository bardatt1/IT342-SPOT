package com.example.spot.util

import android.content.Context
import com.example.spot.model.Notification
import com.example.spot.model.NotificationType
import com.example.spot.model.Section
import com.example.spot.model.Student
import com.example.spot.repository.NotificationRepository

/**
 * Utility class to log notifications and activities across the application
 */
object NotificationLogger {
    private lateinit var repository: NotificationRepository
    
    /**
     * Initialize the logger with application context
     */
    fun init(context: Context) {
        repository = NotificationRepository(context)
    }
    
    /**
     * Log seat plan selection or change
     */
    fun logSeatSelection(sectionName: String, row: Int, column: Int, sectionId: Long) {
        repository.addNotification(
            title = "Seat Plan",
            message = "Selected seat at row $row, column $column in $sectionName",
            type = NotificationType.SEAT_PLAN,
            relatedEntityId = sectionId
        )
    }
    
    /**
     * Log attendance recording
     */
    fun logAttendanceRecorded(sectionName: String, date: String, sectionId: Long) {
        repository.addNotification(
            title = "Attendance",
            message = "Attendance recorded for $sectionName on $date",
            type = NotificationType.ATTENDANCE, 
            relatedEntityId = sectionId
        )
    }
    
    /**
     * Log successful enrollment to a course section
     */
    fun logEnrollment(courseName: String, sectionName: String, sectionId: Long) {
        repository.addNotification(
            title = "Enrollment",
            message = "Successfully enrolled in $courseName - $sectionName",
            type = NotificationType.ENROLLMENT,
            relatedEntityId = sectionId
        )
    }
    
    /**
     * Log profile update
     */
    fun logProfileUpdate(fieldName: String, studentId: Long? = null) {
        repository.addNotification(
            title = "Profile Update",
            message = "Updated profile information: $fieldName",
            type = NotificationType.PROFILE_UPDATE,
            relatedEntityId = studentId
        )
    }
    
    /**
     * Log password change
     */
    fun logPasswordChange(studentId: Long? = null) {
        repository.addNotification(
            title = "Profile Update",
            message = "Password changed successfully",
            type = NotificationType.PROFILE_UPDATE,
            relatedEntityId = studentId
        )
    }
    
    /**
     * Log course information
     */
    fun logCourseInfo(courseName: String, message: String, courseId: Long) {
        repository.addNotification(
            title = "Course",
            message = "$courseName: $message",
            type = NotificationType.COURSE,
            relatedEntityId = courseId
        )
    }
    
    /**
     * Log section information
     */
    fun logSectionInfo(sectionName: String, message: String, sectionId: Long) {
        repository.addNotification(
            title = "Section",
            message = "$sectionName: $message",
            type = NotificationType.SECTION,
            relatedEntityId = sectionId
        )
    }
    
    /**
     * Log schedule information
     */
    fun logScheduleChange(sectionName: String, message: String, sectionId: Long) {
        repository.addNotification(
            title = "Schedule",
            message = "$sectionName: $message",
            type = NotificationType.SCHEDULE,
            relatedEntityId = sectionId
        )
    }
    
    /**
     * Log QR scan for attendance
     */
    fun logQrScan(sectionName: String, sectionId: Long) {
        repository.addNotification(
            title = "Attendance",
            message = "Scanned QR code for $sectionName attendance",
            type = NotificationType.ATTENDANCE,
            relatedEntityId = sectionId
        )
    }
    
    /**
     * Log system notification
     */
    fun logSystemNotification(message: String) {
        repository.addNotification(
            title = "System",
            message = message,
            type = NotificationType.SYSTEM
        )
    }
}
