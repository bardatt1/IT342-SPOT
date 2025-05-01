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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.AttendanceDayItem
import com.example.spot.model.AttendanceStatus
import com.example.spot.model.Section
import com.example.spot.model.StudentAttendance
import com.example.spot.ui.theme.Green700
import com.example.spot.viewmodel.AttendanceViewModel
import com.example.spot.viewmodel.SectionAttendanceState
import com.example.spot.viewmodel.SectionState
import com.example.spot.viewmodel.SectionViewModel
import java.time.LocalDate
import java.time.LocalTime
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> Text("Attendance Calendar", color = Green700, fontWeight = FontWeight.Bold)
                    }
                },
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
        // Change from showing "Error" to showing a more appropriate title based on the message
        val title = if (message.contains("success", ignoreCase = true)) {
            "No Data Available"
        } else {
            "Error"
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = if (title == "Error") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
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

@Composable
fun AttendanceCalendarContent(
    section: Section,
    attendanceStats: StudentAttendance,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Section info
        CalendarSectionInfoCard(section = section)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Attendance summary
        CalendarAttendanceSummaryCard(attendanceStats = attendanceStats)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Month navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_previous),
                    contentDescription = "Previous Month",
                    tint = Green700
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar",
                    tint = Green700
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = currentMonth.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_next),
                    contentDescription = "Next Month",
                    tint = Green700
                )
            }
        }
        
        // Calendar legend
        CalendarLegend()
        
        // Calendar grid
        CalendarGrid(
            currentMonth = currentMonth,
            attendanceByDate = attendanceStats.attendanceByDate,
            attendanceData = attendanceStats.attendanceData
        )
    }
}

@Composable
fun CalendarSectionInfoCard(section: Section) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = section.course.courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Section: ${section.sectionName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Schedule: ${section.schedule ?: "No schedule set"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CalendarAttendanceSummaryCard(attendanceStats: StudentAttendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Attendance Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalendarStatItem(
                    label = "Total Classes",
                    value = attendanceStats.totalClassDays.toString(),
                    textColor = Color.Black
                )
                
                CalendarStatItem(
                    label = "Present",
                    value = attendanceStats.daysPresent.toString(),
                    textColor = Color(0xFF4CAF50) // Green
                )
                
                CalendarStatItem(
                    label = "Absent",
                    value = (attendanceStats.totalClassDays - attendanceStats.daysPresent).toString(),
                    textColor = Color(0xFFE57373) // Red
                )
                
                CalendarStatItem(
                    label = "Rate",
                    value = "${String.format("%.1f", attendanceStats.attendanceRate * 100)}%",
                    textColor = Color.Black
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
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (day in 1..7) {
            val dayName = java.time.DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            Text(
                text = dayName,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Legend for attendance status colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = Color(0xFF4CAF50), label = "Present") // Green
        LegendItem(color = Color(0xFFFFEB3B), label = "Late") // Yellow
        LegendItem(color = Color(0xFFE57373), label = "Absent") // Red
        LegendItem(color = Color(0xFFEEEEEE), label = "No Class") // Light Gray
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
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    attendanceByDate: Map<String, Boolean>,
    attendanceData: Map<String, com.example.spot.model.StudentAttendance.AttendanceDetail>? = null
) {
    val days = createCalendarDays(currentMonth, attendanceByDate, attendanceData)
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(days) { day ->
            CalendarDay(day)
        }
    }
}

@Composable
fun CalendarDay(day: AttendanceDayItem) {
    val backgroundColor = when (day.status) {
        AttendanceStatus.PRESENT -> Color(0xFF4CAF50) // Green
        AttendanceStatus.LATE -> Color(0xFFFFEB3B) // Yellow
        AttendanceStatus.ABSENT -> Color(0xFFE57373) // Red
        AttendanceStatus.NO_CLASS -> Color(0xFFEEEEEE) // Light Gray
    }
    
    // Determine if this day is today
    val isToday = day.date == LocalDate.now()
    
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) Green700 else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.status == AttendanceStatus.NO_CLASS) Color.Gray else 
                   if (day.status == AttendanceStatus.LATE) Color.Black else Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Helper function to create calendar day items
fun createCalendarDays(
    yearMonth: YearMonth,
    attendanceByDate: Map<String, Boolean>,
    attendanceData: Map<String, com.example.spot.model.StudentAttendance.AttendanceDetail>? = null
): List<AttendanceDayItem> {
    val days = mutableListOf<AttendanceDayItem>()
    
    // Calculate first day of month offset (e.g., if month starts on Wednesday, offset is 2)
    val firstDayOfMonth = yearMonth.atDay(1)
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 // Convert Monday=1 to Sunday=0 system
    
    // Add empty spaces for days before the first day of month
    for (i in 0 until dayOfWeekValue) {
        val prevDate = firstDayOfMonth.minusDays((dayOfWeekValue - i).toLong())
        days.add(
            AttendanceDayItem(
                date = prevDate,
                status = AttendanceStatus.NO_CLASS
            )
        )
    }
    
    // Process actual days in the month
    val daysInMonth = yearMonth.lengthOfMonth()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    for (dayOfMonth in 1..daysInMonth) {
        val date = yearMonth.atDay(dayOfMonth)
        val dateStr = date.format(formatter)
        
        // Determine attendance status - using enhanced logic with time details if available
        var attendanceStatus = AttendanceStatus.NO_CLASS
        var startTime: LocalTime? = null
        var scheduleStartTime: LocalTime? = null
        
        if (attendanceData?.containsKey(dateStr) == true) {
            // Enhanced data available with time details
            val detail = attendanceData[dateStr]
            startTime = detail?.startTime
            scheduleStartTime = detail?.scheduleStartTime
            
            if (detail?.present == true) {
                // Check if student was late (15+ minutes after schedule start)
                attendanceStatus = if (startTime != null && scheduleStartTime != null &&
                    startTime.isAfter(scheduleStartTime.plusMinutes(15))) {
                    AttendanceStatus.LATE
                } else {
                    AttendanceStatus.PRESENT
                }
            } else {
                attendanceStatus = AttendanceStatus.ABSENT
            }
        } else if (attendanceByDate.containsKey(dateStr)) {
            // Basic attendance data without time details
            attendanceStatus = if (attendanceByDate[dateStr] == true) {
                AttendanceStatus.PRESENT
            } else {
                AttendanceStatus.ABSENT
            }
        }
        
        days.add(
            AttendanceDayItem(
                date = date,
                status = attendanceStatus,
                startTime = startTime,
                scheduleStartTime = scheduleStartTime
            )
        )
    }
    
    // Calculate how many days to add to complete the grid (if needed)
    val remainingCells = 42 - days.size // 6 rows * 7 columns = 42 total cells
    if (remainingCells > 0 && remainingCells < 7) {
        val lastDate = yearMonth.atEndOfMonth()
        for (i in 1..remainingCells) {
            val nextDate = lastDate.plusDays(i.toLong())
            days.add(
                AttendanceDayItem(
                    date = nextDate,
                    status = AttendanceStatus.NO_CLASS
                )
            )
        }
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
        CalendarSectionInfoCard(section = section)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Attendance Data",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Student attendance stats retrieved successfully, but there are no attendance records yet for this course.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Green700)
        ) {
            Text("Refresh")
        }
    }
}
