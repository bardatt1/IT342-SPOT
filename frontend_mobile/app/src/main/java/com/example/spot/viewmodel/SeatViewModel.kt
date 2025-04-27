package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Seat
import com.example.spot.repository.SeatRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for seat operations following MVI pattern
 */
class SeatViewModel : ViewModel() {
    
    private val seatRepository = SeatRepository()
    
    // UI States
    private val _seatsState = MutableStateFlow<SeatsState>(SeatsState.Idle)
    val seatsState: StateFlow<SeatsState> = _seatsState
    
    private val _studentSeatState = MutableStateFlow<StudentSeatState>(StudentSeatState.Idle)
    val studentSeatState: StateFlow<StudentSeatState> = _studentSeatState
    
    private val _pickSeatState = MutableStateFlow<PickSeatState>(PickSeatState.Idle)
    val pickSeatState: StateFlow<PickSeatState> = _pickSeatState
    
    // Events
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
                else -> {}
            }
        }
    }
    
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
                else -> {}
            }
        }
    }
    
    fun pickSeat(sectionId: Long, row: Int, column: Int) {
        _pickSeatState.value = PickSeatState.Loading
        
        viewModelScope.launch {
            val userId = TokenManager.getUserId().first()
            if (userId == null) {
                _pickSeatState.value = PickSeatState.Error("User ID not found. Please log in again.")
                return@launch
            }
            
            when (val result = seatRepository.pickSeat(userId, sectionId, row, column)) {
                is NetworkResult.Success -> {
                    _pickSeatState.value = PickSeatState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _pickSeatState.value = PickSeatState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetStates() {
        _seatsState.value = SeatsState.Idle
        _studentSeatState.value = StudentSeatState.Idle
        _pickSeatState.value = PickSeatState.Idle
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

sealed class PickSeatState {
    object Idle : PickSeatState()
    object Loading : PickSeatState()
    data class Success(val seat: Seat) : PickSeatState()
    data class Error(val message: String) : PickSeatState()
}
