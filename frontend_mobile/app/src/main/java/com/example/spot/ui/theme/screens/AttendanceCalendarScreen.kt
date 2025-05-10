package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
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
import com.example.spot.model.AttendanceDayItem
import com.example.spot.model.AttendanceStatus
import com.example.spot.model.Section
import com.example.spot.model.SectionSchedule
import com.example.spot.model.StudentAttendance
import com.example.spot.ui.theme.Green700
import com.example.spot.viewmodel.AttendanceViewModel
import com.example.spot.viewmodel.SectionAttendanceState
import com.example.spot.viewmodel.SectionState
import com.example.spot.viewmodel.SectionViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCalendarScreen(
    navController: NavController,
    sectionId: Long,
    sectionViewModel: SectionViewModel = viewModel(),
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    val sectionState by sectionViewModel.sectionState.collectAsState()
    val attendanceState by attendanceViewModel.sectionAttendanceState.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    // Load section and attendance data
    LaunchedEffect(sectionId) {
        sectionViewModel.fetchSectionById(sectionId)
        attendanceViewModel.loadSectionAttendanceHistory(sectionId)
    }
    
    // Clean up when leaving screen
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
                        is SectionState.Success -> {
                            val section = (sectionState as SectionState.Success).section
                            Text(
                                "${section.course.courseCode} - ${section.sectionName} Attendance", 
                                color = Green700, 
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                        }
                        else -> Text("Attendance Calendar", color = Green700, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 16.dp)
        ) {
            when {
                sectionState is SectionState.Loading || attendanceState is SectionAttendanceState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Green700
                    )
                }
                
                sectionState is SectionState.Error -> {
                    CalendarErrorContent(
                        message = (sectionState as SectionState.Error).message,
                        onRetry = { sectionViewModel.fetchSectionById(sectionId) }
                    )
                }
                
                attendanceState is SectionAttendanceState.Error -> {
                    // Only show as error if it's not actually a success message
                    val errorMessage = (attendanceState as SectionAttendanceState.Error).message
                    
                    if (errorMessage.contains("success", ignoreCase = true)) {
                        // If it's actually a success message but was incorrectly categorized as error
                        // And we know that sectionState must be Success here otherwise we'd hit the previous condition
                        val section = (sectionState as SectionState.Success).section
                        EmptyAttendanceContent(
                            section = section,
                            onRetry = { attendanceViewModel.loadSectionAttendanceHistory(sectionId) }
                        )
                    } else {
                        CalendarErrorContent(
                            message = errorMessage,
                            onRetry = { attendanceViewModel.loadSectionAttendanceHistory(sectionId) }
                        )
                    }
                }
                
                sectionState is SectionState.Success && attendanceState is SectionAttendanceState.Success -> {
                    val section = (sectionState as SectionState.Success).section
                    val attendanceStats = (attendanceState as SectionAttendanceState.Success).studentAttendance
                    
                    if (attendanceStats.attendanceByDate.isEmpty()) {
                        EmptyAttendanceContent(
                            section = section,
                            onRetry = { attendanceViewModel.loadSectionAttendanceHistory(sectionId) }
                        )
                    } else {
                        AttendanceCalendarContent(
                            section = section,
                            attendanceStats = attendanceStats,
                            currentMonth = currentMonth,
                            onMonthChange = { currentMonth = it }
                        )
                    }
                }
                
                else -> {
                    // Idle state or unexpected state
                    Text(
                        "Ready to load attendance data",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarErrorContent(
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
fun AttendanceCalendarContent(
    section: Section,
    attendanceStats: StudentAttendance,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // Class info card
        CalendarSectionInfoCard(section)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Attendance statistics
        CalendarAttendanceSummaryCard(attendanceStats)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Month navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onMonthChange(currentMonth.minusMonths(1)) }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = Green700
                )
            }
            
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green700
            )
            
            IconButton(
                onClick = { onMonthChange(currentMonth.plusMonths(1)) }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = Green700
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar legend
        CalendarLegend()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
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
                    .padding(12.dp)
            ) {
                // Days of week header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in listOf("M", "T", "W", "T", "F", "S", "S")) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar days grid
                CalendarGrid(
                    currentMonth = currentMonth,
                    attendanceByDate = attendanceStats.attendanceByDate,
                    attendanceData = attendanceStats.attendanceDetails
                )
            }
        }
    }
}

@Composable
fun CalendarSectionInfoCard(section: Section) {
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
        }
    }
}

