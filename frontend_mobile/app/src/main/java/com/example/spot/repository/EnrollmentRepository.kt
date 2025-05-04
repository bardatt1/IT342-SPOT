package com.example.spot.repository

import android.util.Log
import com.example.spot.model.EnrollRequest
import com.example.spot.model.Enrollment
import com.example.spot.model.Section
import com.example.spot.model.SectionSchedule
import com.example.spot.model.Schedule
import com.example.spot.network.RetrofitClient
import com.example.spot.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Repository for handling enrollment related API calls
 */
class EnrollmentRepository {
    private val apiService = RetrofitClient.apiService
    
    // Cache of student enrollments to work around backend permission issues
    private val _enrollmentsCache = MutableStateFlow<List<Enrollment>>(emptyList())
    val enrollmentsCache: StateFlow<List<Enrollment>> = _enrollmentsCache
    
    /**
     * Get all enrollments for a student using the cache first, then fall back to API call
     * This is a workaround for the backend permission issue with the /api/enrollments/student/{studentId} endpoint
     */
    suspend fun getEnrollmentsByStudentId(studentId: Long): NetworkResult<List<Enrollment>> {
        return withContext(Dispatchers.IO) {
            try {
                // First try to load from the backend
                val response = apiService.getEnrollmentsByStudentId(studentId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    // If successful, update cache and return data
                    _enrollmentsCache.value = response.data
                    
                    // Process each enrollment to add schedules to the sections
                    val enrollmentsWithSchedules = response.data.map { enrollment ->
                        try {
                            // Fetch schedules for this section
                            val schedulesResult = getSchedulesForSection(enrollment.section.id)
                            
                            if (schedulesResult is NetworkResult.Success && schedulesResult.data.isNotEmpty()) {
                                // Create formatted section schedules
                                val sectionSchedules = schedulesResult.data.map { schedule ->
                                    val dayName = when(schedule.dayOfWeek) {
                                        1 -> "Monday"
                                        2 -> "Tuesday"
                                        3 -> "Wednesday"
                                        4 -> "Thursday"
                                        5 -> "Friday"
                                        6 -> "Saturday"
                                        7 -> "Sunday"
                                        else -> "Unknown"
                                    }
                                    SectionSchedule(
                                        day = dayName,
                                        startTime = schedule.timeStart,
                                        endTime = schedule.timeEnd
                                    )
                                }
                                
                                // Create a new section with schedules
                                val updatedSection = Section(
                                    id = enrollment.section.id,
                                    course = enrollment.section.course,
                                    teacher = enrollment.section.teacher,
                                    sectionName = enrollment.section.sectionName,
                                    enrollmentKey = enrollment.section.enrollmentKey,
                                    enrollmentOpen = enrollment.section.enrollmentOpen,
                                    enrollmentCount = enrollment.section.enrollmentCount,
                                    schedule = enrollment.section.schedule,
                                    schedules = sectionSchedules
                                )
                                
                                // Create a new enrollment with the updated section
                                enrollment.copy(section = updatedSection)
                            } else {
                                enrollment
                            }
                        } catch (e: Exception) {
                            Log.e("EnrollmentRepository", "Error fetching schedules for section ${enrollment.section.id}", e)
                            enrollment
                        }
                    }
                    
                    // Update cache with enriched data
                    _enrollmentsCache.value = enrollmentsWithSchedules
                    return@withContext NetworkResult.Success(enrollmentsWithSchedules)
                } else {
                    // If backend call fails but we have cached data, use that
                    if (_enrollmentsCache.value.isNotEmpty()) {
                        Log.d("EnrollmentRepository", "Using cached enrollments due to backend permission issue")
                        return@withContext NetworkResult.Success(_enrollmentsCache.value)
                    }
                    
                    // If both fail, try to fetch sections with enrollment status
                    try {
                        // Get all courses
                        val coursesResponse = apiService.getAllCourses()
                        if (coursesResponse.result == "SUCCESS" && coursesResponse.data != null) {
                            val sections = mutableListOf<Section>()
                            
                            // For each course, get its sections
                            coursesResponse.data.forEach { course ->
                                val sectionsResponse = apiService.getSectionsByCourseId(course.id)
                                if (sectionsResponse.result == "SUCCESS" && sectionsResponse.data != null) {
                                    sections.addAll(sectionsResponse.data)
                                }
                            }
                            
                            // Build enrollments list
                            val enrollments = _enrollmentsCache.value.toMutableList()
                            
                            return@withContext NetworkResult.Success(enrollments)
                        }
                    } catch (e: Exception) {
                        Log.e("EnrollmentRepository", "Error fetching courses and sections", e)
                    }
                    
                    // If all else fails, return error from original call
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Get enrollments error (Ask Gemini)", e)
                
                // If we have cached data, use that instead of failing
                if (_enrollmentsCache.value.isNotEmpty()) {
                    Log.d("EnrollmentRepository", "Using cached enrollments after API error")
                    return@withContext NetworkResult.Success(_enrollmentsCache.value)
                }
                
                return@withContext NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Get all enrollments for a section
     */
    suspend fun getEnrollmentsBySectionId(sectionId: Long): NetworkResult<List<Enrollment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEnrollmentsBySectionId(sectionId)
                
                if (response.result == "SUCCESS") {
                    return@withContext NetworkResult.Success(response.data ?: emptyList())
                } else {
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Get section enrollments error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Check if a student is enrolled in a section
     */
    suspend fun isStudentEnrolled(studentId: Long, sectionId: Long): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.isStudentEnrolled(studentId, sectionId)
                
                if (response.result == "SUCCESS" && response.data != null) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Check enrollment error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Enroll a student in a section using an enrollment key
     */
    suspend fun enrollStudent(enrollmentKey: String): NetworkResult<Enrollment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.enrollStudent(EnrollRequest(enrollmentKey))
                
                if (response.result == "SUCCESS" && response.data != null) {
                    // Add the new enrollment to our cache
                    val updatedCache = _enrollmentsCache.value.toMutableList()
                    updatedCache.add(response.data)
                    _enrollmentsCache.value = updatedCache
                    
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Enroll student error", e)
                NetworkResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Fetch schedules for a specific section
     */
    private suspend fun getSchedulesForSection(sectionId: Long): NetworkResult<List<Schedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSchedulesBySectionId(sectionId)
                
                if (response.result == "SUCCESS") {
                    return@withContext NetworkResult.Success(response.data ?: emptyList())
                } else {
                    return@withContext NetworkResult.Error(response.message)
                }
            } catch (e: Exception) {
                Log.e("EnrollmentRepository", "Error fetching schedules", e)
                return@withContext NetworkResult.Error("Network error: ${e.message}")
            }
        }
    }
}
