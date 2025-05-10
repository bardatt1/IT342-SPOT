package com.example.spot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Seat
import com.example.spot.model.SeatCoordinate
import com.example.spot.model.Section
import com.example.spot.model.Student
import com.example.spot.model.PickSeatRequest
import com.example.spot.repository.EnrollmentRepository
import com.example.spot.repository.SeatRepository
import com.example.spot.repository.ScheduleRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import com.example.spot.util.NotificationLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for seat plan operations following MVI pattern
 */
class SeatPlanViewModel : ViewModel() {
    
    private val seatRepository = SeatRepository()
    private val enrollmentRepository = EnrollmentRepository()
    private val scheduleRepository = ScheduleRepository()
    private val TAG = "SeatPlanViewModel"
    
    // UI States
    private val _sectionState = MutableStateFlow<Section?>(null)
    val sectionState: StateFlow<Section?> = _sectionState
    
    private val _seatsState = MutableStateFlow<SeatPlanState>(SeatPlanState.Idle)
    val seatsState: StateFlow<SeatPlanState> = _seatsState
    
    private val _selectedSeatState = MutableStateFlow<SeatCoordinate?>(null)
    val selectedSeatState: StateFlow<SeatCoordinate?> = _selectedSeatState
    
    private val _studentSeatState = MutableStateFlow<Seat?>(null)
    val studentSeatState: StateFlow<Seat?> = _studentSeatState
    
    private val _pickSeatState = MutableStateFlow<SeatPlanPickState>(SeatPlanPickState.Idle)
    val pickSeatState: StateFlow<SeatPlanPickState> = _pickSeatState
    
    private val _userId = MutableStateFlow<Long?>(null)
    val userId: StateFlow<Long?> = _userId
    
    init {
        // Initialize userId when ViewModel is created
        viewModelScope.launch {
            _userId.value = TokenManager.getUserId().first()
        }
    }
    
    /**
     * Set the current section
     */
    fun setSection(section: Section) {
        viewModelScope.launch {
            Log.d(TAG, "Setting section with ID: ${section.id}, original schedule: ${section.schedule}")
            // First fetch schedule information for this section and enhance it
            val enhancedSection = fetchScheduleForSection(section)
            Log.d(TAG, "Setting enhanced section with schedule: ${enhancedSection.schedule}")
            _sectionState.value = enhancedSection
            loadSeatPlan(enhancedSection.id)
        }
    }
    
