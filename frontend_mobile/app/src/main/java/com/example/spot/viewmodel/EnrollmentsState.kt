package com.example.spot.viewmodel

import com.example.spot.model.Enrollment

/**
 * States for Enrollments UI
 */
sealed class EnrollmentsState {
    object Idle : EnrollmentsState()
    object Loading : EnrollmentsState()
    object Empty : EnrollmentsState()
    data class Success(val enrollments: List<Enrollment>) : EnrollmentsState()
    data class Error(val message: String) : EnrollmentsState()
}
