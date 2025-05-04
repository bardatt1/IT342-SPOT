@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
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
                // Don't hide the form on error, but also don't immediately try to focus
                // as this might cause the FocusRequester error
                classesViewModel.resetEnrollState()
            }
            else -> { /* No action needed for other states */ }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ViewModule,
                            contentDescription = "Classes",
                            tint = Green700,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your Classes", 
                            color = Green700, 
                            fontWeight = FontWeight.Bold
                        ) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(
                        onClick = { 
                            showEnrollmentForm = !showEnrollmentForm
                            if (showEnrollmentForm) {
                                coroutineScope.launch {
                                    delay(300) // Increased delay to ensure animation completes
                                    try {
                                        focusRequester.requestFocus()
                                    } catch (e: Exception) {
                                        // Safely handle focus request failures
                                        Log.e("ClassesScreen", "Failed to request focus: ${e.message}")
                                    }
                                }
                            } else {
                                // Clear enrollment key when hiding form
                                classesViewModel.updateEnrollmentKey("")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (showEnrollmentForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showEnrollmentForm) "Cancel" else "Add Class",
                            tint = Green700
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Enrollment Form
                AnimatedVisibility(visible = showEnrollmentForm) {
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Enroll in a New Class",
                                style = MaterialTheme.typography.titleMedium,
                                color = Green700,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = enrollmentKey,
                                onValueChange = { classesViewModel.updateEnrollmentKey(it) },
                                label = { Text("Enrollment Key") },
                                placeholder = { Text("Enter the key provided by your teacher") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green700,
                                    focusedLabelColor = Green700,
                                    cursorColor = Green700
                                ),
                                isError = enrollmentKey.isBlank() && enrollState is EnrollState.Error,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { 
                                        if (enrollmentKey.isNotBlank()) {
                                            classesViewModel.enrollInClass()
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Please enter a valid enrollment key",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = "Enrollment Key",
                                        tint = Green700
                                    )
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { 
                                        showEnrollmentForm = false 
                                        classesViewModel.updateEnrollmentKey("")  // Clear input on cancel
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Green700
                                    ),
                                    border = BorderStroke(1.dp, Green700.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        "Cancel",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                
                                Button(
                                    onClick = { 
                                        if (enrollmentKey.isBlank()) {
                                            // Show error in SnackBar rather than trying to set focus
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Please enter a valid enrollment key",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            classesViewModel.enrollInClass() 
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Enroll",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Green700)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading your classes...", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Green700
                                )
                            }
                        }
                    }
                    is EnrollmentsState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Green700)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading your classes...", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Green700
                                )
                            }
                        }
                    }
                    is EnrollmentsState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Green700.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.School,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "No Classes Yet",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Green700
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "You're not enrolled in any classes yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDark.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
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
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Enroll in a Class")
                                    }
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Failed to load classes",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDark
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDark.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { classesViewModel.refreshEnrollments() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Green700)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Retry", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    is EnrollmentsState.Success -> {
                        val enrollments = (enrollmentsState as EnrollmentsState.Success).enrollments
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { navController.navigate("${Routes.SEAT_PLAN.replace("{sectionId}", section.id.toString())}") }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with course code and title
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
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Class details
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = teacherName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark.copy(alpha = 0.7f)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                verticalAlignment = Alignment.Top,
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
                    text = scheduleText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark.copy(alpha = 0.7f)),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (course.courseDescription.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Green700,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = course.courseDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDark.copy(alpha = 0.7f)),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("${Routes.SEAT_PLAN.replace("{sectionId}", section.id.toString())}") },
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
                    onClick = { navController.navigate(Routes.QR_SCANNER) },
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