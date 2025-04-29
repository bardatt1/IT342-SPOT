package edu.cit.spot.service;

import edu.cit.spot.dto.auth.JwtResponse;
import edu.cit.spot.dto.auth.LoginRequest;
import edu.cit.spot.dto.auth.StudentIdLoginRequest;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    
    JwtResponse authenticateUser(LoginRequest loginRequest);
    
    JwtResponse handleOAuth2Authentication(OAuth2User oAuth2User, String registrationType);
    
    boolean bindGoogleAccount(String email, String googleId);
    
    boolean isEmailInUse(String email);

    JwtResponse authenticateByStudentId(StudentIdLoginRequest loginRequest);
}
