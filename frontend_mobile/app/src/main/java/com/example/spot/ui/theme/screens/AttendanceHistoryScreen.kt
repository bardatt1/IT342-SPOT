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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.example.spot.viewmodel.ClassesViewModel
import com.example.spot.viewmodel.EnrollmentsState

/**
 * Screen to display list of courses for attendance history selection
 */
@Composable
fun AttendanceHistoryScreen(
    navController: NavController,
    classesViewModel: ClassesViewModel = viewModel()
) {
    val enrollmentsState by classesViewModel.enrollmentsState.collectAsState()
    
    // Load student enrollments when component is created
    LaunchedEffect(Unit) {
        classesViewModel.loadEnrollments()
    }
    
    // Clean up when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            // Reset enrollment state
            classesViewModel.resetEnrollState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance History", color = Green700, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_revert),
                            contentDescription = "Back",
                            tint = Green700
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (enrollmentsState) {
                is EnrollmentsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Green700
                    )
                }
                
                is EnrollmentsState.Success -> {
                    val enrollments = (enrollmentsState as EnrollmentsState.Success).enrollments
                    if (enrollments.isEmpty()) {
                        Text(
                            text = "You are not enrolled in any courses",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            item {
                                Text(
                                    text = "Select a course to view attendance",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        color = TextDark
                                    ),
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                            
                            items(enrollments) { enrollment ->
                                AttendanceEnrollmentCard(
                                    enrollment = enrollment,
                                    onClick = {
                                        // Navigate to calendar view with attendance details
                                        navController.navigate("attendance_calendar/${enrollment.section.id}")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                is EnrollmentsState.Empty -> {
                    Text(
                        text = "You are not enrolled in any courses",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                is EnrollmentsState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading courses",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (enrollmentsState as EnrollmentsState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { classesViewModel.loadEnrollments() },
                            colors = ButtonDefaults.buttonColors(containerColor = Green700)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                else -> {}
            }
        }
    }
}

@Composable
fun AttendanceEnrollmentCard(
    enrollment: Enrollment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    text = enrollment.section.course.courseCode.take(2).uppercase(),
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
                    text = "${enrollment.section.course.courseCode} - ${enrollment.section.sectionName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = enrollment.section.course.courseName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Schedule: ${enrollment.section.schedule ?: "Not available"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Right arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Attendance",
                tint = Green700
            )
        }
    }
}
