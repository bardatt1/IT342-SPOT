@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.Enrollment
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.Green700
import com.example.spot.ui.theme.TextDark
import com.example.spot.util.TokenManager
import com.example.spot.viewmodel.EnrollActionState
import com.example.spot.viewmodel.EnrollmentViewModel
import com.example.spot.viewmodel.EnrollmentsState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    enrollmentViewModel: EnrollmentViewModel = viewModel()
) {
    val enrollmentsState by enrollmentViewModel.enrollmentsState.collectAsState()
    val enrollActionState by enrollmentViewModel.enrollAction.collectAsState()
    var userName by remember { mutableStateOf("Student") }
    var showEnrollModal by remember { mutableStateOf(false) }
    var enrollmentKey by remember { mutableStateOf("") }
    var enrollError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Load user data and enrollments
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val name = TokenManager.getUserName().first() ?: "Student"
                userName = name
                
                // Load enrollments when component is created
                enrollmentViewModel.loadStudentEnrollments()
            } catch (e: Exception) {
                // Log.e("DashboardScreen", "Error loading user data", e)
            }
        }
    }
    
    // Handle enrollment action state changes
    LaunchedEffect(enrollActionState) {
        when (enrollActionState) {
            is EnrollActionState.Success -> {
                // Close the dialog and refresh enrollments on success
                showEnrollModal = false
                enrollmentKey = ""
                enrollError = null
                enrollmentViewModel.loadStudentEnrollments()
                // Reset the action state to prevent reprocessing
                enrollmentViewModel.resetEnrollActionState()
            }
            is EnrollActionState.Error -> {
                // Show error message but keep dialog open
                enrollError = (enrollActionState as EnrollActionState.Error).message
            }
            else -> {
                // Do nothing for other states
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Hello, $userName",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Green700,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Floating Action Button for Enrollment
            FloatingActionButton(
                onClick = { showEnrollModal = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp),
                containerColor = Green700,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Enroll in class")
            }
            
            // Content based on state
            when (enrollmentsState) {
                is EnrollmentsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green700)
                    }
                }
                is EnrollmentsState.Success -> {
                    val enrollments = (enrollmentsState as EnrollmentsState.Success).enrollments
                    
                    if (enrollments.isEmpty()) {
                        // No enrollments view
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Classes Enrolled",
                                    style = MaterialTheme.typography.headlineSmall.copy(color = TextDark),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the + button to enroll in a class",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                                )
                            }
                        }
                    } else {
                        // Show enrollments
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(enrollments) { enrollment ->
                                EnrollmentCard(
                                    enrollment = enrollment,
                                    onCardClick = {
                                        // Navigate to class view with the section ID
                                        navController.navigate("${Routes.CLASS_VIEW.substringBefore('{')}/${enrollment.section.id}")
                                    },
                                    onScanQrClick = {
                                        // Navigate to QR scanner
                                        navController.navigate(Routes.QR_SCANNER)
                                    },
                                    onSeatPlanClick = {
                                        // Navigate to seat plan
                                        navController.navigate("${Routes.SEAT_PLAN.replace("{sectionId}", enrollment.section.id.toString())}")
                                    }
                                )
                            }
                        }
                    }
                }
                is EnrollmentsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading classes",
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (enrollmentsState as EnrollmentsState.Error).message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { enrollmentViewModel.loadStudentEnrollments() },
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
    }
    
    // Enrollment Dialog
    if (showEnrollModal) {
        AlertDialog(
            onDismissRequest = { 
                if (enrollActionState !is EnrollActionState.Loading) {
                    showEnrollModal = false 
                }
            },
            title = { Text("Enroll in a Class") },
            text = {
                Column {
                    TextField(
                        value = enrollmentKey,
                        onValueChange = { enrollmentKey = it },
                        label = { Text("Enrollment Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enrollActionState !is EnrollActionState.Loading
                    )
                    
                    if (enrollError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = enrollError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    if (enrollActionState is EnrollActionState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Green700,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enrolling...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enrollmentKey.isBlank()) {
                            enrollError = "Please enter a valid enrollment key"
                        } else {
                            enrollError = null
                            // Call the enrollment function
                            enrollmentViewModel.enrollInSection(enrollmentKey)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green700),
                    enabled = enrollActionState !is EnrollActionState.Loading && enrollmentKey.isNotBlank()
                ) {
                    Text("Enroll")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEnrollModal = false },
                    enabled = enrollActionState !is EnrollActionState.Loading
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clean up when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            enrollmentViewModel.resetStates()
        }
    }
}

@Composable
fun EnrollmentCard(
    enrollment: Enrollment,
    onCardClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onSeatPlanClick: () -> Unit
) {
    val courseNamePrefix = enrollment.section.course.courseCode
    val courseName = enrollment.section.course.courseName
    val sectionName = enrollment.section.sectionName
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course code circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Green700),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = courseNamePrefix.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Course details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$courseNamePrefix - $sectionName",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Seat plan button
            IconButton(
                onClick = onSeatPlanClick
            ) {
                Icon(
                    imageVector = Icons.Default.Chair,
                    contentDescription = "View Seat Plan",
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // QR code scan button
            IconButton(
                onClick = onScanQrClick
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Scan QR for Attendance",
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}