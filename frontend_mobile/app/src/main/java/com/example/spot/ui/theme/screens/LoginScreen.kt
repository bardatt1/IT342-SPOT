package com.example.spot.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spot.R

import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.Green700
import com.example.spot.ui.theme.TextDark
import com.example.spot.viewmodel.*

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // Collect login state
    val loginState by authViewModel.loginState.collectAsState()
    
    // UI State variables
    val isLoading = loginState is AuthState.Loading
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    
    // Student ID and password state
    var studentPhysicalId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    

    
    // Collect password change state
    val passwordChangeState by authViewModel.passwordChangeState.collectAsState()
    
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Loading -> {
                // Do nothing, already handled by isLoading state
            }
            is AuthState.Success -> {
                // Navigate based on user type
                val data = (loginState as? AuthState.Success)?.data
                if (data != null) {
                    // Check if using temporary password
                    if (password == "temporary") {
                        // Set temporary password flag and show password change dialog
                        showPasswordChangeDialog = true
                        errorMessage = null
                    } else {
                        // Navigate to dashboard
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            }
            is AuthState.Error -> {
                errorMessage = (loginState as AuthState.Error).message
            }
            else -> {
                // Do nothing for Idle state
            }
        }
    }
    

    
    LaunchedEffect(passwordChangeState) {
        when (passwordChangeState) {
            is PasswordChangeState.Success -> {
                Log.d("LoginScreen", "Password changed successfully")
                // Close password change dialog
                showPasswordChangeDialog = false
                
                // Navigate to dashboard
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            is PasswordChangeState.Error -> {
                Log.e("LoginScreen", "Password change failed: ${(passwordChangeState as PasswordChangeState.Error).message}")
                // Don't dismiss dialog, let user try again
            }
            else -> {
                // Do nothing for Loading and Idle states
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x0D215F47), // from-[#215f47]/5
                        Color.White,       // via-white
                        Color(0x1A215F47)  // to-[#215f47]/10
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            border = BorderStroke(1.dp, Color(0x33215F47)) // border-[#215f47]/20
        ) {
            // Green accent line at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Green700)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge with "Secure Login"
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x0D215F47), // bg-[#215f47]/5
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Secure Login",
                        color = Green700,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.spot_logo),
                    contentDescription = "SPOT Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                )
                
                // Title and Description
                Text(
                    text = "SPOT",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Green700
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "Student Presence and Oversight Tracker",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Error message if any
                errorMessage?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Authentication Error",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color(0xFFB71C1C),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
                
                // Student ID (Email) Input
                OutlinedTextField(
                    value = studentPhysicalId,
                    onValueChange = { studentPhysicalId = it },
                    label = { Text("Student ID") },
                    placeholder = { Text("Enter your student ID") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green700,
                        focusedLabelColor = Green700,
                        cursorColor = Green700
                    )
                )
                
                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("••••••••") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = if (passwordVisible) Green700 else Color.Gray
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green700,
                        focusedLabelColor = Green700,
                        cursorColor = Green700
                    )
                )
                
                // Login Button
                Button(
                    onClick = {
                        if (studentPhysicalId.isNotBlank() && password.isNotBlank()) {
                            authViewModel.loginUser(studentPhysicalId, password)
                        } else {
                            errorMessage = "Please enter both student ID and password"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green700,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Sign in")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = "Login",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Protected access for authorized personnel only",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
    
    // Password change dialog
    if (showPasswordChangeDialog) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var newPasswordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var passwordChangeError by remember { mutableStateOf<String?>(null) }
        
        Dialog(
            onDismissRequest = { /* Dialog cannot be dismissed */ }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Password Change Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You are using a temporary password. For security reasons, please change your password before continuing.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Display error message if any
                    if (passwordChangeError != null) {
                        Text(
                            text = passwordChangeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // New password field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Confirm password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Change password button
                    Button(
                        onClick = {
                            // Validate password change
                            when {
                                newPassword.length < 8 -> {
                                    passwordChangeError = "Password must be at least 8 characters"
                                }
                                newPassword != confirmPassword -> {
                                    passwordChangeError = "Passwords do not match"
                                }
                                else -> {
                                    passwordChangeError = null
                                    
                                    // Call the change password method with the string ID
                                    // Note: The AuthViewModel should handle the conversion
                                    try {
                                        authViewModel.changePassword(studentPhysicalId, newPassword)
                                    } catch (e: Exception) {
                                        passwordChangeError = "Error: ${e.message}"
                                        Log.e("LoginScreen", "Error changing password", e)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green700)
                    ) {
                        Text("Change Password")
                    }
                }
            }
        }
    }

    // Clean up states when leaving the screen
    DisposableEffect(key1 = Unit) {
        onDispose {
            authViewModel.resetStates()
        }
    }
}