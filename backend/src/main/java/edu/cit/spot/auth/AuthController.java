package edu.cit.spot.auth;

import edu.cit.spot.entity.User;
import edu.cit.spot.entity.UserRole;
import edu.cit.spot.security.JwtTokenProvider;
import edu.cit.spot.service.GoogleAuthService;
import edu.cit.spot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final GoogleAuthService googleAuthService;

    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtTokenProvider tokenProvider,
                         GoogleAuthService googleAuthService) {
        log.info("=== AuthController initialized ===\n" +
                "authenticationManager: {}\n" +
                "userService: {}\n" +
                "tokenProvider: {}\n" +
                "googleAuthService: {}",
                authenticationManager.getClass().getName(),
                userService.getClass().getName(),
                tokenProvider.getClass().getName(),
                googleAuthService.getClass().getName());

        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.googleAuthService = googleAuthService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully registered user", 
                     content = @Content(schema = @Schema(implementation = SignupResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Parameter(description = "User registration details") @Valid @RequestBody SignupRequest request, 
            HttpServletRequest httpRequest) {
        log.info("=== Signup Request Details ===");
        log.info("Request URI: {}", httpRequest.getRequestURI());
        log.info("Method: {}", httpRequest.getMethod());
        log.info("Headers:");
        Collections.list(httpRequest.getHeaderNames())
            .forEach(headerName -> log.info("{}: {}", headerName, httpRequest.getHeader(headerName)));
        log.info("Request Body: {}", request);
        log.info("==========================");
        // Create the user in the database
        User user = userService.createUser(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            UserRole.valueOf(request.getRole().toUpperCase()),
            request.getPlatformType()
        );
        
        // Generate JWT token for the newly created user
        String token = tokenProvider.generateToken(user);
        
        log.info("User registered successfully: {}, generating token", user.getEmail());
        
        // Return complete response with token and user details
        return ResponseEntity.ok(new SignupResponse(
            "User registered successfully", 
            user.getEmail(),
            token,
            user
        ));
    }

    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated", 
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login credentials") @Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "Authenticate with Google", description = "Authenticates a user using Google OAuth and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated with Google", 
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid Google token")
    })
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(
            @Parameter(description = "Google authentication token") @Valid @RequestBody GoogleAuthRequest request) {
        User user = googleAuthService.authenticateGoogleToken(request.getIdToken(), request.getPlatformType());
        String token = tokenProvider.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "Get user profile", description = "Returns the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "API Test", description = "Simple endpoint to test if the auth controller is working")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auth controller is working")
    })
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Auth controller is working!");
    }

    @Operation(summary = "Health check", description = "Check if the authentication service is healthy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check endpoint called");
        return ResponseEntity.ok("OK");
    }
}
