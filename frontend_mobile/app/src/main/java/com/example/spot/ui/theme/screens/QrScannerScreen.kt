@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.R
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.*
import com.example.spot.viewmodel.AttendanceState
import com.example.spot.viewmodel.AttendanceViewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

@Composable
fun QrScannerScreen(
    navController: NavController,
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var sectionId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorType by remember { mutableStateOf<ErrorType>(ErrorType.GENERAL) }
    val context = LocalContext.current

    // Check initial permission state
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Request permission if not granted
    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Observe attendance logging state
    val attendanceState by attendanceViewModel.attendanceState.collectAsState()
    
    LaunchedEffect(attendanceState) {
        when (attendanceState) {
            is AttendanceState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is AttendanceState.Success -> {
                isLoading = false
                errorMessage = null
                // We successfully logged attendance, keep scanResult non-null
                // but make sure sectionId is properly set for navigation
                val attendance = (attendanceState as AttendanceState.Success).attendance
                sectionId = attendance.section.id
            }
            is AttendanceState.Error -> {
                isLoading = false
                val message = (attendanceState as AttendanceState.Error).message
                errorMessage = message
                
                // Determine error type for more specific UI feedback
                errorType = when {
                    // Check for our special marker for duplicate attendance or common duplicate phrases
                    message.contains("[DUPLICATE_ATTENDANCE]") || 
                    message.contains("already marked", ignoreCase = true) || 
                    message.contains("already recorded", ignoreCase = true) || 
                    message.contains("duplicate", ignoreCase = true) -> {
                        // Remove marker if present, but we don't need to modify the state directly
                        ErrorType.DUPLICATE_ATTENDANCE
                    }
                    // Special case for HTTP 400 errors - treat as duplicate attendance
                    message.contains("HTTP 400", ignoreCase = true) -> {
                        // Most likely a duplicate attendance since that's the common 400 error
                        ErrorType.DUPLICATE_ATTENDANCE
                    }
                    message.contains("server", ignoreCase = true) || 
                    message.contains("network", ignoreCase = true) || 
                    message.contains("connection", ignoreCase = true) -> ErrorType.SERVER_ERROR
                    message.contains("invalid", ignoreCase = true) || 
                    message.contains("format", ignoreCase = true) -> ErrorType.INVALID_QR
                    else -> ErrorType.GENERAL
                }
            }
            else -> {
                isLoading = false
            }
        }
    }

    if (scanResult != null) {
        // Show result screen with appropriate success/error state
        QrScanResultScreen(
            onBackToClass = { navController.popBackStack(Routes.CLASSES, inclusive = false) },
            onViewAttendanceLog = { 
                navController.navigate("attendance_calendar/$sectionId") 
            },
            onBackToDashboard = { 
                navController.navigate(Routes.DASHBOARD) {
                    // Pop up to the dashboard so pressing back doesn't return to scanner
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }
            },
            onTryAgain = {
                // Reset state and resume scanning
                scanResult = null
                errorMessage = null
                attendanceViewModel.resetStates()
            },
            isLoading = isLoading,
            errorMessage = errorMessage,
            errorType = errorType
        )
    } else if (hasCameraPermission) {
        // Show QR scanner UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Attendance", color = Green700, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Green700)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // QR scanner camera view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            DecoratedBarcodeView(ctx).apply {
                                decodeContinuous(object : BarcodeCallback {
                                    override fun barcodeResult(result: BarcodeResult) {
                                        val scannedText = result.text
                                        scanResult = scannedText
                                        
                                        // Parse sectionId from QR code
                                        try {
                                            // QR code format: "attend:{sectionId}"
                                            if (scannedText.startsWith("attend:")) {
                                                val id = scannedText.substringAfter("attend:").toLong()
                                                sectionId = id
                                                
                                                // Log attendance via ViewModel
                                                attendanceViewModel.logAttendance(id)
                                            } else {
                                                errorMessage = "Invalid QR code format"
                                                errorType = ErrorType.INVALID_QR
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Invalid QR code: ${e.message}"
                                            errorType = ErrorType.INVALID_QR
                                        }
                                        
                                        pause() // Pause scanning after a result is found
                                    }

                                    override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {}
                                })
                                resume() // Start scanning
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /* Gallery functionality */ }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_gallery),
                            contentDescription = "Gallery",
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Flashlight functionality */ }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_info_details),
                            contentDescription = "Flashlight",
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Camera switch functionality */ }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_camera),
                            contentDescription = "Switch Camera",
                            tint = Color.Gray
                        )
                    }
                }

                // Bottom text
                Text(
                    text = "Scan QR code for attendance",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    } else {
        // Show permission denied message
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.spot_logo),
                contentDescription = "Camera Permission",
                modifier = Modifier.size(64.dp),
                tint = Green700
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextDark
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Camera permission is required to scan QR codes for attendance.",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextDark),
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = Green700),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("Grant Permission", color = Color.White)
            }
        }
    }
    
    // Clean up states when leaving the screen
    DisposableEffect(key1 = Unit) {
        onDispose {
            attendanceViewModel.resetStates()
        }
    }
}

