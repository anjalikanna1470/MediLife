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
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientOnboardingScreen(
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
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var aadhaarLast4 by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var familyHistory by remember { mutableStateOf("") }
    var geneticConditions by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }
    var currentMedicines by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var insuranceProvider by remember { mutableStateOf("") }
    var insuranceLast4 by remember { mutableStateOf("") }
    var bloodMenuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val bloodGroups = listOf(
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"
    )

    LaunchedEffect(pending) {
        pending?.let {
            name = it.name
            email = it.email
        }
    }

    fun csvToList(value: String): List<String> {
        return value.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
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
                val session = authRepository.completePatientOnboarding(
                    uid = current.uid,
                    name = name,
                    email = email,
                    dob = dob,
                    address = address,
                    maskedAadhaar = if (aadhaarLast4.isBlank()) {
                        ""
                    } else {
                        "XXXX XXXX ${aadhaarLast4.takeLast(4)}"
                    },
                    bloodGroup = bloodGroup,
                    allergies = csvToList(allergies),
                    familyHistory = csvToList(familyHistory),
                    emergencyContactName = emergencyName,
                    emergencyContactPhone = emergencyPhone,
                    chronicConditions = csvToList(chronicConditions),
                    geneticConditions = csvToList(geneticConditions),
                    currentMedicines = csvToList(currentMedicines),
                    insuranceProvider = insuranceProvider,
                    insurancePolicyLast4 = insuranceLast4
                )

                viewModel.applyAuthenticatedUser(session)
                viewModel.clearPendingSignup()
                onComplete()
            } catch (e: Exception) {
                errorMessage =
                    e.message ?: "Unable to save your health profile."
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
                "Complete your health profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "This information stays in your MediLife Firebase profile and is used to personalize your health experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ProfileField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                enabled = !isSaving
            )

            ProfileField(
                value = email,
                onValueChange = {},
                label = "E-Mail",
                enabled = false
            )

            ProfileField(
                value = dob,
                onValueChange = { dob = it },
                label = "Date of Birth (YYYY-MM-DD)",
                enabled = !isSaving
            )

            ProfileField(
                value = address,
                onValueChange = { address = it },
                label = "Full Address",
                enabled = !isSaving,
                singleLine = false
            )

            ProfileField(
                value = aadhaarLast4,
                onValueChange = {
                    aadhaarLast4 = it.filter(Char::isDigit).take(4)
                },
                label = "Aadhaar Last 4 Digits (optional)",
                enabled = !isSaving,
                keyboardType = KeyboardType.Number
            )

            ExposedDropdownMenuBox(
                expanded = bloodMenuExpanded,
                onExpandedChange = {
                    if (!isSaving) bloodMenuExpanded = !bloodMenuExpanded
                }
            ) {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Blood Group") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = bloodMenuExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = bloodMenuExpanded,
                    onDismissRequest = {
                        bloodMenuExpanded = false
                    }
                ) {
                    bloodGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group) },
                            onClick = {
                                bloodGroup = group
                                bloodMenuExpanded = false
                            }
                        )
                    }
                }
            }

            ProfileField(
                value = allergies,
                onValueChange = { allergies = it },
                label = "Allergies (comma separated)",
                enabled = !isSaving,
                placeholder = "Example: Penicillin, Dust"
            )

            ProfileField(
                value = chronicConditions,
                onValueChange = { chronicConditions = it },
                label = "Chronic / Existing Conditions",
                enabled = !isSaving,
                placeholder = "Example: Asthma, Diabetes"
            )

            ProfileField(
                value = geneticConditions,
                onValueChange = { geneticConditions = it },
                label = "Genetic / Hereditary Conditions",
                enabled = !isSaving,
                placeholder = "Example: BRCA history, none"
            )

            ProfileField(
                value = familyHistory,
                onValueChange = { familyHistory = it },
                label = "Family Medical History",
                enabled = !isSaving,
                placeholder = "Example: Type 2 Diabetes - Mother"
            )

            ProfileField(
                value = currentMedicines,
                onValueChange = { currentMedicines = it },
                label = "Current Medicines (optional)",
                enabled = !isSaving,
                placeholder = "Example: Metformin 500 mg"
            )

            ProfileField(
                value = emergencyName,
                onValueChange = { emergencyName = it },
                label = "Emergency Contact Name",
                enabled = !isSaving
            )

            ProfileField(
                value = emergencyPhone,
                onValueChange = { emergencyPhone = it },
                label = "Emergency Contact Phone",
                enabled = !isSaving,
                keyboardType = KeyboardType.Phone
            )

            ProfileField(
                value = insuranceProvider,
                onValueChange = { insuranceProvider = it },
                label = "Insurance Provider (optional)",
                enabled = !isSaving
            )

            ProfileField(
                value = insuranceLast4,
                onValueChange = {
                    insuranceLast4 = it.filter(Char::isLetterOrDigit).take(4)
                },
                label = "Insurance Policy Last 4 (optional)",
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

                Text("Save Health Profile")
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
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    placeholder: String? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
