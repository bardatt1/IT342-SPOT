package com.example.spot.model

/**
 * Error types for enrollment to provide more specific feedback
 */
enum class EnrollErrorType {
    GENERAL,                  // General error
    DUPLICATE_SECTION,       // Already enrolled in the same section
    DUPLICATE_COURSE,        // Already enrolled in a different section of the same course
    INVALID_KEY,             // Invalid enrollment key
    CLOSED_ENROLLMENT,       // Section not open for enrollment
    NETWORK_ERROR            // Network or server error
}
