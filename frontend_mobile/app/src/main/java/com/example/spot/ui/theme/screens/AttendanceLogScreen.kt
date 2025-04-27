@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavController
import com.example.spot.ui.theme.*

@Composable
fun AttendanceLogScreen(
    navController: NavController
) {
    var showSubmitTicketModal by remember { mutableStateOf(false) }

    // Use the hardcoded classData directly
    val classDetails = classData

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(classDetails.code, color = Green700, fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSubmitTicketModal = true },
                containerColor = Green700,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_help),
                    contentDescription = "Submit Ticket"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Class details
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Gray)
                    ) {
                        // Placeholder for instructor image
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = classDetails.schedule,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                        )
                        Text(
                            text = classDetails.instructor,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                        Text(
                            text = classDetails.room,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextDark.copy(alpha = 0.7f))
                        )
                    }
                }
            }

            // Attendance Log title
            item {
                Text(
                    text = "Attendance Log",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark

                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Monthly attendance grids
            sampleAttendanceLog.forEach { monthlyAttendance ->
                item {
                    MonthlyAttendanceGrid(monthlyAttendance)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Show Submit Ticket modal if triggered
    if (showSubmitTicketModal) {
        SubmitTicketModal(
            onDismiss = { showSubmitTicketModal = false },
            onSubmit = { inquiry ->
                println("Submitted inquiry: $inquiry")
                showSubmitTicketModal = false
            }
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
            Text(
                text = "M",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "W",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "F",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
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

        val rows = (entries.size + 2) / 3 // Ceiling division to determine number of rows
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
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
                                        AttendanceStatus.LATE -> Color.Yellow
                                        AttendanceStatus.NO_CLASS -> Color.Gray
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entry.date,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
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

@Composable
fun SubmitTicketModal(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var inquiryText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submit Ticket",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Close",
                        tint = TextDark
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Submit a ticket here if you have concerns or inquiries regarding your attendance log",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TextField(
                    value = inquiryText,
                    onValueChange = { inquiryText = it },
                    placeholder = { Text("Type inquiry here") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Green100,
                        unfocusedContainerColor = Green100,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(inquiryText) },
                colors = ButtonDefaults.buttonColors(containerColor = Green700),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("SUBMIT", color = Color.White)
            }
        },
        dismissButton = {},
        containerColor = Color.White
    )
}