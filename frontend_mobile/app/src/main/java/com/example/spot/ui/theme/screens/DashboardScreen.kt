@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.R
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

// Use the existing Green700 color from the theme
// val SpotPrimaryColor = Color(0xFF215F47)

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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // SPOT Logo
                        Image(
                            painter = painterResource(id = R.drawable.spot_logo),
                            contentDescription = "SPOT Logo",
                            modifier = Modifier.height(36.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // App name
                        Text(
                            text = "SPOT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Green700
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { paddingValues ->
        // Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Content based on state
            when (enrollmentsState) {
                is EnrollmentsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Green700)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading enrollments...", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = Green700
                            )
                        }
                    }
                }
                
                is EnrollmentsState.Empty -> {
                    // Empty enrollment state - Show guidance to enroll in classes
                    EmptyEnrollmentContent(navController, userName)
                }
                
                is EnrollmentsState.Success -> {
                    val enrollments = (enrollmentsState as EnrollmentsState.Success).enrollments
                    
                    if (enrollments.isEmpty()) {
                        // Success but with empty list - Show guidance to enroll in classes
                        EmptyEnrollmentContent(navController, userName)
                    } else {
                        // Show enrollments
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Green700,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your Enrollments",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Green700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(enrollments) { enrollment ->
                                    EnrollmentCard(
                                        enrollment = enrollment,
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
                }
                is EnrollmentsState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Error loading classes",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = (enrollmentsState as EnrollmentsState.Error).message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Button(
                                onClick = { enrollmentViewModel.loadStudentEnrollments() },
                                colors = ButtonDefaults.buttonColors(containerColor = Green700),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
                    enrollError = null
                }
            },
            title = { 
                Text(
                    "Enroll in a Class", 
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Green700,
                        fontWeight = FontWeight.SemiBold
                    )
                ) 
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = enrollmentKey,
                        onValueChange = { enrollmentKey = it },
                        label = { Text("Enrollment Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enrollActionState !is EnrollActionState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green700,
                            focusedLabelColor = Green700,
                            cursorColor = Green700
                        )
                    )
                    
                    if (enrollError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = enrollError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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
                                color = Green700,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enrollmentKey.isNotBlank()) {
                            enrollmentViewModel.enrollInSection(enrollmentKey)
                        }
                    },
                    enabled = enrollmentKey.isNotBlank() && enrollActionState !is EnrollActionState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) {
                    Text("Enroll")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (enrollActionState !is EnrollActionState.Loading) {
                            showEnrollModal = false 
                            enrollError = null
                        }
                    },
                    enabled = enrollActionState !is EnrollActionState.Loading
                ) {
                    Text("Cancel", color = Green700)
                }
            }
        )
    }
}

@Composable
fun EmptyEnrollmentContent(navController: NavController, userName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Green700.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "No classes",
                            tint = Green700,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Welcome, $userName!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Green700
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "No Classes Enrolled",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "You haven't enrolled in any classes yet. To start tracking your attendance, you need to enroll in classes using an enrollment key provided by your teacher.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Call to action card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Green700.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How to Enroll in a Class:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Green700
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Steps
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Green700),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1", 
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Go to the Classes screen",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextDark
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Green700),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2", 
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Enter the enrollment key your teacher provided",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextDark
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Green700),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3", 
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Start tracking your attendance!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextDark
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { navController.navigate(Routes.CLASSES) },
                        colors = ButtonDefaults.buttonColors(containerColor = Green700),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Enroll",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enroll in Classes",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentCard(
    enrollment: Enrollment,
    onScanQrClick: () -> Unit,
    onSeatPlanClick: () -> Unit
) {
    val section = enrollment.section
    val course = section.course
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Course and Section Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Green700.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Green700,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${course.courseCode} - ${section.sectionName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Green700,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Schedule Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = section.getScheduleDisplay(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSeatPlanClick,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Green700),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green700)
                ) {
                    Icon(
                        imageVector = Icons.Default.Grid4x4,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Seat Plan")
                }
                
                Button(
                    onClick = onScanQrClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan QR")
                }
            }
        }
    }
}