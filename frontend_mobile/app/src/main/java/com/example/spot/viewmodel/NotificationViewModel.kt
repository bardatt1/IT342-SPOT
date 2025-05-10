package com.example.spot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Notification
import com.example.spot.model.NotificationType
import com.example.spot.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel for managing notifications and activity logs
 */
class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NotificationRepository = NotificationRepository(application.applicationContext)
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    init {
        // Collect notifications from repository
        viewModelScope.launch {
            repository.notificationsFlow.collect { notificationsList ->
                _notifications.value = notificationsList
                _unreadCount.value = notificationsList.count { !it.isRead }
            }
        }
    }
    
    /**
     * Log a seat plan related activity
     */
    fun logSeatPlanActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Seat Plan",
            message = message,
            type = NotificationType.SEAT_PLAN,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log an attendance related activity
     */
    fun logAttendanceActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Attendance",
            message = message,
            type = NotificationType.ATTENDANCE,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log an enrollment related activity
     */
    fun logEnrollmentActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Enrollment",
            message = message,
            type = NotificationType.ENROLLMENT,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log a profile update activity
     */
    fun logProfileUpdateActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Profile Update",
            message = message,
            type = NotificationType.PROFILE_UPDATE,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log a course related activity
     */
    fun logCourseActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Course",
            message = message,
            type = NotificationType.COURSE,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log a section related activity
     */
    fun logSectionActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Section",
            message = message,
            type = NotificationType.SECTION,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log a schedule related activity
     */
    fun logScheduleActivity(message: String, entityId: Long? = null) {
        repository.addNotification(
            title = "Schedule",
            message = message,
            type = NotificationType.SCHEDULE,
            relatedEntityId = entityId
        )
    }
    
    /**
     * Log a system notification
     */
    fun logSystemNotification(message: String) {
        repository.addNotification(
            title = "System",
            message = message,
            type = NotificationType.SYSTEM
        )
    }
    
    /**
     * Mark a notification as read
     */
    fun markAsRead(notificationId: Long) {
        repository.markAsRead(notificationId)
    }
    
    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        repository.markAllAsRead()
    }
    
    /**
     * Delete a notification
     */
    fun deleteNotification(notificationId: Long) {
        repository.deleteNotification(notificationId)
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        repository.clearAllNotifications()
    }
}
