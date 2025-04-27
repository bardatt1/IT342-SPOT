package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Section
import com.example.spot.repository.SectionRepository
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for section operations following MVI pattern
 */
class SectionViewModel : ViewModel() {
    
    private val sectionRepository = SectionRepository()
    
    // UI States
    private val _sectionsState = MutableStateFlow<SectionsState>(SectionsState.Idle)
    val sectionsState: StateFlow<SectionsState> = _sectionsState
    
    private val _sectionDetailsState = MutableStateFlow<SectionDetailsState>(SectionDetailsState.Idle)
    val sectionDetailsState: StateFlow<SectionDetailsState> = _sectionDetailsState
    
    // Events
    fun loadSectionsByCourse(courseId: Long) {
        _sectionsState.value = SectionsState.Loading
        
        viewModelScope.launch {
            when (val result = sectionRepository.getSectionsByCourseId(courseId)) {
                is NetworkResult.Success -> {
                    _sectionsState.value = SectionsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _sectionsState.value = SectionsState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun loadSectionDetails(sectionId: Long) {
        _sectionDetailsState.value = SectionDetailsState.Loading
        
        viewModelScope.launch {
            when (val result = sectionRepository.getSectionById(sectionId)) {
                is NetworkResult.Success -> {
                    _sectionDetailsState.value = SectionDetailsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _sectionDetailsState.value = SectionDetailsState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Get section details by enrollment key
     * This is used for class view when student clicks on a class card
     */
    fun getSectionByEnrollmentKey(enrollmentKey: String) {
        _sectionDetailsState.value = SectionDetailsState.Loading
        
        viewModelScope.launch {
            when (val result = sectionRepository.getSectionByEnrollmentKey(enrollmentKey)) {
                is NetworkResult.Success -> {
                    _sectionDetailsState.value = SectionDetailsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _sectionDetailsState.value = SectionDetailsState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetStates() {
        _sectionsState.value = SectionsState.Idle
        _sectionDetailsState.value = SectionDetailsState.Idle
    }
}

// States for Section UI
sealed class SectionsState {
    object Idle : SectionsState()
    object Loading : SectionsState()
    data class Success(val sections: List<Section>) : SectionsState()
    data class Error(val message: String) : SectionsState()
}

sealed class SectionDetailsState {
    object Idle : SectionDetailsState()
    object Loading : SectionDetailsState()
    data class Success(val section: Section) : SectionDetailsState()
    data class Error(val message: String) : SectionDetailsState()
}
