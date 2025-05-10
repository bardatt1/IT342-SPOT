package com.example.spot.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.spot.model.Notification
import com.example.spot.model.NotificationType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository for managing notifications and activity logs for the app
 */
class NotificationRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        NOTIFICATION_PREFS, Context.MODE_PRIVATE
    )
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .create()
    
    private val _notificationsFlow = MutableStateFlow<List<Notification>>(emptyList())
    val notificationsFlow: Flow<List<Notification>> = _notificationsFlow.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    /**
     * Load notifications from SharedPreferences
     */
    private fun loadNotifications() {
        val notificationsJson = sharedPreferences.getString(NOTIFICATIONS_KEY, null)
        if (notificationsJson != null) {
            val type = object : TypeToken<List<Notification>>() {}.type
            val notifications = gson.fromJson<List<Notification>>(notificationsJson, type)
            _notificationsFlow.value = notifications.sortedByDescending { it.timestamp }
        }
    }
    
    /**
     * Save notifications to SharedPreferences
     */
    private fun saveNotifications(notifications: List<Notification>) {
        val notificationsJson = gson.toJson(notifications)
        sharedPreferences.edit().putString(NOTIFICATIONS_KEY, notificationsJson).apply()
        _notificationsFlow.value = notifications.sortedByDescending { it.timestamp }
    }
    
    /**
     * Add a new notification
     */
    fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        relatedEntityId: Long? = null
    ) {
        val currentNotifications = _notificationsFlow.value.toMutableList()
        val newNotification = Notification(
            id = System.currentTimeMillis(),
            title = title,
            message = message,
            type = type,
            relatedEntityId = relatedEntityId,
            timestamp = LocalDateTime.now(),
            isRead = false
        )
        
        currentNotifications.add(0, newNotification)
        saveNotifications(currentNotifications)
    }
    
    /**
     * Mark a notification as read
     */
    fun markAsRead(notificationId: Long) {
        val currentNotifications = _notificationsFlow.value.toMutableList()
        val notificationIndex = currentNotifications.indexOfFirst { it.id == notificationId }
        
        if (notificationIndex != -1) {
            val notification = currentNotifications[notificationIndex]
            currentNotifications[notificationIndex] = notification.copy(isRead = true)
            saveNotifications(currentNotifications)
        }
    }
    
    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        val currentNotifications = _notificationsFlow.value
        val updatedNotifications = currentNotifications.map { it.copy(isRead = true) }
        saveNotifications(updatedNotifications)
    }
    
    /**
     * Delete a notification
     */
    fun deleteNotification(notificationId: Long) {
        val currentNotifications = _notificationsFlow.value.toMutableList()
        currentNotifications.removeIf { it.id == notificationId }
        saveNotifications(currentNotifications)
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        saveNotifications(emptyList())
    }
    
    /**
     * Get unread notifications count
     */
    fun getUnreadCount(): Int {
        return _notificationsFlow.value.count { !it.isRead }
    }
    
    companion object {
        private const val NOTIFICATION_PREFS = "notification_preferences"
        private const val NOTIFICATIONS_KEY = "notifications"
    }
    
    /**
     * Custom type adapter for LocalDateTime serialization/deserialization
     */
    class LocalDateTimeTypeAdapter : com.google.gson.TypeAdapter<LocalDateTime>() {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        
        override fun write(out: com.google.gson.stream.JsonWriter, value: LocalDateTime?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(formatter.format(value))
            }
        }
        
        override fun read(reader: com.google.gson.stream.JsonReader): LocalDateTime? {
            val dateString = reader.nextString()
            return if (dateString == null) null else LocalDateTime.parse(dateString, formatter)
        }
    }
}
