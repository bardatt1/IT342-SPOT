package edu.cit.spot.controller;

import edu.cit.spot.entity.User;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Operations for managing user accounts")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Returns all users (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        log.info("Retrieved all users, count: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID", description = "Returns a specific user by ID (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        log.info("Retrieved user with ID: {}", id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get current user profile", description = "Returns the profile of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        log.info("Retrieved profile for user: {}", user.getEmail());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user profile", description = "Updates the profile of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated user profile"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(@RequestBody User userRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        
        // Only allow updating certain fields (not role or other sensitive data)
        currentUser.setFirstName(userRequest.getFirstName());
        currentUser.setLastName(userRequest.getLastName());
        currentUser.setPhoneNumber(userRequest.getPhoneNumber());
        
        User updatedUser = userService.updateUser(currentUser);
        log.info("Updated profile for user: {}", updatedUser.getEmail());
        
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Change password", description = "Changes the password for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully changed password"),
        @ApiResponse(responseCode = "400", description = "Invalid input or incorrect current password"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        boolean success = userService.changePassword(email, currentPassword, newPassword);
        
        if (success) {
            log.info("Changed password for user: {}", email);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Failed to change password for user: {} - incorrect current password", email);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update user role", description = "Updates the role of a user (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated user role"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update user roles"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        
        User updatedUser = userService.updateUserRole(id, role);
        log.info("Updated role to {} for user with ID: {}", role, id);
        
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Deactivate user account", description = "Deactivates a user account (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deactivated user"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to deactivate users"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        log.info("Deactivated user with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate user account", description = "Activates a deactivated user account (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully activated user"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to activate users"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        log.info("Activated user with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user statistics", description = "Returns statistics about users (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user statistics"),
        @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalUsers = userService.getAllUsers().size();
        int activeUsers = userService.getActiveUsers().size();
        int teacherCount = userService.getUsersByRole("TEACHER").size();
        int studentCount = userService.getUsersByRole("STUDENT").size();
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("teacherCount", teacherCount);
        stats.put("studentCount", studentCount);
        
        log.info("Retrieved user statistics");
        
        return ResponseEntity.ok(stats);
    }
}
