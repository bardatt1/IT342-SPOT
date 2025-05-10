package com.example.spot

import android.app.Application
import android.content.Context
import com.example.spot.util.TokenManager

/**
 * Application class for initializing app-wide components
 */
class SpotApplication : Application() {
    
    companion object {
        lateinit var appContext: Context
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Store application context for use throughout the app
        appContext = applicationContext
        
        // Initialize token manager for authentication
        TokenManager.initialize(applicationContext)
    }
}
