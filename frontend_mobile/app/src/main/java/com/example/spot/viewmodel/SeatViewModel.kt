package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Seat
import com.example.spot.model.PickSeatRequest
import com.example.spot.repository.SeatRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for seat operations
 */
class SeatViewModel : ViewModel() {
    
    private val seatRepository = SeatRepository()
    
    // UI States
    private val _seatsState = MutableStateFlow<SeatsState>(SeatsState.Idle)
    val seatsState: StateFlow<SeatsState> = _seatsState
    
    private val _studentSeatState = MutableStateFlow<StudentSeatState>(StudentSeatState.Idle)
    val studentSeatState: StateFlow<StudentSeatState> = _studentSeatState
    
    private val _pickSeatState = MutableStateFlow<SeatPickState>(SeatPickState.Idle)
    val pickSeatState: StateFlow<SeatPickState> = _pickSeatState
    
    /**
     * Load all seats for the section
     */
    fun loadSeatsForSection(sectionId: Long) {
        _seatsState.value = SeatsState.Loading
        
        viewModelScope.launch {
            when (val result = seatRepository.getSeatsBySectionId(sectionId)) {
                is NetworkResult.Success -> {
                    _seatsState.value = SeatsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _seatsState.value = SeatsState.Error(result.message)
                }
                else -> {
                    _seatsState.value = SeatsState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Load the student's assigned seat in a section
     */
    fun loadStudentSeat(sectionId: Long) {
        _studentSeatState.value = StudentSeatState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _studentSeatState.value = StudentSeatState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            when (val result = seatRepository.getSeatByStudentAndSectionId(userId, sectionId)) {
                is NetworkResult.Success -> {
                    _studentSeatState.value = StudentSeatState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _studentSeatState.value = StudentSeatState.Error(result.message)
                }
                else -> {
                    _studentSeatState.value = StudentSeatState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Pick a seat in a section
     */
    fun pickSeat(sectionId: Long, row: Int, column: Int) {
        _pickSeatState.value = SeatPickState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _pickSeatState.value = SeatPickState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            // Create PickSeatRequest object
            val pickSeatRequest = PickSeatRequest(
                studentId = userId,
                sectionId = sectionId,
                row = row,
                column = column
            )
            
            when (val result = seatRepository.pickSeat(pickSeatRequest)) {
                is NetworkResult.Success -> {
                    _pickSeatState.value = SeatPickState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _pickSeatState.value = SeatPickState.Error(result.message)
                }
                else -> {
                    _pickSeatState.value = SeatPickState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Reset all states to Idle
     */
    fun resetStates() {
        _seatsState.value = SeatsState.Idle
        _studentSeatState.value = StudentSeatState.Idle
        _pickSeatState.value = SeatPickState.Idle
    }
}

// States for Seat UI
sealed class SeatsState {
    object Idle : SeatsState()
    object Loading : SeatsState()
    data class Success(val seats: List<Seat>) : SeatsState()
    data class Error(val message: String) : SeatsState()
}

sealed class StudentSeatState {
    object Idle : StudentSeatState()
    object Loading : StudentSeatState()
    data class Success(val seat: Seat) : StudentSeatState()
    data class Error(val message: String) : StudentSeatState()
}

sealed class SeatPickState {
    object Idle : SeatPickState()
    object Loading : SeatPickState()
    data class Success(val seat: Seat) : SeatPickState()
    data class Error(val message: String) : SeatPickState()
}
