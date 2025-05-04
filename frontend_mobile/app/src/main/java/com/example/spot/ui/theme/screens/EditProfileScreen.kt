@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
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
import com.example.spot.ui.theme.TextDark
import com.example.spot.viewmodel.ProfileUpdateState
import com.example.spot.viewmodel.StudentState
import com.example.spot.viewmodel.StudentViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign

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
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Green700,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Edit Profile",
                            color = Green700, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
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
        containerColor = Color(0xFFF8F8F8)
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Green700)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading profile...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Green700
                            )
                        }
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
                    
                    // Password fields
                    var currentPassword by remember { mutableStateOf("") }
                    var newPassword by remember { mutableStateOf("") }
                    var confirmPassword by remember { mutableStateOf("") }
                    
                    // Password visibility toggles
                    var showCurrentPassword by remember { mutableStateOf(false) }
                    var showNewPassword by remember { mutableStateOf(false) }
                    var showConfirmPassword by remember { mutableStateOf(false) }
                    
                    // Local validation errors
                    var passwordError by remember { mutableStateOf<String?>(null) }
                    
                    // Track if any field has been modified
                    val isModified = remember(firstName, middleName, lastName, year, program, email, studentPhysicalId, currentPassword, newPassword, confirmPassword) {
                        firstName != student.firstName ||
                        middleName != (student.middleName ?: "") ||
                        lastName != student.lastName ||
                        year != student.year ||
                        program != student.program ||
                        email != student.email ||
                        studentPhysicalId != student.studentPhysicalId ||
                        currentPassword.isNotEmpty() ||
                        newPassword.isNotEmpty() ||
                        confirmPassword.isNotEmpty()
                    }
                    
                    // Password validation function
                    val validatePasswords = {
                        when {
                            newPassword.isNotEmpty() && newPassword != confirmPassword -> {
                                passwordError = "New passwords don't match"
                                false
                            }
                            newPassword.isNotEmpty() && newPassword.length < 8 -> {
                                passwordError = "New password must be at least 8 characters long"
                                false
                            }
                            newPassword.isNotEmpty() && currentPassword.isEmpty() -> {
                                passwordError = "Current password is required to set a new password"
                                false
                            }
                            else -> {
                                passwordError = null
                                true
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Personal Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Green700,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                // Form fields
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    label = { Text("First Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = middleName,
                                    onValueChange = { middleName = it },
                                    label = { Text("Middle Name (Optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = { lastName = it },
                                    label = { Text("Last Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Academic Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Green700,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                OutlinedTextField(
                                    value = year,
                                    onValueChange = { year = it },
                                    label = { Text("Year") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = program,
                                    onValueChange = { program = it },
                                    label = { Text("Program") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Student ID field - read-only
                                OutlinedTextField(
                                    value = studentPhysicalId,
                                    onValueChange = { /* No-op - field is read-only */ },
                                    label = { Text("Student ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    enabled = false,
                                    readOnly = true,
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = "Student ID",
                                            tint = Color.Gray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = TextDark.copy(alpha = 0.8f),
                                        disabledLabelColor = TextDark.copy(alpha = 0.6f),
                                        disabledBorderColor = TextDark.copy(alpha = 0.3f),
                                        disabledLeadingIconColor = TextDark.copy(alpha = 0.5f)
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Cannot be changed",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Change Password",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Green700,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                OutlinedTextField(
                                    value = currentPassword,
                                    onValueChange = { currentPassword = it },
                                    label = { Text("Current Password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                            Icon(
                                                if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (showCurrentPassword) "Hide password" else "Show password",
                                                tint = Green700
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { 
                                        newPassword = it
                                        if (it.isNotEmpty()) validatePasswords()
                                    },
                                    label = { Text("New Password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.VpnKey,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                            Icon(
                                                if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (showNewPassword) "Hide password" else "Show password",
                                                tint = Green700
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { 
                                        confirmPassword = it 
                                        if (it.isNotEmpty()) validatePasswords()
                                    },
                                    label = { Text("Confirm New Password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.VpnKey,
                                            contentDescription = null,
                                            tint = Green700
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                            Icon(
                                                if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                                tint = Green700
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Green700,
                                        focusedLabelColor = Green700,
                                        cursorColor = Green700
                                    )
                                )
                                
                                if (passwordError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = passwordError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "Leave password fields empty to keep your current password.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Error message
                        if (updateState is ProfileUpdateState.Error) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (updateState as ProfileUpdateState.Error).message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Green700)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Green700
                                )
                            }
                            
                            // Save button
                            Button(
                                onClick = {
                                    // Validate passwords first
                                    if (validatePasswords()) {
                                        studentViewModel.updateProfile(
                                            studentId = student.id,
                                            firstName = firstName,
                                            middleName = middleName.ifEmpty { null },
                                            lastName = lastName,
                                            studentYear = year,
                                            studentProgram = program,
                                            studentEmail = email,
                                            studentPhysicalId = studentPhysicalId,
                                            newPassword = if (newPassword.isNotEmpty()) newPassword else null,
                                            currentPassword = if (currentPassword.isNotEmpty()) currentPassword else null
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                
                is StudentState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Error loading profile",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = (studentState as StudentState.Error).message,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Button(
                                    onClick = { studentViewModel.loadStudentProfile() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Green700),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retry")
                                }
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
