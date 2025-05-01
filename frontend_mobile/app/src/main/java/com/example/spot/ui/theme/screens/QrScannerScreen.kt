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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
                // Continue showing success screen, we already show it when QR is scanned
            }
            is AttendanceState.Error -> {
                isLoading = false
                errorMessage = (attendanceState as AttendanceState.Error).message
                // Reset scanResult to go back to scanner
                scanResult = null
            }
            else -> {
                isLoading = false
            }
        }
    }

    if (scanResult != null) {
        // Show success screen if scan is successful
        QrScanSuccessScreen(
            onBackToClass = { navController.popBackStack(Routes.DASHBOARD, inclusive = false) },
            onViewAttendanceLog = { 
                navController.navigate("attendance_calendar/$sectionId") 
            },
            isLoading = isLoading,
            errorMessage = errorMessage
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
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Invalid QR code: ${e.message}"
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
            Text(
                text = "Camera permission is required to scan QR codes.",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextDark),
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = Green700)
            ) {
                Text("Request Permission", color = Color.White)
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

@Composable
fun QrScanSuccessScreen(
    onBackToClass: () -> Unit,
    onViewAttendanceLog: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", color = Green700, fontWeight = FontWeight.Bold) },
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
                CircularProgressIndicator(
                    color = Green700,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Logging your attendance...",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextDark)
                )
            } else if (errorMessage != null) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_dialog_alert),
                    contentDescription = "Error",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Attendance Error",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onBackToClass,
                    colors = ButtonDefaults.buttonColors(containerColor = Green700),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text("Back to Classes", color = Color.White)
                }
            } else {
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
                    text = "Attendance Recorded",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Green700
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your attendance has been successfully logged for this class.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToClass,
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    ) {
                        Text("Back to Classes", color = TextDark)
                    }
                    
                    Button(
                        onClick = onViewAttendanceLog,
                        colors = ButtonDefaults.buttonColors(containerColor = Green700)
                    ) {
                        Text("View Attendance Log", color = Color.White)
                    }
                }
            }
        }
    }
}