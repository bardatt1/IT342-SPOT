package com.example.spot.util

/**
 * Constants used throughout the app
 */
object Constants {
    /**
     * API base URLs - Different URLs for emulator and physical devices
     * - 10.0.2.2 is used to reach localhost from Android emulator
     * - For real devices, use the actual IP address of your development machine
     * 
     * You can toggle between them by changing USE_EMULATOR_URL
     */
    private const val USE_EMULATOR_URL = false
    private const val EMULATOR_BASE_URL = "https://backend.spot-edu.me/"
    private const val PHYSICAL_DEVICE_BASE_URL = "https://backend.spot-edu.me/" // Updated with correct IP address
    
    val API_BASE_URL: String
        get() = if (USE_EMULATOR_URL) EMULATOR_BASE_URL else PHYSICAL_DEVICE_BASE_URL
    
    /**
     * QR code format for attendance
     */
    const val QR_ATTENDANCE_PREFIX = "attend:"
    
    /**
     * Authentication token type
     */
    const val TOKEN_TYPE = "Bearer"
    
    /**
     * Preference keys
     */
    const val PREF_NAME = "spot_preferences"
}
