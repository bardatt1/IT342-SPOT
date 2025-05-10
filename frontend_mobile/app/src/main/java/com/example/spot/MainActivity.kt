package com.example.spot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.spot.navigation.AppNavGraph
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.BottomBar
import com.example.spot.ui.theme.SpotTheme
import com.example.spot.util.NotificationLogger
import com.example.spot.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    // ViewModel for authentication that persists across configuration changes
    private val authViewModel: AuthViewModel by viewModels()
    

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the NotificationLogger
        NotificationLogger.init(applicationContext)
        

        
        setContent {
            SpotTheme {
                MainApp()
            }
        }
    }
    

    
    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.LOGIN
    
    // List of routes where BottomBar should be visible
    val bottomBarRoutes = listOf(
        Routes.DASHBOARD,
        Routes.CLASSES,
        Routes.NOTIFICATIONS,
        Routes.PROFILE
    )
    
    // Only show BottomBar on main screens
    val showBottomBar = bottomBarRoutes.any { 
        currentRoute == it || currentRoute.startsWith(it)
    }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
