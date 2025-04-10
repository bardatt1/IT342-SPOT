package edu.cit.spot.service;

import edu.cit.spot.entity.Notification;
import edu.cit.spot.entity.User;

import java.util.List;

public interface NotificationService {
    // Original methods
    List<Notification> getAllNotifications();
    Notification getNotificationById(Long id);
    List<Notification> getNotificationsByUser(User user);
    List<Notification> getUnreadNotificationsByUser(User user);
    long getUnreadNotificationCount(User user);
    Notification createNotification(Notification notification);
    Notification markAsRead(Long id);
    void markAllAsRead(User user);
    void deleteNotification(Long id);
    
    // New methods needed by controllers
    List<Notification> getNotificationsByUserId(Long userId);
    int getUnreadNotificationCount(Long userId);
    void markAllAsRead(Long userId);
}
