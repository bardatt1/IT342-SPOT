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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
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

// Color scheme matching the web design
val SpotPrimaryColor = Color(0xFF215F47)
val SpotPrimaryLightColor = Color(0x1A215F47) // 10% opacity
val SpotPrimaryMediumColor = Color(0x33215F47) // 20% opacity
val SpotPrimaryBorderColor = Color(0x1A215F47) // 10% opacity
val SpotLightBackground = Color(0x0D215F47) // 5% opacity
val SpotDisabledColor = Color(0xFF9E9E9E)
val SpotTextColor = Color(0xFF333333)
val SpotTextSecondaryColor = Color(0xFF666666)
val SpotWarningColor = Color(0xFFFFA000)
val SpotWarningBackgroundColor = Color(0xFFFFF8E1)
val SpotSuccessColor = Color(0xFF4CAF50)

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
    
    // State for displaying messages
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
    
    // Handle seat picking state
    LaunchedEffect(pickSeatState) {
        when (pickSeatState) {
            is SeatPlanPickState.Success -> {
                // Show success message
                successMessage = "Seat selection successful!"
                showSuccessMessage = true
                showErrorMessage = false
                
                // Show toast
                Toast.makeText(
                    context,
                    "Seat successfully selected!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is SeatPlanPickState.Error -> {
                // Show error message
                val errorState = pickSeatState as SeatPlanPickState.Error
                errorMessage = errorState.message
                showErrorMessage = true
                showSuccessMessage = false
                
                // Show toast
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Grid3x3,
                            contentDescription = null,
                            tint = SpotPrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            val titleText = if (sectionState is SectionState.Success) {
                                val successState = sectionState as SectionState.Success
                                "${successState.section.course.courseCode} - ${successState.section.sectionName}"
                            } else {
                                "Seat Plan"
                            }
                            Text(
                                text = titleText, 
                                fontWeight = FontWeight.Bold,
                                color = SpotPrimaryColor
                            )
                            Text(
                                text = "Seat Selection",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotTextSecondaryColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = SpotPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        // Main content
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Success message
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFAED581))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SpotSuccessColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Success",
                                fontWeight = FontWeight.Bold,
                                color = SpotSuccessColor
                            )
                            Text(
                                text = successMessage,
                                color = SpotTextColor
                            )
                        }
                    }
                }
            }
            
            // Error message
            if (showErrorMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Error",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = errorMessage,
                                color = SpotTextColor
                            )
                        }
                    }
                }
            }

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
                            // Section Info Card
                            TeacherInfoCard(section = currentSection ?: successState.section)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Main seat plan content using grid layout
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
                                CircularProgressIndicator(color = SpotPrimaryColor)
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
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = SpotPrimaryColor)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading seat plan...",
                color = SpotTextSecondaryColor
            )
        }
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
    // Create a scroll state for the column
    val scrollState = rememberScrollState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SpotPrimaryBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Grid3x3,
                    contentDescription = null,
                    tint = SpotPrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Seat Map",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SpotPrimaryColor
                )
            }
            
            Text(
                text = "Arrange your seating in the classroom",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotTextSecondaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Front of classroom badge
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = SpotLightBackground,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Front of Classroom",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotPrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Teacher's desk at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(40.dp)
                    .background(
                        color = SpotPrimaryColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Teacher's Desk", 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Responsive seat grid layout
            val rowCount = 5
            val colCount = 6
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SpotPrimaryBorderColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0 until rowCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until colCount) {
                                // Find if this seat is taken by looking through the seats list
                                val seat = seats.find { 
                                    it.row == row && it.column == col 
                                }
                                
                                // Determine if this is the student's current seat
                                val isStudentSeat = seat?.student?.id == userId
                                val isOccupiedByOther = seat?.student != null && !isStudentSeat
                                
                                Box(
                                    modifier = Modifier.weight(1f, fill = true),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SeatBox(
                                        row = row,
                                        column = col,
                                        isOccupied = isOccupiedByOther,
                                        isSelected = selectedSeatState?.let { 
                                            it.row == row && it.column == col 
                                        } ?: false,
                                        isStudentSeat = isStudentSeat,
                                        onClick = { r, c -> 
                                            if (seat?.student == null || isStudentSeat) {
                                                onSeatSelected(r, c)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display seat legend
            SeatLegend()
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Selected seat info
    selectedSeatState?.let { coordinate ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpotLightBackground),
            border = BorderStroke(1.dp, SpotPrimaryBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selection Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SpotPrimaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Selected seat card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SpotPrimaryBorderColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Selected Seat",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotPrimaryColor.copy(alpha = 0.7f)
                            )
                            
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = SpotPrimaryColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = "Row ${coordinate.row + 1}, Column ${coordinate.column + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            // Show warning if seat is occupied
                            val seat = seats.find { 
                                it.row == coordinate.row && it.column == coordinate.column 
                            }
                            
                            if (seat?.student != null && seat.student.id != userId) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    color = SpotWarningBackgroundColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = SpotWarningColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = "This seat is already occupied by ${seat.student.firstName} ${seat.student.lastName}. Selecting will override the current assignment.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SpotWarningColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onSubmitSeatSelection,
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotPrimaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Confirm Seat Selection")
                }
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
        isStudentSeat -> SpotPrimaryLightColor
        isOccupied -> SpotPrimaryLightColor
        else -> Color.White
    }
    
    val borderColor = when {
        isSelected -> SpotPrimaryColor
        isStudentSeat || isOccupied -> SpotPrimaryBorderColor
        else -> SpotPrimaryBorderColor.copy(alpha = 0.3f)
    }
    
    val textColor = when {
        isStudentSeat -> SpotPrimaryColor
        isOccupied -> SpotPrimaryColor
        else -> SpotTextSecondaryColor
    }
    
    val borderWidth = if (isSelected) 2.dp else 1.dp
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(4.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(2.dp)
            .clickable { onClick(row, column) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isOccupied || isStudentSeat) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (isStudentSeat) SpotPrimaryColor.copy(alpha = 0.2f) else SpotPrimaryColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isStudentSeat) Icons.Default.Person else Icons.Default.PersonOutline,
                        contentDescription = null,
                        tint = SpotPrimaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = SpotTextSecondaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "${row+1}-${column+1}",
                fontSize = 10.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SeatLegend() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = BorderStroke(1.dp, SpotPrimaryBorderColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Legend:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SpotPrimaryColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Available seat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.dp, SpotPrimaryBorderColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotTextSecondaryColor
                    )
                }
                
                // Your seat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.dp, SpotPrimaryColor, RoundedCornerShape(4.dp))
                            .background(SpotPrimaryLightColor, RoundedCornerShape(4.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Your Seat",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotTextSecondaryColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Occupied seat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.dp, SpotPrimaryBorderColor, RoundedCornerShape(4.dp))
                            .background(SpotPrimaryLightColor, RoundedCornerShape(4.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Occupied",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotTextSecondaryColor
                    )
                }
                
                // Selected seat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(2.dp, SpotPrimaryColor, RoundedCornerShape(4.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotTextSecondaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherInfoCard(section: Section) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SpotPrimaryBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Course info with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course code and name
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${section.course.courseCode} - ${section.sectionName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SpotPrimaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = section.course.courseName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = SpotTextColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Teacher info with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SpotPrimaryMediumColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = section.teacher?.firstName?.firstOrNull()?.toString() ?: "T",
                        color = SpotPrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = "Teacher:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotTextSecondaryColor
                    )
                    
                    Text(
                        text = "${section.teacher?.firstName ?: "Unknown"} ${section.teacher?.lastName ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SpotTextColor
                    )
                }
            }
            
            // Schedule information - display as simple string since we don't have detailed schedule objects
            if (section.schedule != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                HorizontalDivider(
                    thickness = 1.dp,
                    color = SpotPrimaryBorderColor
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Schedule display
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Schedule",
                        tint = SpotPrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Schedule:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotTextSecondaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Display schedule string directly
                Text(
                    text = section.schedule,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotTextColor
                )
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
