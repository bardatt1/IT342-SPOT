package edu.cit.spot.service;

import edu.cit.spot.entity.User;
import edu.cit.spot.entity.UserRole;
import edu.cit.spot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User createUser(String email, String password, String firstName, String lastName, 
                         UserRole role, String platformType) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setPlatformType(platformType);

        return userRepository.save(user);
    }

    public User createGoogleUser(String email, String firstName, String lastName, 
                               String googleId, String pictureUrl, UserRole role, String platformType) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseGet(User::new));

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setGoogleId(googleId);
        user.setProfilePicture(pictureUrl);
        user.setRole(role);
        user.setPlatformType(platformType);
        user.setPassword(passwordEncoder.encode(googleId)); // Use googleId as password for Google users

        return userRepository.save(user);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