    /**
     * Fetch schedules for a section and update the section object with formatted schedule
     */
    private suspend fun fetchScheduleForSection(section: Section): Section {
        try {
            val scheduleResult = scheduleRepository.getSchedulesBySectionId(section.id)
            
            if (scheduleResult is NetworkResult.Success && scheduleResult.data.isNotEmpty()) {
                val schedules = scheduleResult.data
                Log.d(TAG, "Found ${schedules.size} schedules for section ${section.id}")
                
                // For debugging
                schedules.forEach { schedule ->
                    Log.d(TAG, "Schedule: day=${schedule.dayOfWeek}, time=${schedule.timeStart}-${schedule.timeEnd}, room=${schedule.room}")
                }
                
                val formattedSchedule = formatSchedulesToString(schedules)
                Log.d(TAG, "Formatted schedule: $formattedSchedule")
                
                // Create a brand new section with formatted schedule
                return Section(
                    id = section.id,
                    course = section.course,
                    teacher = section.teacher,
                    sectionName = section.sectionName,
                    enrollmentKey = section.enrollmentKey,
                    enrollmentOpen = section.enrollmentOpen,
                    enrollmentCount = section.enrollmentCount,
                    schedule = formattedSchedule
                )
            } else {
                if (scheduleResult is NetworkResult.Error) {
                    Log.e(TAG, "Error fetching schedules: ${scheduleResult.message}")
                } else {
                    Log.d(TAG, "No schedules found for section ${section.id}")
                }
                return section
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing section: ${e.message}", e)
            return section
        }
    }
    
    /**
     * Format a list of schedules into a readable string
     */
    private fun formatSchedulesToString(schedules: List<com.example.spot.model.Schedule>): String {
        if (schedules.isEmpty()) return ""
        
        return schedules.joinToString(", ") { schedule ->
            val day = when (schedule.dayOfWeek) {
                1 -> "Mon"
                2 -> "Tue"
                3 -> "Wed"
                4 -> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                7 -> "Sun"
                else -> "Unknown"
            }
            
            val startTime = formatTime(schedule.timeStart)
            val endTime = formatTime(schedule.timeEnd)
            val room = schedule.room
            val type = schedule.scheduleType
            
            "$day ${startTime}-${endTime} | ${room} (${type})"
        }
    }
    
    /**
     * Format time from 24-hour format (HH:MM:SS) to 12-hour format (h:MMa)
     */
    private fun formatTime(timeString: String): String {
        try {
            // Extract hours and minutes from the time string
            val timeParts = timeString.split(":")
            if (timeParts.size < 2) return timeString
            
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            val amPm = if (hour < 12) "AM" else "PM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            
            // Format minutes with leading zero if needed
            val minuteStr = if (minute < 10) "0$minute" else minute.toString()
            
            return "${hour12}:${minuteStr}${amPm}"
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: $timeString", e)
            return timeString
        }
    }
    
    /**
     * Load all seats for the section
     */
    fun loadSeatPlan(sectionId: Long) {
        _seatsState.value = SeatPlanState.Loading
        
        viewModelScope.launch {
            try {
                // First check if student is enrolled in the section
                val userId = TokenManager.getUserId().first() ?: return@launch
                
                // Check enrollment status first
                val enrollmentsResult = enrollmentRepository.getEnrollmentsByStudentId(userId)
                
                if (enrollmentsResult is NetworkResult.Success && enrollmentsResult.data.isNotEmpty()) {
                    val enrollments = enrollmentsResult.data
                    Log.d(TAG, "Found ${enrollments.size} enrollments for student $userId")
                    
                    if (enrollments.any { it.section.id == sectionId }) {
                        // Try to use new dedicated endpoint first to get all seats
                        when (val allSeatsResult = seatRepository.getAllSeatsForSection(sectionId)) {
                            is NetworkResult.Success -> {
                                _seatsState.value = SeatPlanState.Success(allSeatsResult.data)
                                loadStudentSeat(sectionId)
                            }
                            is NetworkResult.Error -> {
                                // Fall back to original endpoint if the new one fails
                                when (val result = seatRepository.getSeatsBySectionId(sectionId)) {
                                    is NetworkResult.Success -> {
                                        _seatsState.value = SeatPlanState.Success(result.data)
                                        loadStudentSeat(sectionId)
                                    }
                                    is NetworkResult.Error -> {
                                        _seatsState.value = SeatPlanState.Error(result.message)
                                    }
                                    else -> {
                                        _seatsState.value = SeatPlanState.Error("Unknown error occurred")
                                    }
                                }
                            }
                            else -> {
                                _seatsState.value = SeatPlanState.Error("Unknown error occurred")
                            }
                        }
                    } else {
                        _seatsState.value = SeatPlanState.Error("You are not enrolled in this section. Please enroll before viewing the seat plan.")
                    }
                } else if (enrollmentsResult is NetworkResult.Error) {
                    _seatsState.value = SeatPlanState.Error(enrollmentsResult.message)
                } else {
                    _seatsState.value = SeatPlanState.Error("Unknown enrollment error occurred")
                }
            } catch (e: Exception) {
                _seatsState.value = SeatPlanState.Error(e.message ?: "An unexpected error occurred")
                Log.e(TAG, "Error loading seat plan: ${e.message}", e)
            }
        }
    }
    
    /**
     * Load the current student's seat
     */
    private fun loadStudentSeat(sectionId: Long) {
        viewModelScope.launch {
            try {
                val userId = TokenManager.getUserId().first() ?: return@launch
                val result = seatRepository.getSeatByStudentAndSectionId(userId, sectionId)
                
                if (result is NetworkResult.Success) {
                    _studentSeatState.value = result.data
                    // If student already has a seat, pre-select it in the UI
                    _selectedSeatState.value = SeatCoordinate(result.data.row, result.data.column)
                } else {
                    _studentSeatState.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading student seat: ${e.message}", e)
            }
        }
    }
    
    /**
     * Select a seat temporarily (just UI state, not submitted yet)
     */
    fun selectSeat(row: Int, column: Int) {
        _selectedSeatState.value = SeatCoordinate(row, column)
    }
    
    /**
     * Clear the seat selection
     */
    fun clearSeatSelection() {
        val currentStudentSeat = _studentSeatState.value
        if (currentStudentSeat != null) {
            // If student already has a seat, reset to that
            _selectedSeatState.value = SeatCoordinate(currentStudentSeat.row, currentStudentSeat.column)
        } else {
            // Otherwise, clear the selection
            _selectedSeatState.value = null
        }
    }
    
    /**
     * Submit the selected seat to the backend
     */
    fun submitSeatSelection() {
        val selectedSeat = _selectedSeatState.value ?: return
        val section = _sectionState.value ?: return
        
        _pickSeatState.value = SeatPlanPickState.Loading
        
        viewModelScope.launch {
            try {
                val userId = TokenManager.getUserId().first() ?: return@launch
                
                val pickSeatRequest = PickSeatRequest(
                    studentId = userId,
                    sectionId = section.id,
                    row = selectedSeat.row,
                    column = selectedSeat.column
                )
                
                when (val result = seatRepository.pickSeat(pickSeatRequest)) {
                    is NetworkResult.Success -> {
                        _studentSeatState.value = result.data
                        _pickSeatState.value = SeatPlanPickState.Success(result.data)
                        
                        // Log the seat selection activity
                        val sectionName = section.sectionName ?: section.course.courseName
                        NotificationLogger.logSeatSelection(
                            sectionName = sectionName,
                            row = selectedSeat.row + 1, // Fix: add 1 to the row
                            column = selectedSeat.column + 1, // Fix: add 1 to the column
                            sectionId = section.id
                        )
                        
                        // Refresh the seat plan
                        loadSeatPlan(section.id)
                    }
                    is NetworkResult.Error -> {
                        _pickSeatState.value = SeatPlanPickState.Error(result.message)
                    }
                    else -> {
                        _pickSeatState.value = SeatPlanPickState.Error("Unknown error occurred")
                    }
                }
            } catch (e: Exception) {
                _pickSeatState.value = SeatPlanPickState.Error(e.message ?: "An unexpected error occurred")
                Log.e(TAG, "Error submitting seat selection: ${e.message}", e)
            }
        }
    }
    
    /**
     * Reset the pick seat state (e.g., after handling error or success)
     */
    fun resetPickSeatState() {
        _pickSeatState.value = SeatPlanPickState.Idle
    }
    
    /**
     * Reset all states to their initial values
     */
    fun resetStates() {
        _seatsState.value = SeatPlanState.Idle
        _selectedSeatState.value = null
        _studentSeatState.value = null
        _pickSeatState.value = SeatPlanPickState.Idle
    }
}

/**
 * States for Seat Plan UI
 */
sealed class SeatPlanState {
    object Idle : SeatPlanState()
    object Loading : SeatPlanState()
    data class Success(val seats: List<Seat>) : SeatPlanState()
    data class Error(val message: String) : SeatPlanState()
}

/**
 * States for Pick Seat process
 */
sealed class SeatPlanPickState {
    object Idle : SeatPlanPickState()
    object Loading : SeatPlanPickState()
    data class Success(val seat: Seat) : SeatPlanPickState()
    data class Error(val message: String) : SeatPlanPickState()
}
