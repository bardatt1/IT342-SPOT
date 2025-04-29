package com.example.spot.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for student ID login request
 */
data class StudentIdLoginRequest(
    @SerializedName("studentPhysicalId")
    val studentPhysicalId: String,
    
    @SerializedName("password")
    val password: String
)
