package com.example.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spot.model.Course
import com.example.spot.repository.CourseRepository
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for course operations following MVI pattern
 */
class CourseViewModel : ViewModel() {
    
    private val courseRepository = CourseRepository()
    
    // UI States
    private val _coursesState = MutableStateFlow<CoursesState>(CoursesState.Idle)
    val coursesState: StateFlow<CoursesState> = _coursesState
    
    private val _courseDetailsState = MutableStateFlow<CourseDetailsState>(CourseDetailsState.Idle)
    val courseDetailsState: StateFlow<CourseDetailsState> = _courseDetailsState
    
    // Events
    fun loadAllCourses() {
        _coursesState.value = CoursesState.Loading
        
        viewModelScope.launch {
            when (val result = courseRepository.getAllCourses()) {
                is NetworkResult.Success -> {
                    _coursesState.value = CoursesState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _coursesState.value = CoursesState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun loadCourseDetails(courseId: Long) {
        _courseDetailsState.value = CourseDetailsState.Loading
        
        viewModelScope.launch {
            when (val result = courseRepository.getCourseById(courseId)) {
                is NetworkResult.Success -> {
                    _courseDetailsState.value = CourseDetailsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _courseDetailsState.value = CourseDetailsState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun loadCourseByCode(courseCode: String) {
        _courseDetailsState.value = CourseDetailsState.Loading
        
        viewModelScope.launch {
            when (val result = courseRepository.getCourseByCourseCode(courseCode)) {
                is NetworkResult.Success -> {
                    _courseDetailsState.value = CourseDetailsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _courseDetailsState.value = CourseDetailsState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetStates() {
        _coursesState.value = CoursesState.Idle
        _courseDetailsState.value = CourseDetailsState.Idle
    }
}

// States for Course UI
sealed class CoursesState {
    object Idle : CoursesState()
    object Loading : CoursesState()
    data class Success(val courses: List<Course>) : CoursesState()
    data class Error(val message: String) : CoursesState()
}

sealed class CourseDetailsState {
    object Idle : CourseDetailsState()
    object Loading : CourseDetailsState()
    data class Success(val course: Course) : CourseDetailsState()
    data class Error(val message: String) : CourseDetailsState()
}
