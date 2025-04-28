@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.Seat
import com.example.spot.model.Section
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.*
import com.example.spot.viewmodel.SeatViewModel
import com.example.spot.viewmodel.SeatsState
import com.example.spot.viewmodel.StudentSeatState
import com.example.spot.viewmodel.SeatPickState
import com.example.spot.viewmodel.SectionViewModel
import com.example.spot.viewmodel.SectionDetailsState
import kotlinx.coroutines.launch

@Composable
fun ClassViewScreen(
    navController: NavController,
    classCode: String,
    seatViewModel: SeatViewModel = viewModel(),
    sectionViewModel: SectionViewModel = viewModel()
) {
    val sectionState by sectionViewModel.sectionDetailsState.collectAsState()
    val seatsState by seatViewModel.seatsState.collectAsState()
    val studentSeatState by seatViewModel.studentSeatState.collectAsState()
    val pickSeatState by seatViewModel.pickSeatState.collectAsState()
    
    // Section details from the classCode (enrollment key)
    LaunchedEffect(classCode) {
        sectionViewModel.getSectionByEnrollmentKey(classCode)
    }
    
    // Handle section state
    when (sectionState) {
        is SectionDetailsState.Loading -> {
            LoadingScreen("Loading class details...")
        }
        is SectionDetailsState.Error -> {
            val errorMessage = (sectionState as SectionDetailsState.Error).message
            ErrorScreen(errorMessage) {
                sectionViewModel.getSectionByEnrollmentKey(classCode)
            }
        }
        is SectionDetailsState.Success -> {
            val section = (sectionState as SectionDetailsState.Success).section
            
            // Load seats and check if student has a seat
            LaunchedEffect(section.id) {
                seatViewModel.loadSeatsForSection(section.id)
                seatViewModel.loadStudentSeat(section.id)
            }
            
            // Determine if we need to show seat selection or class view
            when (studentSeatState) {
                is StudentSeatState.Loading -> {
                    LoadingScreen("Checking your seat assignment...")
                }
                is StudentSeatState.Error -> {
                    // No seat assigned, show seat selection
                    when (seatsState) {
                        is SeatsState.Loading -> {
                            LoadingScreen("Loading available seats...")
                        }
                        is SeatsState.Error -> {
                            val errorMessage = (seatsState as SeatsState.Error).message
                            ErrorScreen(errorMessage) {
                                seatViewModel.loadSeatsForSection(section.id)
                            }
                        }
                        is SeatsState.Success -> {
                            val seats = (seatsState as SeatsState.Success).seats
                            ChooseSeatScreen(
                                navController = navController,
                                section = section,
                                seats = seats,
                                pickSeatState = pickSeatState,
                                onPickSeat = { row, column ->
                                    seatViewModel.pickSeat(section.id, row, column)
                                }
                            )
                        }
                        is SeatsState.Idle -> {
                            // Do nothing, waiting for LaunchedEffect
                        }
                    }
                }
                is StudentSeatState.Success -> {
                    // Student has a seat, show class view
                    val studentSeat = (studentSeatState as StudentSeatState.Success).seat
                    
                    when (seatsState) {
                        is SeatsState.Loading -> {
                            LoadingScreen("Loading class seats...")
                        }
                        is SeatsState.Error -> {
                            val errorMessage = (seatsState as SeatsState.Error).message
                            ErrorScreen(errorMessage) {
                                seatViewModel.loadSeatsForSection(section.id)
                            }
                        }
                        is SeatsState.Success -> {
                            ClassViewContent(
                                navController = navController,
                                section = section,
                                studentSeat = studentSeat
                            )
                        }
                        is SeatsState.Idle -> {
                            // Do nothing, waiting for LaunchedEffect
                        }
                    }
                }
                is StudentSeatState.Idle -> {
                    // Do nothing, waiting for LaunchedEffect
                }
            }
        }
        is SectionDetailsState.Idle -> {
            // Do nothing, waiting for LaunchedEffect
        }
    }
}

