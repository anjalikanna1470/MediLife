package com.example.medilife

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DoctorOnboardingScreen(
    viewModel: AppViewModel,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pending by viewModel.pendingSignup.collectAsState()
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var registrationId by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(pending) {
        pending?.let {
            name = it.name
            email = it.email
        }
    }

    fun saveProfile() {
        val current = pending

        if (current == null) {
            errorMessage = "Signup session expired. Please register again."
            return
        }

        scope.launch {
            isSaving = true
            errorMessage = null

            try {
                val session = authRepository.completeDoctorOnboarding(
                    uid = current.uid,
                    name = name,
                    email = email,
                    specialty = specialty,
                    registrationId = registrationId,
                    hospital = hospital,
                    phone = phone,
                    city = city
                )

                viewModel.applyAuthenticatedUser(session)
                viewModel.clearPendingSignup()
                onComplete()
            } catch (e: Exception) {
                errorMessage =
                    e.message ?: "Unable to save the doctor profile."
            } finally {
                isSaving = false
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Complete your doctor profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "These professional details are stored under your MediLife doctor profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DoctorField(
                value = name,
                onValueChange = { name = it },
                label = "Doctor Name",
                enabled = !isSaving
            )

            DoctorField(
                value = email,
                onValueChange = {},
                label = "E-Mail",
                enabled = false
            )

            DoctorField(
                value = specialty,
                onValueChange = { specialty = it },
                label = "Specialty",
                enabled = !isSaving,
                placeholder = "Example: General Physician & Cardiologist"
            )

            DoctorField(
                value = registrationId,
                onValueChange = { registrationId = it },
                label = "Medical Registration ID",
                enabled = !isSaving
            )

            DoctorField(
                value = hospital,
                onValueChange = { hospital = it },
                label = "Hospital / Clinic",
                enabled = !isSaving
            )

            DoctorField(
                value = phone,
                onValueChange = { phone = it },
                label = "Professional Contact Phone",
                enabled = !isSaving,
                keyboardType = KeyboardType.Phone
            )

            DoctorField(
                value = city,
                onValueChange = { city = it },
                label = "City",
                enabled = !isSaving
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = ::saveProfile,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text("Save Doctor Profile")
            }

            TextButton(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DoctorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