/**
 * Error types for attendance scanning
 */
enum class ErrorType {
    GENERAL,             // Generic error
    SERVER_ERROR,        // Server-side or network error
    DUPLICATE_ATTENDANCE, // Already marked attendance
    INVALID_QR           // Invalid QR format or content
}

@Composable
fun QrScanResultScreen(
    onBackToClass: () -> Unit,
    onViewAttendanceLog: () -> Unit,
    onBackToDashboard: () -> Unit,
    onTryAgain: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    errorType: ErrorType = ErrorType.GENERAL
) {
    // Check if this is a duplicate attendance case (already marked attendance)
    val isDuplicate = errorType == ErrorType.DUPLICATE_ATTENDANCE
    
    // Determine if this is a success state - should be true if:
    // 1. No error message (normal success case)
    // 2. Duplicate attendance (special success case)
    // 3. Message contains "success" or "logged successfully" (API returned success)
    val isSuccess = errorMessage == null || 
                   isDuplicate || 
                   (errorMessage?.contains("success", ignoreCase = true) == true) || 
                   (errorMessage?.contains("logged successfully", ignoreCase = true) == true)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when {
                            isLoading -> "Processing"
                            isSuccess -> "Attendance Success"
                            else -> "Attendance Failed"
                        },
                        color = when {
                            isLoading -> TextDark
                            isSuccess -> Green700
                            else -> MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackToClass) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Green700)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                // Loading state
                CircularProgressIndicator(
                    color = Green700,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Logging your attendance...",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextDark)
                )
            } else if (errorMessage != null && !isSuccess) {
                // True error state (not success or duplicate)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(48.dp))
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    when (errorType) {
                        ErrorType.SERVER_ERROR -> Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Server Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        ErrorType.INVALID_QR -> Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Invalid QR",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        else -> Icon(
                            painter = painterResource(R.drawable.spot_logo),
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when (errorType) {
                        ErrorType.SERVER_ERROR -> "Server Error"
                        ErrorType.INVALID_QR -> "Invalid QR Code"
                        else -> "Attendance Failed"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onTryAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = Green700),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Try Again", color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBackToClass,
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Back to Classes", color = TextDark)
                }
            } else {
                // Success state - includes:  
                // 1. Normal success (no error message)
                // 2. Duplicate attendance (special case)
                // 3. Success with message from server (API returned 201/success with message)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(48.dp))
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when {
                        isDuplicate -> "Already Recorded"
                        errorMessage?.contains("success", ignoreCase = true) == true -> "Attendance Success"
                        else -> "Attendance Recorded"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Green700
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = when {
                        isDuplicate -> {
                            if (errorMessage?.contains("HTTP 400") == true) {
                                "Your attendance for this class has already been recorded."
                            } else if (errorMessage?.contains("[DUPLICATE_ATTENDANCE]") == true) {
                                errorMessage.replace("[DUPLICATE_ATTENDANCE] ", "")
                            } else {
                                errorMessage ?: "Your attendance for this class has already been recorded."
                            }
                        }
                        errorMessage != null -> {
                            // Use the actual success message from the server if available
                            errorMessage
                        }
                        else -> {
                            "Your attendance has been successfully logged for this class."
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onViewAttendanceLog,
                        colors = ButtonDefaults.buttonColors(containerColor = Green700),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("View Attendance Log", color = Color.White)
                    }
                    
                    OutlinedButton(
                        onClick = onBackToClass,
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Back to Classes", color = TextDark)
                    }
                    
                    OutlinedButton(
                        onClick = onBackToDashboard,
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Back to Dashboard", color = TextDark)
                    }
                }
            }
        }
    }
}