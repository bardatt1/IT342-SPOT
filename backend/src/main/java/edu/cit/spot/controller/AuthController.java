package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.auth.JwtResponse;
import edu.cit.spot.dto.auth.LoginRequest;
import edu.cit.spot.dto.auth.StudentIdLoginRequest;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for authentication operations")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Login with email and password")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Login successful", jwtResponse));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/login/student-id")
    @Operation(summary = "Student ID login", description = "Login with physical student ID and password")
    public ResponseEntity<ApiResponse<JwtResponse>> loginWithStudentId(@Valid @RequestBody StudentIdLoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateByStudentId(loginRequest);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Login successful", jwtResponse));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/bind-oauth")
    @Operation(summary = "Bind Google account", description = "Bind a Google account to existing user account")
    public ResponseEntity<ApiResponse<Boolean>> bindGoogleAccount(
            @RequestParam String email, 
            @RequestParam String googleId) {
        try {
            boolean result = authService.bindGoogleAccount(email, googleId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Google account bound successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 login success", description = "Callback endpoint for successful OAuth2 login")
    public ResponseEntity<ApiResponse<JwtResponse>> handleOAuth2LoginSuccess(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam String registrationType) {
        try {
            JwtResponse jwtResponse = authService.handleOAuth2Authentication(oauth2User, registrationType);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "OAuth login successful", jwtResponse));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/oauth2/failure")
    @Operation(summary = "OAuth2 login failure", description = "Callback endpoint for failed OAuth2 login")
    public ResponseEntity<ApiResponse<Void>> handleOAuth2LoginFailure() {
        return GlobalExceptionHandler.errorResponseEntity("OAuth login failed", HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check if email is in use", description = "Check if an email is already registered")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailInUse(@RequestParam String email) {
        try {
            boolean isInUse = authService.isEmailInUse(email);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Email check completed", isInUse));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
