package com.example.spot.network

import com.example.spot.model.CreateStudentRequest
import com.example.spot.model.*
import retrofit2.http.*

/**
 * API Service interface containing endpoints from the Spring Boot backend
 */
interface ApiService {
    
    // Authentication
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): ApiResponse<JwtResponse>
    
    @POST("api/auth/login/student-id")
    suspend fun loginWithStudentId(@Body loginRequest: StudentIdLoginRequest): ApiResponse<JwtResponse>
    
    @POST("api/auth/bind-oauth")
    suspend fun bindGoogleAccount(
        @Query("email") email: String,
        @Query("googleId") googleId: String
    ): ApiResponse<Boolean>
    
    @GET("api/auth/oauth2/success")
    suspend fun googleOauthLogin(
        @Query("registrationType") registrationType: String,
        @Query("email") email: String,
        @Query("googleId") googleId: String
    ): ApiResponse<JwtResponse>
    
    @POST("api/students/{id}/bind-oauth")
    suspend fun bindStudentGoogleAccount(
        @Path("id") id: Long,
        @Query("googleId") googleId: String
    ): ApiResponse<Student>
    
    @POST("api/teachers/{id}/bind-oauth")
    suspend fun bindTeacherGoogleAccount(
        @Path("id") id: Long,
        @Query("googleId") googleId: String
    ): ApiResponse<Teacher>
    
    @GET("api/auth/check-email")
    suspend fun checkEmailInUse(@Query("email") email: String): ApiResponse<Boolean>
    
    @GET("api/students/physical-id/{physicalId}")
    suspend fun getStudentByPhysicalId(@Path("physicalId") physicalId: String): ApiResponse<Student>
    
    // Student registration
    @POST("api/admin/create-student")
    suspend fun registerStudent(@Body createStudentRequest: CreateStudentRequest): ApiResponse<Student>
    
    // Student
    @GET("api/students/{id}")
    suspend fun getStudentById(@Path("id") id: Long): ApiResponse<Student>
    
    @GET("api/students/email/{email}")
    suspend fun getStudentByEmail(@Path("email") email: String): ApiResponse<Student>
    
    // Student operations
    @PUT("api/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: Long,
        @Body updateRequest: StudentUpdateRequest
    ): ApiResponse<Student>
    
    // Enrollments
    @GET("api/enrollments/student/{studentId}")
    suspend fun getEnrollmentsByStudentId(@Path("studentId") studentId: Long): ApiResponse<List<Enrollment>>
    
    @GET("api/enrollments/section/{sectionId}")
    suspend fun getEnrollmentsBySectionId(@Path("sectionId") sectionId: Long): ApiResponse<List<Enrollment>>
    
    @GET("api/enrollments/status")
    suspend fun isStudentEnrolled(
        @Query("studentId") studentId: Long,
        @Query("sectionId") sectionId: Long
    ): ApiResponse<Boolean>
    
    @POST("api/enrollments/enroll")
    suspend fun enrollStudent(@Body enrollRequest: EnrollRequest): ApiResponse<Enrollment>
    
    // Seats
    @GET("api/seats")
    suspend fun getSeatsBySectionId(@Query("sectionId") sectionId: Long): ApiResponse<List<Seat>>
    
    @GET("api/seats/section/{sectionId}")
    suspend fun getAllSeatsForSection(@Path("sectionId") sectionId: Long): ApiResponse<List<Seat>>
    
    @GET("api/seats/student")
    suspend fun getSeatByStudentAndSectionId(
        @Query("studentId") studentId: Long,
        @Query("sectionId") sectionId: Long
    ): ApiResponse<Seat>
    
    @POST("api/seats/pick")
    suspend fun pickSeat(@Body pickSeatRequest: PickSeatRequest): ApiResponse<Seat>
    
    // Sections
    @GET("api/sections")
    suspend fun getSectionsByCourseId(@Query("courseId") courseId: Long): ApiResponse<List<Section>>
    
    @GET("api/sections/{id}")
    suspend fun getSectionById(@Path("id") id: Long): ApiResponse<Section>
    
    @GET("api/sections/enrollment-key/{enrollmentKey}")
    suspend fun getSectionByEnrollmentKey(@Path("enrollmentKey") enrollmentKey: String): ApiResponse<Section>
    
    // Schedules
    @GET("api/schedules")
    suspend fun getSchedulesBySectionId(@Query("sectionId") sectionId: Long): ApiResponse<List<Schedule>>
    
    // Attendance
    @POST("api/attendance/log")
    suspend fun logAttendance(@Body logAttendanceRequest: LogAttendanceRequest): ApiResponse<Attendance>
    
    @GET("api/attendance/student/{studentId}")
    suspend fun getAttendanceByStudentId(@Path("studentId") studentId: Long): ApiResponse<List<Attendance>>
    
    // Courses
    @GET("api/courses")
    suspend fun getAllCourses(): ApiResponse<List<Course>>
    
    @GET("api/courses/{id}")
    suspend fun getCourseById(@Path("id") id: Long): ApiResponse<Course>
    
    @GET("api/courses/code/{courseCode}")
    suspend fun getCourseByCourseCode(@Path("courseCode") courseCode: String): ApiResponse<Course>
}
