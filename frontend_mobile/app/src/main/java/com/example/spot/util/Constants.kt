package com.example.spot.util

/**
 * Constants used throughout the app
 */
object Constants {
    /**
     * API base URL - Uses 10.0.2.2 to reach localhost from Android emulator
     * For real devices on the same network as the server, use the actual IP address
     */
    const val API_BASE_URL = "http://10.0.2.2:8080/"
    
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
