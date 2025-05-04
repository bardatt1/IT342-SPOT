@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.Section
import com.example.spot.model.StudentAttendance
import com.example.spot.ui.theme.*
import com.example.spot.viewmodel.AttendanceViewModel
import com.example.spot.viewmodel.SectionAttendanceState
import com.example.spot.viewmodel.SectionViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceLogScreen(
    navController: NavController,
    sectionId: Long?,
    sectionViewModel: SectionViewModel = viewModel(),
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    val sectionState by sectionViewModel.sectionState.collectAsState()
    val attendanceState by attendanceViewModel.sectionAttendanceState.collectAsState()
    
    LaunchedEffect(sectionId) {
        if (sectionId != null) {
            sectionViewModel.fetchSectionById(sectionId)
            attendanceViewModel.loadSectionAttendanceHistory(sectionId)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            sectionViewModel.resetStates()
            attendanceViewModel.resetStates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    when (sectionState) {
                        is com.example.spot.viewmodel.SectionState.Success -> {
                            val section = (sectionState as com.example.spot.viewmodel.SectionState.Success).section
                            Text(
                                "${section.course.courseCode} - ${section.sectionName}", 
                                color = Green700, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontSize = 18.sp
                            )
                        }
                        else -> Text("Attendance History", color = Green700, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Green700
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Green700
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x0D215F47), // from-[#215f47]/5
                            Color.White,       // via-white
                            Color(0x1A215F47)  // to-[#215f47]/10
                        )
                    )
                )
        ) {
            when {
                sectionState is com.example.spot.viewmodel.SectionState.Loading || 
                attendanceState is SectionAttendanceState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Green700
                    )
                }
                
                sectionState is com.example.spot.viewmodel.SectionState.Error -> {
                    ErrorContent(
                        message = (sectionState as com.example.spot.viewmodel.SectionState.Error).message,
                        onRetry = { 
                            if (sectionId != null) {
                                sectionViewModel.fetchSectionById(sectionId)
                            }
                        }
                    )
                }
                
                attendanceState is SectionAttendanceState.Error -> {
                    ErrorContent(
                        message = (attendanceState as SectionAttendanceState.Error).message,
                        onRetry = { 
                            if (sectionId != null) {
                                attendanceViewModel.loadSectionAttendanceHistory(sectionId)
                            }
                        }
                    )
                }
                
                sectionState is com.example.spot.viewmodel.SectionState.Success && 
                attendanceState is SectionAttendanceState.Success -> {
                    val section = (sectionState as com.example.spot.viewmodel.SectionState.Success).section
                    val attendanceStats = (attendanceState as SectionAttendanceState.Success).studentAttendance
                    
                    AttendanceContent(
                        section = section,
                        attendanceStats = attendanceStats
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Error Loading Data",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green700
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun AttendanceContent(
    section: Section,
    attendanceStats: StudentAttendance
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Class details
        item {
            SectionInfoCard(section)
        }

        // Attendance stats summary
        item {
            AttendanceSummaryCard(attendanceStats)
        }

        // Attendance Log title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Attendance Calendar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Green700
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Create a monthly view of attendance
                    val monthlyAttendance = createMonthlyAttendance(attendanceStats.attendanceByDate)
                    if (monthlyAttendance.isEmpty()) {
                        Text(
                            text = "No attendance records found for this section",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        for (month in monthlyAttendance) {
                            MonthlyAttendanceGrid(month)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionInfoCard(section: Section) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0x33215F47))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${section.course.courseCode} - ${section.sectionName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Display dynamically fetched schedule information
            val scheduleText = section.getScheduleDisplay()
            scheduleText.split("\n").forEach { scheduleLine ->
                Text(
                    text = "• $scheduleLine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Display teacher information
            Text(
                text = "Teacher: ${section.teacher?.name ?: "Not Assigned"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun AttendanceSummaryCard(attendanceStats: StudentAttendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0x33215F47))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Attendance Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green700,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Present count
                StatItem(
                    label = "Present",
                    value = "${attendanceStats.presentCountForUI}",
                    textColor = Color(0xFF4CAF50) // Green
                )
                
                // Absent count
                StatItem(
                    label = "Absent",
                    value = "${attendanceStats.absentCountForUI}",
                    textColor = Color(0xFFF44336) // Red
                )
                
                // Attendance rate
                StatItem(
                    label = "Rate",
                    value = "${attendanceStats.attendanceRateForUI}%",
                    textColor = Green700
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, textColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun MonthlyAttendanceGrid(monthlyAttendance: AttendanceMonthlyDisplay) {
    val yearMonth = monthlyAttendance.yearMonth
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Month header
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green700,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Days table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .padding(8.dp)
        ) {
            // Loop through the days in this month
            for (day in monthlyAttendance.attendanceDays) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Text(
                        text = day.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(100.dp)
                    )
                    
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (day.present) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Status text
                    Text(
                        text = if (day.present) "Present" else "Absent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (day.present) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

// Data class for monthly attendance display
data class AttendanceMonthlyDisplay(
    val yearMonth: YearMonth,
    val attendanceDays: List<AttendanceDay>
)

// Data class for individual attendance day
data class AttendanceDay(
    val date: LocalDate,
    val present: Boolean,
    val formattedDate: String = date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
)

// Helper function to create monthly attendance from attendanceByDate map
fun createMonthlyAttendance(attendanceByDate: Map<String, Boolean>): List<AttendanceMonthlyDisplay> {
    val result = mutableListOf<AttendanceMonthlyDisplay>()
    
    if (attendanceByDate.isEmpty()) {
        return result
    }
    
    // Group attendance data by month
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val attendanceDays = attendanceByDate.map { (dateStr, present) ->
        val date = LocalDate.parse(dateStr, dateFormatter)
        AttendanceDay(date = date, present = present)
    }.sortedBy { it.date }
    
    // Group by year-month
    val groupedByMonth = attendanceDays.groupBy { YearMonth.from(it.date) }
    
    // Create monthly attendance objects
    for ((yearMonth, days) in groupedByMonth) {
        result.add(
            AttendanceMonthlyDisplay(
                yearMonth = yearMonth,
                attendanceDays = days
            )
        )
    }
    
    return result.sortedByDescending { it.yearMonth }
}