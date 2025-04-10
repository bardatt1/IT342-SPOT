package edu.cit.spot.config;

import org.springframework.context.annotation.Configuration;

/**
 * IMPORTANT: This class has been deprecated.
 * Its functionality has been consolidated into SecurityConfig to avoid
 * configuration conflicts and duplicate security filter chains.
 * 
 * This class is kept as a placeholder to maintain backward compatibility
 * but does not contribute any beans to the application context.
 */
@Configuration
public class WebSecurityConfig {
    // All security configuration is now in SecurityConfig
}
