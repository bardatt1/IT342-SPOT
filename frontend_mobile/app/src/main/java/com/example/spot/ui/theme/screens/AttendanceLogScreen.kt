@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> Text("Attendance History", color = Green700, fontWeight = FontWeight.Bold)
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
        Text(
            text = "Error Loading Data",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
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
fun AttendanceContent(
    section: Section,
    attendanceStats: StudentAttendance
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
            Text(
                text = "Attendance Calendar",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Generate monthly attendance grids based on actual data
        val attendanceByMonth = createMonthlyAttendance(attendanceStats.attendanceByDate)
        items(attendanceByMonth) { monthlyAttendance ->
            MonthlyAttendanceGrid(monthlyAttendance)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SectionInfoCard(section: Section) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Green700.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val teacherInitials = section.teacher?.let {
                    val firstName = it.firstName.firstOrNull() ?: ""
                    val lastName = it.lastName.firstOrNull() ?: ""
                    "$firstName$lastName"
                } ?: "NA"
                
                Text(
                    text = teacherInitials,
                    style = MaterialTheme.typography.titleLarge,
                    color = Green700
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = section.schedule ?: "Schedule not available",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                )
                
                Text(
                    text = section.teacher?.name ?: "Instructor not assigned",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                
                Text(
                    text = section.course.courseName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark.copy(alpha = 0.7f))
                )
            }
        }
    }
}

@Composable
fun AttendanceSummaryCard(attendanceStats: StudentAttendance) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Green700),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Attendance Summary",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Classes",
                    value = attendanceStats.totalClassDays.toString(),
                    textColor = Color.White
                )
                
                StatItem(
                    label = "Present",
                    value = attendanceStats.daysPresent.toString(),
                    textColor = Color.White
                )
                
                StatItem(
                    label = "Attendance Rate",
                    value = String.format("%.1f%%", attendanceStats.attendanceRate * 100),
                    textColor = Color.White
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = textColor.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
fun MonthlyAttendanceGrid(monthlyAttendance: MonthlyAttendance) {
    Column {
        // Month header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green700)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthlyAttendance.month.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Day headers (M, W, F)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Attendance grid
        val entries = monthlyAttendance.entries
        if (entries.isEmpty()) {
            Text(
                text = "No attendance data available",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                modifier = Modifier.padding(8.dp)
            )
            return@Column
        }

        val rows = (entries.size + 6) / 7 // Ceiling division to determine number of rows
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    if (index < entries.size) {
                        val entry = entries[index]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (entry.status) {
                                        AttendanceStatus.PRESENT -> Green500
                                        AttendanceStatus.ABSENT -> Color.Red
                                        AttendanceStatus.LATE -> Color(0xFFFFB300) // Amber
                                        AttendanceStatus.NO_CLASS -> Color.Gray
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entry.date,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (entry.status == AttendanceStatus.NO_CLASS) 
                                        Color.White.copy(alpha = 0.7f) else Color.White
                                )
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// Helper function to create monthly attendance from attendanceByDate map
private fun createMonthlyAttendance(attendanceByDate: Map<String, Boolean>): List<MonthlyAttendance> {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    
    // Group dates by month-year
    val attendanceByMonth = attendanceByDate.keys
        .mapNotNull { dateStr ->
            try {
                dateStr to LocalDate.parse(dateStr, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }
        .groupBy { (_, date) -> 
            YearMonth.from(date).format(monthFormatter)
        }
    
    return attendanceByMonth.map { (month, dates) ->
        val entries = dates.map { (dateStr, date) ->
            val status = if (attendanceByDate[dateStr] == true) {
                AttendanceStatus.PRESENT
            } else {
                AttendanceStatus.ABSENT
            }
            
            AttendanceEntry(
                date = date.format(dayFormatter),
                dayOfWeek = date.dayOfWeek.toString().take(1),
                status = status
            )
        }.sortedBy { it.date }
        
        MonthlyAttendance(month = month, entries = entries)
    }.sortedBy { it.month }
}