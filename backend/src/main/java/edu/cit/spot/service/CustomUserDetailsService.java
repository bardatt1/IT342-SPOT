package edu.cit.spot.service;

import edu.cit.spot.entity.Admin;
import edu.cit.spot.entity.Student;
import edu.cit.spot.entity.Teacher;
import edu.cit.spot.repository.AdminRepository;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find user in each repository
        Student student = studentRepository.findByEmail(email).orElse(null);
        if (student != null) {
            return createUserDetails(email, student.getPassword(), "ROLE_STUDENT");
        }

        Teacher teacher = teacherRepository.findByEmail(email).orElse(null);
        if (teacher != null) {
            return createUserDetails(email, teacher.getPassword(), "ROLE_TEACHER");
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        if (admin.isSystemAdmin()) {
            return createUserDetails(email, admin.getPassword(), "ROLE_SYSTEMADMIN");
        } else {
            return createUserDetails(email, admin.getPassword(), "ROLE_ADMIN");
        }
    }

    private UserDetails createUserDetails(String username, String password, String role) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        return new User(username, password, authorities);
    }
}
