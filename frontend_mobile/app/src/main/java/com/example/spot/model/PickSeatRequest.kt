package com.example.spot.model

/**
 * Request model for picking a seat in a section
 */
data class PickSeatRequest(
    val studentId: Long,
    val sectionId: Long,
    val row: Int,
    val column: Int
)
