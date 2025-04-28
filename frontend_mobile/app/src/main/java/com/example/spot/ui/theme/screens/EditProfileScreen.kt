@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.model.Student
import com.example.spot.ui.theme.Green700
import com.example.spot.viewmodel.ProfileUpdateState
import com.example.spot.viewmodel.StudentState
import com.example.spot.viewmodel.StudentViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    studentViewModel: StudentViewModel = viewModel()
) {
    val studentState by studentViewModel.studentState.collectAsState()
    val updateState by studentViewModel.profileUpdateState.collectAsState()
    
    // Load student profile if it's not already loaded
    LaunchedEffect(Unit) {
        if (studentState !is StudentState.Success) {
            studentViewModel.loadStudentProfile()
        }
    }
    
    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is ProfileUpdateState.Success -> {
                // Navigate back on success
                navController.popBackStack()
            }
            else -> {
                // Stay on this screen
            }
        }
    }
    
    // Clean up when leaving
    DisposableEffect(Unit) {
        onDispose {
            studentViewModel.resetUpdateState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Green700, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            when (studentState) {
                is StudentState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green700)
                    }
                }
                
                is StudentState.Success -> {
                    val student = (studentState as StudentState.Success).student
                    var firstName by remember { mutableStateOf(student.firstName) }
                    var middleName by remember { mutableStateOf(student.middleName ?: "") }
                    var lastName by remember { mutableStateOf(student.lastName) }
                    var year by remember { mutableStateOf(student.year) }
                    var program by remember { mutableStateOf(student.program) }
                    var email by remember { mutableStateOf(student.email) }
                    var studentPhysicalId by remember { mutableStateOf(student.studentPhysicalId) }
                    var password by remember { mutableStateOf("") }
                    var showPassword by remember { mutableStateOf(false) }
                    
                    // Track if any field has been modified
                    val isModified = remember(firstName, middleName, lastName, year, program, email, studentPhysicalId, password) {
                        firstName != student.firstName ||
                        middleName != (student.middleName ?: "") ||
                        lastName != student.lastName ||
                        year != student.year ||
                        program != student.program ||
                        email != student.email ||
                        studentPhysicalId != student.studentPhysicalId ||
                        password.isNotEmpty()
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Form fields
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = middleName,
                            onValueChange = { middleName = it },
                            label = { Text("Middle Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("Year") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = program,
                            onValueChange = { program = it },
                            label = { Text("Program") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = studentPhysicalId,
                            onValueChange = { studentPhysicalId = it },
                            label = { Text("Student ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("New Password (Leave blank to keep current)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Error message
                        if (updateState is ProfileUpdateState.Error) {
                            Text(
                                text = (updateState as ProfileUpdateState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        // Save button
                        Button(
                            onClick = {
                                studentViewModel.updateProfile(
                                    student.id,
                                    firstName,
                                    middleName.ifEmpty { null },
                                    lastName,
                                    year,
                                    program,
                                    email,
                                    studentPhysicalId,
                                    password.ifEmpty { null }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isModified && updateState !is ProfileUpdateState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Green700)
                        ) {
                            if (updateState is ProfileUpdateState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Save Changes")
                        }
                    }
                }
                
                is StudentState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading profile",
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (studentState as StudentState.Error).message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { studentViewModel.loadStudentProfile() },
                                colors = ButtonDefaults.buttonColors(containerColor = Green700)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                else -> {
                    // Idle state - showing nothing until student is loaded
                }
            }
        }
    }
}