@Composable
fun LoadingScreen(message: String = "Loading...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Green700)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = TextDark)
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = TextDark,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Green700)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ChooseSeatScreen(
    navController: NavController,
    section: Section,
    seats: List<Seat>,
    pickSeatState: SeatPickState,
    onPickSeat: (Int, Int) -> Unit
) {
    var selectedSeatPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Handle pick seat state
    LaunchedEffect(pickSeatState) {
        when (pickSeatState) {
            is SeatPickState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Seat selected successfully!")
                    // Refresh the student seat
                    // This could navigate back or refresh data
                }
            }
            is SeatPickState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        pickSeatState.message,
                        actionLabel = "Retry"
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(section.courseName, color = Green700, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Green700
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section details
            Text(
                text = "Section: ${section.sectionNumber}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "Instructor: ${section.instructorName}",
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Schedule: ${section.schedule}",
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Choose seat instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Choose Your Seat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Green700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Select an available seat from the layout below. This will be your assigned seat for the entire semester.",
                        color = TextDark
                    )
                }
            }
            
            // Seat grid
            // This is simplified; in a real app, you'd want to organize seats by row and column
            Spacer(modifier = Modifier.height(16.dp))
            
            // Group seats by row
            val seatsByRow = seats.groupBy { it.row }
            val rowCount = seatsByRow.keys.maxOrNull() ?: 0
            
            // Front of classroom label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("FRONT", fontWeight = FontWeight.Bold, color = TextDark)
            }
            
            // Seat grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 1..rowCount) {
                    val rowSeats = seatsByRow[row] ?: emptyList()
                    val sortedRowSeats = rowSeats.sortedBy { it.column }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Display row number
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R$row", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        
                        // Display seats in this row
                        sortedRowSeats.forEach { seat ->
                            SeatButton(
                                seat = seat,
                                isSelected = selectedSeatPosition?.let { it.first == seat.row && it.second == seat.column } == true,
                                onSeatSelected = { 
                                    if (!seat.isTaken) {
                                        selectedSeatPosition = Pair(seat.row, seat.column)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
            
            // Seat selection confirmation
            Button(
                onClick = {
                    selectedSeatPosition?.let { (row, column) ->
                        onPickSeat(row, column)
                    }
                },
                enabled = selectedSeatPosition != null && pickSeatState !is SeatPickState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green700,
                    disabledContainerColor = Color.Gray
                )
            ) {
                if (pickSeatState is SeatPickState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm Seat Selection")
                }
            }
        }
    }
}

@Composable
fun ClassViewContent(
    navController: NavController,
    section: Section,
    studentSeat: Seat
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(section.courseName, color = Green700, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Class details
            Text(
                text = "Section: ${section.sectionNumber}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "Instructor: ${section.instructorName}",
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Schedule: ${section.schedule}",
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Your seat information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Your Assigned Seat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Green700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Row: ${studentSeat.row}, Column: ${studentSeat.column}",
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                }
            }
            
            // Class options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Class Options",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Green700
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Scan attendance QR code
                    Button(
                        onClick = { navController.navigate(Routes.QR_SCANNER) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Green700)
                    ) {
                        Text("Scan Attendance QR Code")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // View attendance log
                    OutlinedButton(
                        onClick = { navController.navigate(Routes.ATTENDANCE_LOG) },
                        modifier = Modifier.fillMaxWidth(),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text("View Attendance Log", color = Green700)
                    }
                }
            }
            
            // Seat map preview (simple visualization)
            Text(
                "Class Seat Map",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Green700,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Simplified seat map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Tap to view full seat map",
                    color = TextDark,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SeatButton(
    seat: Seat,
    isSelected: Boolean,
    onSeatSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    isSelected -> Green700
                    seat.isTaken -> Color.Gray
                    else -> Color.White
                }
            )
            .padding(1.dp)
            .clickable(enabled = !seat.isTaken) { onSeatSelected() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "C${seat.column}",
            color = if (isSelected || seat.isTaken) Color.White else TextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}