@Composable
fun CalendarAttendanceSummaryCard(attendanceStats: StudentAttendance) {
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
                text = "Attendance Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green700,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Present count
                CalendarStatItem(
                    label = "Present",
                    value = "${attendanceStats.presentCount}",
                    textColor = Color(0xFF4CAF50) // Green
                )
                
                // Absent count
                CalendarStatItem(
                    label = "Absent",
                    value = "${attendanceStats.absentCount}",
                    textColor = Color(0xFFF44336) // Red
                )
                
                // Attendance rate
                CalendarStatItem(
                    label = "Rate",
                    value = "${attendanceStats.attendanceRate}%",
                    textColor = Green700
                )
            }
        }
    }
}

@Composable
fun CalendarStatItem(label: String, value: String, textColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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
fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = Color(0xFF4CAF50), label = "Present")
        LegendItem(color = Color(0xFFF44336), label = "Absent")
        LegendItem(color = Color.LightGray, label = "No Class")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    attendanceByDate: Map<String, Boolean>,
    attendanceData: Map<String, StudentAttendance.AttendanceDetail>? = null
) {
    val calendarDays = createCalendarDays(currentMonth, attendanceByDate, attendanceData)
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(calendarDays) { day ->
            CalendarDay(day)
        }
    }
}

@Composable
fun CalendarDay(day: AttendanceDayItem) {
    // Generate a background color based on the attendance status
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                color = when (day.attendanceStatus) {
                    AttendanceStatus.PRESENT -> Color(0xFF4CAF50).copy(alpha = 0.8f) // Green
                    AttendanceStatus.ABSENT -> Color(0xFFF44336).copy(alpha = 0.8f)  // Red
                    AttendanceStatus.LATE -> Color(0xFFFF9800).copy(alpha = 0.8f)    // Orange
                    AttendanceStatus.UPCOMING -> Color(0xFF2196F3).copy(alpha = 0.2f) // Light Blue
                    AttendanceStatus.NOT_RECORDED -> Color(0xFF9E9E9E).copy(alpha = 0.2f) // Gray
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when (day.attendanceStatus) {
                AttendanceStatus.PRESENT, AttendanceStatus.ABSENT, AttendanceStatus.LATE -> Color.White
                else -> if (!day.isCurrentMonth) Color.LightGray else Color.DarkGray
            },
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Helper function to create calendar day items
 */
private fun createCalendarDays(
    yearMonth: YearMonth,
    attendanceByDate: Map<String, Boolean>,
    attendanceData: Map<String, StudentAttendance.AttendanceDetail>? = null
): List<AttendanceDayItem> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    val firstOfMonth = yearMonth.atDay(1)
    val lastOfMonth = yearMonth.atEndOfMonth()
    
    // Determine the start day (previous month days to show)
    var startDay = firstOfMonth
    while (startDay.dayOfWeek.value != 1) { // Start from Monday (1)
        startDay = startDay.minusDays(1)
    }
    
    // Determine the end day (next month days to show)
    var endDay = lastOfMonth
    while (endDay.dayOfWeek.value != 7) { // End with Sunday (7)
        endDay = endDay.plusDays(1)
    }
    
    val days = mutableListOf<AttendanceDayItem>()
    var currentDay = startDay
    
    while (!currentDay.isAfter(endDay)) {
        val isInCurrentMonth = !currentDay.isBefore(firstOfMonth) && !currentDay.isAfter(lastOfMonth)
        val dateStr = currentDay.format(formatter)
        
        // Determine attendance status
        val status = if (isInCurrentMonth) {
            when {
                attendanceByDate.containsKey(dateStr) -> {
                    val isPresent = attendanceByDate[dateStr] ?: false
                    
                    if (isPresent) {
                        // Check if we have detailed information about attendance time
                        val detail = attendanceData?.get(dateStr)
                        if (detail != null && detail.startTime != null && detail.scheduleStartTime != null &&
                            detail.startTime.isAfter(detail.scheduleStartTime)) {
                            AttendanceStatus.LATE
                        } else {
                            AttendanceStatus.PRESENT
                        }
                    } else {
                        AttendanceStatus.ABSENT
                    }
                }
                currentDay.isAfter(today) -> AttendanceStatus.UPCOMING
                else -> AttendanceStatus.NOT_RECORDED
            }
        } else {
            AttendanceStatus.NOT_APPLICABLE
        }
        
        // Get attendance detail for this date if available
        val detail = attendanceData?.get(dateStr)
        
        days.add(
            AttendanceDayItem(
                date = currentDay,
                isCurrentMonth = isInCurrentMonth,
                attendanceStatus = status,
                isSelected = false, // Can be updated based on selection state
                detail = detail
            )
        )
        
        currentDay = currentDay.plusDays(1)
    }
    
    return days
}

@Composable
fun EmptyAttendanceContent(
    section: Section,
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
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "No Attendance",
                    tint = Green700,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                
                Text(
                    text = "No Attendance Records",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "No attendance data found for ${section.course.courseCode} - ${section.sectionName}",
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
                    Text("Refresh")
                }
            }
        }
    }
}
