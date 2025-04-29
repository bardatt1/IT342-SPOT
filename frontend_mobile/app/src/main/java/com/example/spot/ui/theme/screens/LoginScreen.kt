package com.example.spot.ui.theme.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.spot.auth.GoogleAuthHelper
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.Green700
import com.example.spot.ui.theme.TextDark
import com.example.spot.util.TokenManager
import com.example.spot.viewmodel.*

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // UI State variables
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleSignInLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBindGooglePrompt by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var currentUserType by remember { mutableStateOf<String?>(null) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var googleLinked by remember { mutableStateOf(false) }

    // Email and password state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSignUpTypeModal by remember { mutableStateOf(false) }
    
    // Initialize TokenManager with context
    val context = LocalContext.current
    
    // Create GoogleAuthHelper instance
    val googleAuthHelper = remember { GoogleAuthHelper(context) }
    
    // Collect binding state from ViewModel
    val bindStudentGoogleState by authViewModel.bindStudentGoogleState.collectAsState()
    
    // Collect showBindGooglePrompt state from the ViewModel
    val shouldBindGoogleAccount by authViewModel.showBindGooglePrompt.collectAsState()
    
    // Function to handle binding a Google account after successful sign-in
    val handleGoogleAccountBinding = { googleId: String ->
        currentUserId?.let { userId ->
            when (currentUserType) {
                "STUDENT" -> {
                    Log.d("LoginScreen", "Binding Google account to student ID: $userId")
                    authViewModel.bindGoogleAccountByUserType(currentUserType ?: "", userId, googleId)
                }
                else -> {
                    Log.e("LoginScreen", "Unsupported user type for mobile: $currentUserType")
                    errorMessage = "Cannot bind Google account: unsupported user type for mobile"
                }
            }
        } ?: run {
            Log.e("LoginScreen", "Cannot bind Google account: no user ID")
            errorMessage = "Cannot bind Google account: please log in first"
        }
    }
    
    // Set up the activity result launcher for Google Sign In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In activity result received: resultCode=${result.resultCode}")
        isGoogleSignInLoading = false
        
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = googleAuthHelper.handleSignInResult(result.data)
                if (account != null) {
                    // Process the signed-in account
                    Log.d("LoginScreen", "Google Sign-In successful for: ${account.email}, binding=${showBindGooglePrompt}")
                    
                    if (showBindGooglePrompt && currentUserId != null) {
                        // We're binding a Google account to an existing student account
                        Log.d("LoginScreen", "Binding Google account to existing student ID: $currentUserId")
                        handleGoogleAccountBinding(account.id ?: "")
                    } else {
                        // Regular Google sign-in flow for returning users
                        authViewModel.performGoogleLogin(account.email ?: "", account.id ?: "")
                    }
                } else {
                    Log.e("LoginScreen", "Google Sign-In returned null account")
                    errorMessage = "Google Sign-In failed. Account could not be obtained."
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Error processing Google Sign-In result", e)
                errorMessage = "Error during Google Sign-In: ${e.message}"
            }
        } else {
            Log.d("LoginScreen", "Google Sign-In was cancelled or failed. Result code: ${result.resultCode}")
            errorMessage = "Google Sign-In was cancelled"
        }
    }
    
    // Trigger Google Sign-In if binding is needed and prompt is shown
    LaunchedEffect(shouldBindGoogleAccount) {
        if (shouldBindGoogleAccount && !showBindGooglePrompt) {
            Log.d("LoginScreen", "Showing Google binding prompt based on viewModel state")
            showBindGooglePrompt = true
        }
    }
    
    LaunchedEffect(Unit) {
        TokenManager.initialize(context)
    }
    
    // Observe login state
    val loginState by authViewModel.loginState.collectAsState()
    
    // Observe Google sign-in state for binding flow
    val googleSignInState by authViewModel.googleSignInState.collectAsState()
    LaunchedEffect(googleSignInState) {
        when (googleSignInState) {
            is GoogleSignInState.Loading -> {
                isGoogleSignInLoading = true
                errorMessage = null
            }
            is GoogleSignInState.Success -> {
                isGoogleSignInLoading = false
                val result = googleSignInState as GoogleSignInState.Success
                
                if (showBindGooglePrompt && currentUserId != null) {
                    // User is logged in and needs to bind Google account
                    Log.d("LoginScreen", "Binding Google account ${result.email} to user ID $currentUserId")
                    // Call the binding function with the googleId
                    currentUserId?.let { userId ->
                        Log.d("LoginScreen", "Binding Google account to student ID: $userId")
                        authViewModel.bindStudentGoogleAccount(userId, result.googleId)
                    }
                } else {
                    // Regular Google sign-in flow for returning users
                    Log.d("LoginScreen", "Attempting Google login with email: ${result.email}")
                    authViewModel.performGoogleLogin(result.email, result.googleId)
                }
            }
            is GoogleSignInState.Error -> {
                isGoogleSignInLoading = false
                val errorMsg = (googleSignInState as GoogleSignInState.Error).message
                errorMessage = "Google Sign-In Error: $errorMsg"
                Log.e("LoginScreen", "Google Sign-In Error: $errorMsg")
            }
            is GoogleSignInState.Cancelled -> {
                isGoogleSignInLoading = false
                errorMessage = "Google Sign-In was cancelled"
                Log.d("LoginScreen", "Google Sign-In was cancelled")
            }
            else -> {
                // Idle state, no action needed
            }
        }
    }
    
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is AuthState.Success -> {
                isLoading = false
                val data = (loginState as AuthState.Success).data
                if (data != null) {
                    // Store googleLinked value
                    googleLinked = data.googleLinked // Set googleLinked from the login response

                    // Rest of your login success logic...
                    currentUserId = data.id
                    currentUserType = data.userType
                    // Navigate to the dashboard if already linked
                    if (data.googleLinked) {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        showBindGooglePrompt = true
                    }
                } else {
                    errorMessage = "Login successful but no user data received"
                    Log.e("LoginScreen", "No user data received in successful login")
                }
            }
            is AuthState.Error -> {
                isLoading = false
                isGoogleSignInLoading = false
                errorMessage = (loginState as AuthState.Error).message
                Log.e("LoginScreen", "Login error: $errorMessage")
            }
            else -> {
                // Idle state, no action needed
            }
        }
    }
    
    // Observe the Google binding state to properly handle successful binding
    LaunchedEffect(bindStudentGoogleState) {
        when (bindStudentGoogleState) {
            is BindOAuthState.Success -> {
                Log.d("LoginScreen", "Google account binding successful")
                showBindGooglePrompt = false
                // Reset the binding prompt flag in ViewModel
                authViewModel.resetBindGooglePrompt()
                
                // Navigate to dashboard after successful binding
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            is BindOAuthState.Error -> {
                isGoogleSignInLoading = false
                val errorMsg = (bindStudentGoogleState as BindOAuthState.Error).message
                Log.e("LoginScreen", "Google account binding failed: $errorMsg")
                errorMessage = "Failed to bind Google account: $errorMsg"
            }
            is BindOAuthState.Loading -> {
                isGoogleSignInLoading = true
                errorMessage = null
            }
            else -> {
                // Do nothing for Idle state
            }
        }
    }
    
    // Collect password change state
    val passwordChangeState by authViewModel.passwordChangeState.collectAsState()
    
    // Effect to handle password change state
    LaunchedEffect(passwordChangeState) {
        when (passwordChangeState) {
            is PasswordChangeState.Success -> {
                Log.d("LoginScreen", "Password changed successfully")
                // Close password change dialog
                showPasswordChangeDialog = false
                
                // Check if Google account binding is needed
                currentUserId?.let { _ ->
                    if (currentUserType == "STUDENT") {
                        val jwtResponse = (loginState as? AuthState.Success)?.data
                        if (jwtResponse != null && !jwtResponse.googleLinked) {
                            // Show Google account binding prompt
                            showBindGooglePrompt = true
                            Log.d("LoginScreen", "Showing Google binding prompt after password change")
                        } else {
                            // Navigate to dashboard
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.spot_logo),
            contentDescription = "SPOT Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )

        // Title
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontSize = 28.sp
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Error message if any
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = errorMessage != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password field with visibility toggle
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = errorMessage != null,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        // Forgot Password link
        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextDark,
                fontSize = 14.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
                .clickable { /* Handle forgot password */ },
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Login button
        Button(
            onClick = {
                // Validate input fields
                if (email.isBlank()) {
                    errorMessage = "Email cannot be empty"
                    return@Button
                }
                if (password.isBlank()) {
                    errorMessage = "Password cannot be empty"
                    return@Button
                }
                
                // Clear error message and trigger login
                errorMessage = null
                authViewModel.loginUser(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green700),
            enabled = !isLoading && !isGoogleSignInLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continue with Google button
        OutlinedButton(
            onClick = { 
                isGoogleSignInLoading = true
                errorMessage = null
                Log.d("LoginScreen", "Launching Google Sign-In intent")
                try {
                    val signInIntent = googleAuthHelper.getSignInIntent()
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Error launching Google Sign-In", e)
                    errorMessage = "Error launching Google Sign-In: ${e.message}"
                    isGoogleSignInLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            enabled = !isLoading && !isGoogleSignInLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF4285F4) // Google blue color
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGoogleSignInLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    // Using a text "G" styled like Google logo since we can't use the actual logo
                    Text(
                        text = "G",
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF4285F4), shape = CircleShape)
                            .padding(4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue with Google",
                    color = TextDark,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign up link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "You don't have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextDark,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "Sign up",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Green700,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { showSignUpTypeModal = true }
            )
        }

        // Google account binding prompt dialog
        if (showBindGooglePrompt) {
            // Display the binding dialog
            Dialog(
                onDismissRequest = {
                    showBindGooglePrompt = false
                    authViewModel.resetBindGooglePrompt()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
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
                            text = "Link Google Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Do you want to link your Google account for easier sign-in next time?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (!googleLinked) {
                                // Show Link Google button if not already linked
                                Button(
                                    onClick = {
                                        isGoogleSignInLoading = true
                                        errorMessage = null
                                        Log.d("LoginScreen", "Launching Google Sign-In intent for binding")
                                        try {
                                            val signInIntent = googleAuthHelper.getSignInIntent()
                                            googleSignInLauncher.launch(signInIntent)
                                        } catch (e: Exception) {
                                            Log.e("LoginScreen", "Error launching Google Sign-In", e)
                                            errorMessage = "Error launching Google Sign-In: ${e.message}"
                                            isGoogleSignInLoading = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Green700),
                                    enabled = !isGoogleSignInLoading
                                ) {
                                    if (isGoogleSignInLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Link Google")
                                    }
                                }
                            } else {
                                // If Google is already linked, show the Skip option
                                Text("Your Google account is already linked!", color = Color.Green)
                            }
                        }
                    }
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
                                        imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                                        
                                        // If we have a student ID, change the password
                                        currentUserId?.let { studentId ->
                                            authViewModel.changePassword(studentId, newPassword)
                                        } ?: run {
                                            passwordChangeError = "Error: Student ID not found"
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
    }

    // Clean up states when leaving the screen
    DisposableEffect(key1 = Unit) {
        onDispose {
            authViewModel.resetStates()
        }
    }
}