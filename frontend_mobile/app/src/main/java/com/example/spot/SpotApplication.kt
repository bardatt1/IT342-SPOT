package com.example.spot

import android.app.Application
import com.example.spot.util.TokenManager

/**
 * Application class for initializing app-wide components
 */
class SpotApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize token manager for authentication
        TokenManager.initialize(applicationContext)
    }
}
