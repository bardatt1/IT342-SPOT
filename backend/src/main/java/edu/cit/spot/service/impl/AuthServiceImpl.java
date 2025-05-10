package edu.cit.spot.service.impl;

import edu.cit.spot.dto.auth.JwtResponse;
import edu.cit.spot.dto.auth.LoginRequest;
import edu.cit.spot.dto.auth.StudentIdLoginRequest;
import edu.cit.spot.entity.Admin;
import edu.cit.spot.entity.Student;
import edu.cit.spot.entity.Teacher;
import edu.cit.spot.repository.AdminRepository;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.repository.TeacherRepository;
import edu.cit.spot.security.JwtTokenProvider;
import edu.cit.spot.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.email(),
                loginRequest.password()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Determine user type and fetch relevant info
        Student student = studentRepository.findByEmail(loginRequest.email()).orElse(null);
        if (student != null) {
            return new JwtResponse(
                jwt,
                "Bearer",
                "STUDENT",
                student.getId(),
                student.getEmail(),
                student.getFirstName() + " " + student.getLastName(),
                student.isGoogleLinked()
            );
        }
        
        Teacher teacher = teacherRepository.findByEmail(loginRequest.email()).orElse(null);
        if (teacher != null) {
            return new JwtResponse(
                jwt,
                "Bearer",
                "TEACHER",
                teacher.getId(),
                teacher.getEmail(),
                teacher.getFirstName() + " " + teacher.getLastName(),
                teacher.isGoogleLinked()
            );
        }
        
        Admin admin = adminRepository.findByEmail(loginRequest.email()).orElseThrow(
            () -> new IllegalArgumentException("User not found with email: " + loginRequest.email())
        );
        
        // Check if the admin is a system admin and set the role accordingly
        String role = admin.isSystemAdmin() ? "SYSTEMADMIN" : "ADMIN";
        
        return new JwtResponse(
            jwt,
            "Bearer",
            role,
            admin.getId(),
            admin.getEmail(),
            admin.getFirstName() + " " + admin.getLastName(),
            false
        );
    }

    @Override
    public JwtResponse authenticateByStudentId(StudentIdLoginRequest loginRequest) {
        // Find student by physical ID
        Student student = studentRepository.findByStudentPhysicalId(loginRequest.getStudentPhysicalId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + loginRequest.getStudentPhysicalId()));
        
        // Use the found student's email for authentication
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                student.getEmail(),
                loginRequest.getPassword()
            )
        );
    
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return new JwtResponse(
            jwt,
            "Bearer",
            "STUDENT",
            student.getId(),
            student.getEmail(),
            student.getFirstName() + " " + student.getLastName(),
            student.isGoogleLinked()
        );
    }
    
    @Override
    @Transactional
    public JwtResponse handleOAuth2Authentication(OAuth2User oAuth2User, String registrationType) {
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        
        if (email == null || googleId == null) {
            throw new IllegalArgumentException("Email or Google ID is missing from OAuth2 attributes");
        }
        
        switch (registrationType.toUpperCase()) {
            case "STUDENT":
                return handleStudentOAuth(email, googleId);
            case "TEACHER":
                return handleTeacherOAuth(email, googleId);
            default:
                throw new IllegalArgumentException("Invalid registration type: " + registrationType);
        }
    }
    
    private JwtResponse handleStudentOAuth(String email, String googleId) {
        Student student = studentRepository.findByEmail(email).orElse(null);
        
        if (student == null) {
            student = studentRepository.findByGoogleId(googleId).orElseThrow(
                () -> new IllegalArgumentException("No student account found to link with Google account")
            );
        }
        
        // Update Google ID if not already set
        if (student.getGoogleId() == null || !student.getGoogleId().equals(googleId)) {
            student.setGoogleId(googleId);
            student.setGoogleLinked(true);
            studentRepository.save(student);
        }
        
        // Create authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            email, "", java.util.Collections.emptyList()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return new JwtResponse(
            jwt,
            "Bearer",
            "STUDENT",
            student.getId(),
            student.getEmail(),
            student.getFirstName() + " " + student.getLastName(),
            true
        );
    }
    
    private JwtResponse handleTeacherOAuth(String email, String googleId) {
        Teacher teacher = teacherRepository.findByEmail(email).orElse(null);
        
        if (teacher == null) {
            teacher = teacherRepository.findByGoogleId(googleId).orElseThrow(
                () -> new IllegalArgumentException("No teacher account found to link with Google account")
            );
        }
        
        // Update Google ID if not already set
        if (teacher.getGoogleId() == null || !teacher.getGoogleId().equals(googleId)) {
            teacher.setGoogleId(googleId);
            teacher.setGoogleLinked(true);
            teacherRepository.save(teacher);
        }
        
        // Create authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            email, "", java.util.Collections.emptyList()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return new JwtResponse(
            jwt,
            "Bearer",
            "TEACHER",
            teacher.getId(),
            teacher.getEmail(),
            teacher.getFirstName() + " " + teacher.getLastName(),
            true
        );
    }

    @Override
    @Transactional
    public boolean bindGoogleAccount(String email, String googleId) {
        Student student = studentRepository.findByEmail(email).orElse(null);
        if (student != null) {
            student.setGoogleId(googleId);
            student.setGoogleLinked(true);
            studentRepository.save(student);
            return true;
        }
        
        Teacher teacher = teacherRepository.findByEmail(email).orElse(null);
        if (teacher != null) {
            teacher.setGoogleId(googleId);
            teacher.setGoogleLinked(true);
            teacherRepository.save(teacher);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean isEmailInUse(String email) {
        return studentRepository.existsByEmail(email) || 
               teacherRepository.existsByEmail(email) || 
               adminRepository.existsByEmail(email);
    }
}
