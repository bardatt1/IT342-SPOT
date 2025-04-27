package com.example.spot.model

/**
 * Seat data model matching the backend SeatDto
 */
data class Seat(
    val id: Long,
    val sectionId: Long,
    val student: Student?,
    val column: Int,
    val row: Int
) {
    // Computed property to check if seat is taken
    val isTaken: Boolean
        get() = student != null
}
