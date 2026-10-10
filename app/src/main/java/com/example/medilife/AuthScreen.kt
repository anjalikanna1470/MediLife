package com.example.medilife

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: AppViewModel,
    onLoginSuccess: (UserMode) -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val authRepository = remember { AuthRepository() }
    val googleAuthManager = remember(context) {
        GoogleAuthManager(context)
    }

    var email by remember {
        mutableStateOf(
            context.getSharedPreferences("medilife_auth", Context.MODE_PRIVATE)
                .getString("remembered_email", "")
                .orEmpty()
        )
    }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(email.isNotBlank()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleRoleDialog by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val busy = isLoading || isGoogleLoading

    fun saveRememberedEmail() {
        context.getSharedPreferences("medilife_auth", Context.MODE_PRIVATE)
            .edit()
            .apply {
                if (rememberMe) {
                    putString("remembered_email", email.trim())
                } else {
                    remove("remembered_email")
                }
            }
            .apply()
    }

    fun loginWithGoogle(mode: UserMode) {
        scope.launch {
            isGoogleLoading = true
            errorMessage = null
            infoMessage = null

            try {
                val googleAccount = googleAuthManager.getGoogleAccount()

                val session = authRepository.signInWithGoogle(
                    idToken = googleAccount.idToken,
                    requestedMode = mode
                )

                viewModel.applyAuthenticatedUser(session)
                onLoginSuccess(session.role)
            } catch (e: Exception) {
                errorMessage =
                    e.message ?: "Google sign-in failed. Please try again."
            } finally {
                isGoogleLoading = false
                showGoogleRoleDialog = false
            }
        }
    }

    if (showGoogleRoleDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isGoogleLoading) {
                    showGoogleRoleDialog = false
                }
            },
            title = {
                Text("Continue with Google")
            },
            text = {
                Text(
                    "Select the MediLife role for this account. " +
                        "Google does not decide whether the account is a patient or doctor."
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        enabled = !isGoogleLoading,
                        onClick = {
                            loginWithGoogle(UserMode.PATIENT)
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Patient")
                    }

                    Button(
                        enabled = !isGoogleLoading,
                        onClick = {
                            loginWithGoogle(UserMode.DOCTOR)
                        }
                    ) {
                        Icon(
                            Icons.Outlined.MedicalServices,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Doctor")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isGoogleLoading,
                    onClick = {
                        showGoogleRoleDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            Image(
                painter = painterResource(R.drawable.medilife_logo),
                contentDescription = "MediLife",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(126.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Log in to your account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Access your personal health records securely.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            OutlinedButton(
                onClick = {
                    errorMessage = null
                    infoMessage = null
                    showGoogleRoleDialog = true
                },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "G",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    if (isGoogleLoading) "Connecting to Google..."
                    else "Continue with Google",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text(
                    "  OR  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                HorizontalDivider(Modifier.weight(1f))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("E-Mail") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                singleLine = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled = !busy
                    )
                    Text("Remember Me")
                }

                TextButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            errorMessage = null
                            infoMessage = null

                            try {
                                authRepository.sendPasswordReset(email)
                                infoMessage =
                                    "Password reset email sent. Check your inbox."
                            } catch (e: Exception) {
                                errorMessage =
                                    e.message ?: "Unable to send the reset email."
                            }
                        }
                    }
                ) {
                    Text("Forgot Your Password?")
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            if (!infoMessage.isNullOrBlank()) {
                Text(
                    text = infoMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    saveRememberedEmail()

                    viewModel.signIn(
                        email = email,
                        password = password,
                        requestedMode = UserMode.PATIENT,
                        onSuccess = onLoginSuccess,
                        onError = { errorMessage = it }
                    )
                },
                enabled = !busy &&
                    email.isNotBlank() &&
                    password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isLoading) "Signing In..." else "Sign In as Patient",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    saveRememberedEmail()

                    viewModel.signIn(
                        email = email,
                        password = password,
                        requestedMode = UserMode.DOCTOR,
                        onSuccess = onLoginSuccess,
                        onError = { errorMessage = it }
                    )
                },
                enabled = !busy &&
                    email.isNotBlank() &&
                    password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Outlined.MedicalServices,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sign In as Doctor",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Register",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        enabled = !busy,
                        onClick = onNavigateToSignup
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
