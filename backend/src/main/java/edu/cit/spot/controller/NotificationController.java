package edu.cit.spot.controller;

import edu.cit.spot.entity.Notification;
import edu.cit.spot.entity.User;
import edu.cit.spot.service.NotificationService;
import edu.cit.spot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Operations for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "Get notifications for current user", description = "Returns all notifications for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        List<Notification> notifications = notificationService.getNotificationsByUserId(user.getId());
        log.info("Retrieved {} notifications for user {}", notifications.size(), user.getEmail());
        
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications count", description = "Returns the count of unread notifications for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved unread count"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        int unreadCount = notificationService.getUnreadNotificationCount(user.getId());
        log.info("Retrieved unread notification count for user {}: {}", user.getEmail(), unreadCount);
        
        return ResponseEntity.ok(unreadCount);
    }

    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully marked as read"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this notification")
    })
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        Notification notification = notificationService.getNotificationById(id);
        
        // Verify ownership
        if (!notification.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to mark another user's notification as read", user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        notificationService.markAsRead(id);
        log.info("Marked notification {} as read for user {}", id, user.getEmail());
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications for the authenticated user as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully marked all as read"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        notificationService.markAllAsRead(user.getId());
        log.info("Marked all notifications as read for user {}", user.getEmail());
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete notification", description = "Deletes a specific notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted notification"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this notification")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        Notification notification = notificationService.getNotificationById(id);
        
        // Verify ownership
        if (!notification.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to delete another user's notification", user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        notificationService.deleteNotification(id);
        log.info("Deleted notification {} for user {}", id, user.getEmail());
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create notification for a user", description = "Creates a new notification for a specific user (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created notification"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create notifications")
    })
    @PostMapping("/admin/create")
    public ResponseEntity<Notification> createNotification(
            @RequestParam Long userId,
            @RequestBody Notification notification) {
        
        User recipient = userService.getUserById(userId);
        notification.setUser(recipient);
        
        Notification createdNotification = notificationService.createNotification(notification);
        log.info("Created notification for user {}: {}", recipient.getEmail(), notification.getTitle());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
    }
}
