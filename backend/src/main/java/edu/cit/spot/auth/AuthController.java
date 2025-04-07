package edu.cit.spot.auth;

import edu.cit.spot.entity.User;
import edu.cit.spot.entity.UserRole;
import edu.cit.spot.security.JwtTokenProvider;
import edu.cit.spot.service.GoogleAuthService;
import edu.cit.spot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request, HttpServletRequest httpRequest) {
        log.info("=== Signup Request Details ===");
        log.info("Request URI: {}", httpRequest.getRequestURI());
        log.info("Method: {}", httpRequest.getMethod());
        log.info("Headers:");
        Collections.list(httpRequest.getHeaderNames())
            .forEach(headerName -> log.info("{}: {}", headerName, httpRequest.getHeader(headerName)));
        log.info("Request Body: {}", request);
        log.info("==========================");
        User user = userService.createUser(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            UserRole.valueOf(request.getRole().toUpperCase()),
            request.getPlatformType()
        );

        return ResponseEntity.ok(new SignupResponse("User registered successfully", user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        User user = googleAuthService.authenticateGoogleToken(request.getIdToken(), request.getPlatformType());
        String token = tokenProvider.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Auth controller is working!");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check endpoint called");
        return ResponseEntity.ok("OK");
    }
}
