package com.example.spot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpotTheme {
                MainApp()
            }
        }
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
