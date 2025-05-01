@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.Student
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.*
import com.example.spot.viewmodel.StudentState
import com.example.spot.viewmodel.StudentViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    studentViewModel: StudentViewModel = viewModel()
) {
    val studentState by studentViewModel.studentState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Load student profile when component is created
    LaunchedEffect(Unit) {
        studentViewModel.loadStudentProfile()
    }
    
    // Clean up when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            studentViewModel.resetStates()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Profile", color = Green700, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Content based on state
        when (studentState) {
            is StudentState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green700)
                }
            }
            
            is StudentState.Success -> {
                val student = (studentState as StudentState.Success).student
                ProfileContent(
                    student = student,
                    navController = navController,
                    onLogoutClick = { showLogoutDialog = true }
                )
            }
            
            is StudentState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading profile",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (studentState as StudentState.Error).message,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { studentViewModel.loadStudentProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = Green700)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                // Idle state, do nothing
            }
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    student: Student,
    navController: NavController,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile picture placeholder with initials
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Green700.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            val initials = student.run {
                val first = firstName.firstOrNull() ?: ""
                val last = lastName.firstOrNull() ?: ""
                "$first$last"
            }
            
            Text(
                text = initials.uppercase(),
                color = Green700,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User full name
        Text(
            text = "${student.firstName} ${student.middleName ?: ""} ${student.lastName}".trim(),
            color = TextDark,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // User email
        Text(
            text = student.email,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Student specific fields
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Student ID: ${student.studentPhysicalId}",
                color = TextDark.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${student.program} • Year ${student.year}",
                color = TextDark.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile options
        ProfileOption(
            title = "Edit Profile",
            onClick = { navController.navigate(Routes.EDIT_PROFILE) }
        )

        ProfileOption(
            title = "Attendance History",
            onClick = { navController.navigate(Routes.ATTENDANCE_HISTORY) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Logout option
        ProfileOption(
            title = "Logout",
            onClick = onLogoutClick,
            isLogout = true
        )
    }
}

@Composable
fun ProfileOption(
    title: String,
    onClick: () -> Unit,
    isLogout: Boolean = false
) {
    val textColor = if (isLogout) MaterialTheme.colorScheme.error else TextDark
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            
            if (!isLogout) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Green700
                )
            }
        }
    }
}