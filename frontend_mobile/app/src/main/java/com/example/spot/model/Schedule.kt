package com.example.spot.model

/**
 * Schedule data model matching the backend ScheduleDto
 */
data class Schedule(
    val id: Long,
    val sectionId: Long,
    val dayOfWeek: Int,
    val timeStart: String,
    val timeEnd: String,
    val scheduleType: String,
    val room: String
)
