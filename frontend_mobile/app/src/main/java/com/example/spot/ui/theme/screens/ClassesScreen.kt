@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.Enrollment
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.*
import com.example.spot.viewmodel.ClassesViewModel
import com.example.spot.viewmodel.EnrollmentsState
import com.example.spot.viewmodel.EnrollState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ClassesScreen(
    navController: NavController,
    classesViewModel: ClassesViewModel = viewModel()
) {
    val enrollmentsState by classesViewModel.enrollmentsState.collectAsState()
    val enrollState by classesViewModel.enrollState.collectAsState()
    val enrollmentKey by classesViewModel.enrollmentKey.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    var showEnrollmentForm by remember { mutableStateOf(false) }
    
    // Handle enrollment state changes
    LaunchedEffect(enrollState) {
        when (enrollState) {
            is EnrollState.Success -> {
                val enrollment = (enrollState as EnrollState.Success).enrollment
                snackbarHostState.showSnackbar(
                    "Successfully enrolled in ${enrollment.section.course.courseCode} - ${enrollment.section.sectionName}",
                    duration = SnackbarDuration.Short
                )
                showEnrollmentForm = false
                classesViewModel.resetEnrollState()
            }
            is EnrollState.Error -> {
                val message = (enrollState as EnrollState.Error).message
                snackbarHostState.showSnackbar(
                    "Enrollment failed: $message",
                    duration = SnackbarDuration.Short
                )
                // Don't hide the form on error
                coroutineScope.launch {
                    delay(100) // Short delay to ensure focus works
                    focusRequester.requestFocus()
                }
                classesViewModel.resetEnrollState()
            }
            else -> { /* No action needed for other states */ }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Your Classes", color = Green700, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { 
                        showEnrollmentForm = !showEnrollmentForm
                        if (showEnrollmentForm) {
                            coroutineScope.launch {
                                delay(100) // Short delay to ensure focus works
                                focusRequester.requestFocus()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Class",
                            tint = Green700
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Enrollment Form
            AnimatedVisibility(visible = showEnrollmentForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Green100.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Enroll in a New Class",
                            style = MaterialTheme.typography.titleMedium,
                            color = Green700,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = enrollmentKey,
                            onValueChange = { classesViewModel.updateEnrollmentKey(it) },
                            label = { Text("Enrollment Key") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green700,
                                focusedLabelColor = Green700,
                                cursorColor = Green700
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { classesViewModel.enrollInClass() }
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEnrollmentForm = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Green700
                                )
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = { classesViewModel.enrollInClass() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green700
                                ),
                                enabled = enrollState !is EnrollState.Loading
                            ) {
                                if (enrollState is EnrollState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Enroll")
                                }
                            }
                        }
                    }
                }
            }
            
            // Main Content with classes
            when (enrollmentsState) {
                is EnrollmentsState.Idle -> {
                    // Initial state, start loading data
                    LaunchedEffect(Unit) {
                        classesViewModel.loadEnrollments()
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green700)
                    }
                }
                is EnrollmentsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green700)
                    }
                }
                is EnrollmentsState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "You're not enrolled in any classes yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextDark.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    showEnrollmentForm = true
                                    coroutineScope.launch {
                                        delay(100)
                                        focusRequester.requestFocus()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Green700)
                            ) {
                                Text("Enroll in a Class")
                            }
                        }
                    }
                }
                is EnrollmentsState.Error -> {
                    val error = (enrollmentsState as EnrollmentsState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Failed to load classes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextDark
                            )
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDark.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { classesViewModel.refreshEnrollments() },
                                colors = ButtonDefaults.buttonColors(containerColor = Green700)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is EnrollmentsState.Success -> {
                    val enrollments = (enrollmentsState as EnrollmentsState.Success).enrollments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(enrollments) { enrollment ->
                            ClassCard(enrollment, navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(enrollment: Enrollment, navController: NavController) {
    val section = enrollment.section
    val course = section.course
    val teacher = section.teacher
    
    // Construct teacher name
    val teacherName = if (teacher != null) {
        "${teacher.firstName} ${teacher.lastName}"
    } else {
        "No instructor assigned"
    }
    
    // Format the schedule information
    val scheduleText = remember(section.id, section.schedule) {
        when {
            section.schedule == null -> "No schedule information available"
            section.schedule.isBlank() -> "No schedule information available"
            else -> section.schedule
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { navController.navigate("${Routes.SEAT_PLAN.replace("{sectionId}", section.id.toString())}") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Green100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Instructor",
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${course.courseCode} - ${section.sectionName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (course.courseDescription.isNotBlank()) {
                    Text(
                        text = course.courseDescription,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDark.copy(alpha = 0.7f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = teacherName,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark.copy(alpha = 0.7f)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = scheduleText,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark.copy(alpha = 0.7f)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = { 
                    navController.navigate(Routes.QR_SCANNER)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green700),
                modifier = Modifier
                    .height(34.dp)
                    .width(60.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("SPOT", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}