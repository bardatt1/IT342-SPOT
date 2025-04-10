package edu.cit.spot.config;

import edu.cit.spot.security.JwtAuthenticationFilter;
import edu.cit.spot.security.JwtTokenProvider;
import edu.cit.spot.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Separate configuration class for authentication-related beans.
 * This helps break the circular dependency between SecurityConfig and UserService.
 */
@Configuration
public class AuthenticationConfig {

    /**
     * Creates the JWT authentication filter bean.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, (UserService) userDetailsService);
    }

    /**
     * Creates the authentication provider bean.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
