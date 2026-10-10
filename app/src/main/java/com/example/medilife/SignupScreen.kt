package com.example.medilife

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    viewModel: AppViewModel,
    onPatientOnboarding: () -> Unit,
    onDoctorOnboarding: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val authRepository = remember { AuthRepository() }
    val googleAuthManager = remember(context) {
        GoogleAuthManager(context)
    }

    var selectedRole by remember { mutableStateOf(UserMode.PATIENT) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun handleSuccess(pending: PendingAuthUser) {
        viewModel.setPendingSignup(pending)

        if (pending.role == UserMode.PATIENT) {
            onPatientOnboarding()
        } else {
            onDoctorOnboarding()
        }
    }

    fun signUpWithGoogle() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val googleAccount = googleAuthManager.getGoogleAccount()

                val pending = authRepository.registerWithGoogle(
                    idToken = googleAccount.idToken,
                    requestedMode = selectedRole
                )

                handleSuccess(pending)
            } catch (e: Exception) {
                errorMessage =
                    e.message ?: "Google sign-up failed. Please try again."
            } finally {
                isLoading = false
            }
        }
    }

    fun signUpWithEmail() {
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match."
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val pending = authRepository.registerWithEmail(
                    name = name,
                    email = email,
                    password = password,
                    requestedMode = selectedRole
                )

                handleSuccess(pending)
            } catch (e: Exception) {
                errorMessage =
                    e.message ?: "Unable to create the account."
            } finally {
                isLoading = false
            }
        }
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
            Spacer(Modifier.height(8.dp))

            Image(
                painter = painterResource(R.drawable.medilife_logo),
                contentDescription = "MediLife",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(112.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Create your MediLife account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Choose your role first. Your health profile will be collected after registration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedRole == UserMode.PATIENT) {
                    Button(
                        onClick = {
                            selectedRole = UserMode.PATIENT
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Patient")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            selectedRole = UserMode.PATIENT
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Patient")
                    }
                }

                if (selectedRole == UserMode.DOCTOR) {
                    Button(
                        onClick = {
                            selectedRole = UserMode.DOCTOR
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.MedicalServices, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Doctor")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            selectedRole = UserMode.DOCTOR
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.MedicalServices, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Doctor")
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedButton(
                onClick = ::signUpWithGoogle,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "G",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (isLoading) "Creating account..." else "Sign up with Google",
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
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Full Name") },
                enabled = !isLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("E-Mail") },
                enabled = !isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm Password") },
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = ::signUpWithEmail,
                enabled = !isLoading &&
                    name.isNotBlank() &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    confirmPassword.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (selectedRole == UserMode.PATIENT)
                        "Create Patient Account"
                    else
                        "Create Doctor Account",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(18.dp))

            TextButton(
                onClick = onBackToLogin,
                enabled = !isLoading
            ) {
                Text("Already have an account? Log in")
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}
