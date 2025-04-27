package com.example.spot.model

/**
 * LocalTime data model matching the backend LocalTime structure
 */
data class LocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nano: Int
) {
    override fun toString(): String {
        val hourStr = hour.toString().padStart(2, '0')
        val minuteStr = minute.toString().padStart(2, '0')
        return "$hourStr:$minuteStr"
    }
}
