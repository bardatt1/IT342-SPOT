@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.spot.model.SeatCoordinate
import com.example.spot.model.Section
import com.example.spot.model.Seat
import com.example.spot.navigation.Routes
import com.example.spot.network.RetrofitClient
import com.example.spot.repository.SectionRepository
import com.example.spot.util.NetworkResult
import com.example.spot.util.TokenManager
import com.example.spot.viewmodel.SeatPlanPickState
import com.example.spot.viewmodel.SeatPlanState
import com.example.spot.viewmodel.SeatPlanViewModel
import com.example.spot.viewmodel.SectionViewModel
import com.example.spot.viewmodel.SectionState

/**
 * Screen for displaying and selecting seats for a section
 * Allows students to view the classroom seat plan and pick a seat
 */
@Composable
fun SeatPlanScreen(
    navController: NavController,
    sectionId: Long,
    viewModel: SeatPlanViewModel = viewModel(),
    sectionViewModel: SectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val seatsState by viewModel.seatsState.collectAsState()
    val selectedSeatState by viewModel.selectedSeatState.collectAsState()
    val pickSeatState by viewModel.pickSeatState.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val sectionState by sectionViewModel.sectionState.collectAsState()
    val currentSection by viewModel.sectionState.collectAsState()
    
    // Load section when sectionId changes
    LaunchedEffect(sectionId) {
        sectionViewModel.fetchSectionById(sectionId)
    }
    
    // Set the section when section data is loaded
    LaunchedEffect(sectionState) {
        if (sectionState is SectionState.Success) {
            val successState = sectionState as SectionState.Success
            Log.d("SeatPlanScreen", "Section loaded from SectionViewModel: ${successState.section.id}, schedule: ${successState.section.schedule}")
            viewModel.setSection(successState.section)
        }
    }
    
    // Log when currentSection changes
    LaunchedEffect(currentSection) {
        Log.d("SeatPlanScreen", "Current section from SeatPlanViewModel updated: ${currentSection?.id}, schedule: ${currentSection?.schedule}")
    }
    
    // Handle seat picking state
    LaunchedEffect(pickSeatState) {
        when (pickSeatState) {
            is SeatPlanPickState.Success -> {
                // Show success toast
                Toast.makeText(
                    context,
                    "Seat successfully selected!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is SeatPlanPickState.Error -> {
                // Show error toast
                val errorState = pickSeatState as SeatPlanPickState.Error
                Toast.makeText(
                    context,
                    "Error: ${errorState.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                // Handle other states
            }
        }
    }
    
    // Clean up when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetStates()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val titleText = if (sectionState is SectionState.Success) {
                        val successState = sectionState as SectionState.Success
                        "${successState.section.course.courseCode} - ${successState.section.sectionName}"
                    } else {
                        "Seat Plan"
                    }
                    Text(titleText, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (sectionState) {
                is SectionState.Loading -> {
                    LoadingState()
                }
                is SectionState.Error -> {
                    val errorState = sectionState as SectionState.Error
                    ErrorState(
                        message = errorState.message,
                        onRetry = { sectionViewModel.fetchSectionById(sectionId) }
                    )
                }
                is SectionState.Success -> {
                    val successState = sectionState as SectionState.Success
                    when (val currentSeatsState = seatsState) {
                        is SeatPlanState.Success -> {
                            SeatPlanContent(
                                seats = currentSeatsState.seats,
                                section = currentSection ?: successState.section,
                                selectedSeatState = selectedSeatState,
                                userId = userId,
                                onSeatSelected = { row, col -> viewModel.selectSeat(SeatCoordinate(row, col)) },
                                onSubmitSeatSelection = { viewModel.submitSeatSelection() }
                            )
                        }
                        is SeatPlanState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is SeatPlanState.Error -> {
                            ErrorScreen(
                                message = currentSeatsState.message.orEmpty(),
                                onRetry = { viewModel.loadSeatPlan(sectionId) }
                            )
                        }
                        else -> {
                            // Idle state or other states
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Waiting for seat data...")
                            }
                        }
                    }
                }
                else -> {
                    // Idle state - nothing to display
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun SeatPlanContent(
    seats: List<Seat>,
    section: Section,
    selectedSeatState: SeatCoordinate?,
    userId: Long?,
    onSeatSelected: (Int, Int) -> Unit,
    onSubmitSeatSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Teacher info
        TeacherInfoCard(section)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Seat selection section
        Text(
            text = "Classroom Seat Plan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Seats grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Teacher's desk at the top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Teacher's Desk", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Seat grid - 5 rows, 6 columns as per the updated requirements
                    for (row in 0 until 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until 6) {
                                // Find if this seat is taken by looking through the seats list
                                val seat = seats.find { 
                                    it.row == row && it.column == col 
                                }
                                
                                // Determine if this is the student's current seat
                                val isStudentSeat = seat?.student?.id == userId
                                val isOccupiedByOther = seat?.student != null && !isStudentSeat
                                
                                SeatBox(
                                    row = row,
                                    column = col,
                                    isOccupied = isOccupiedByOther,
                                    isSelected = selectedSeatState?.let { 
                                        it.row == row && it.column == col 
                                    } ?: false,
                                    isStudentSeat = isStudentSeat,
                                    onClick = { r, c -> 
                                        if (seat?.student == null) {
                                            onSeatSelected(r, c)
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Display seat legend
                    SeatLegend()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selected seat info
        selectedSeatState?.let { coordinate ->
            Text(
                text = "Selected Seat: ${coordinate.toDisplayId()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Submit button
            Button(
                onClick = onSubmitSeatSelection,
                enabled = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Confirm Seat Selection")
            }
        }
    }
}

@Composable
fun SeatBox(
    row: Int,
    column: Int,
    isOccupied: Boolean,
    isSelected: Boolean,
    isStudentSeat: Boolean,
    onClick: (Int, Int) -> Unit
) {
    val backgroundColor = when {
        isStudentSeat -> Color.Green // Current student's seat: GREEN
        isOccupied -> Color.Red // Seats taken by other students: RED
        else -> Color.Gray // Available seats: GRAY
    }
    
    val textColor = when {
        isStudentSeat || isOccupied -> Color.White
        else -> Color.Black
    }
    
    val borderColor = if (isSelected) Color.Blue else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(4.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(4.dp)
            .clickable(enabled = !isOccupied || isStudentSeat) { // Only allow clicking if seat is not occupied or is student's own seat
                onClick(row, column)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${row},${column}",
            fontSize = 10.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SeatLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Legend:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = Color.Gray.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Available",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Taken by others",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = Color.Gray.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Selected",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = Color(0xFF34C759),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Your Seat",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TeacherInfoCard(section: Section) {
    // Log the section details for debugging
    val schedule = section.schedule
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Teacher profile picture
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                // Load teacher profile image if available, otherwise show placeholder
                val teacherProfileUrl = ""  // Teacher model doesn't have profile URL
                if (false) { // Disabled since profile picture feature isn't implemented yet
                    Image(
                        painter = rememberAsyncImagePainter(teacherProfileUrl),
                        contentDescription = "Teacher Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstInitial = section.teacher?.firstName?.firstOrNull()?.toString().orEmpty()
                        val lastInitial = section.teacher?.lastName?.firstOrNull()?.toString().orEmpty()
                        Text(
                            text = "$firstInitial$lastInitial",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Teacher information
            Column {
                val firstName = section.teacher?.firstName.orEmpty()
                val lastName = section.teacher?.lastName.orEmpty()
                Text(
                    text = "$firstName $lastName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = section.course.courseName.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Course schedule
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Schedule",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Check if the schedule is available; display a default message if not
                    Text(
                        text = if (!schedule.isNullOrEmpty()) schedule else "No schedule available",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Displays error state for seat plan with navigation button
 */
@Composable
fun SeatPlanErrorState(
    errorMessage: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading seats: $errorMessage",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show enrollment button if the error is about not being enrolled
        if (errorMessage.contains("not enrolled")) {
            Button(
                onClick = { 
                    // Navigate to enrollment screen
                    navController.navigate(Routes.CLASSES) 
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Go to Classes Screen")
            }
        }
    }
}
