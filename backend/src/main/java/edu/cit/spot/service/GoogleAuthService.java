package edu.cit.spot.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import edu.cit.spot.entity.User;
import edu.cit.spot.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.oauth2.client-id}")
    private String clientId;

    private final UserService userService;

    public User authenticateGoogleToken(String idTokenString, String platformType) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");

            // For Google sign-in, determine role based on email domain or platform
            UserRole role = determineUserRole(email, platformType);
            validatePlatformAccess(role, platformType);

            return userService.createGoogleUser(
                email, firstName, lastName, googleId, pictureUrl, role, platformType
            );

        } catch (Exception e) {
            throw new RuntimeException("Error authenticating Google token", e);
        }
    }

    private UserRole determineUserRole(String email, String platformType) {
        // If it's web platform, it's always a teacher
        if ("WEB".equalsIgnoreCase(platformType)) {
            return UserRole.TEACHER;
        }
        // For mobile, check email domain or let the client specify
        // You can add more sophisticated role determination logic here
        return UserRole.STUDENT;
    }

    private void validatePlatformAccess(UserRole role, String platformType) {
        if ("WEB".equalsIgnoreCase(platformType) && role != UserRole.TEACHER) {
            throw new RuntimeException("Only teachers can access the web platform");
        }
        if ("MOBILE".equalsIgnoreCase(platformType) && role == UserRole.TEACHER) {
            throw new RuntimeException("Teachers should use the web platform");
        }
    }
}
