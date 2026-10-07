package com.example.medilife

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun EmergencyScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val emergencyProfile by viewModel.emergencyMedicalProfile.collectAsState()
    val emergencyLogs by viewModel.emergencyAccessLogs.collectAsState()
    val doctorProfile by viewModel.doctorProfile.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val emergencyRequests by viewModel.emergencyAccessRequests.collectAsState()
    val overrideExpiresAtMillis by viewModel.emergencyOverrideExpiresAtMillis.collectAsState()
    val doctorContext = currentMode == UserMode.DOCTOR && userProfile.isDoctor
    val approvedRequest = emergencyRequests.firstOrNull {
        it.status == "APPROVED" && it.doctorName == doctorProfile.doctorName
    }
    var accessWindowActive by remember { mutableStateOf(false) }
    var showOverrideDialog by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }
    var overrideReason by remember { mutableStateOf("") }
    var requestReason by remember { mutableStateOf("") }
    var overrideConfirmed by remember { mutableStateOf(false) }

    LaunchedEffect(approvedRequest?.expiresAtMillis, overrideExpiresAtMillis) {
        val expiresAt = maxOf(approvedRequest?.expiresAtMillis ?: 0L, overrideExpiresAtMillis)
        val remaining = expiresAt - System.currentTimeMillis()
        if (remaining > 0L) {
            accessWindowActive = true
            delay(remaining)
        }
        accessWindowActive = false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Emergency,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Emergency Medical ID",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${userProfile.name} • DOB: ${userProfile.dob} • Last updated: ${emergencyProfile.lastUpdated}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (!doctorContext || accessWindowActive) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmergencyCardItem("Blood Group", emergencyProfile.bloodGroup, Modifier.weight(1f))
                    EmergencyCardItem("Critical Allergies", emergencyProfile.criticalAllergies.joinToString(", "), Modifier.weight(1f))
                }
            }

            item {
                EmergencyCategory(title = "MAJOR CONDITIONS", values = emergencyProfile.majorConditions)
            }

            item {
                EmergencyCategory(title = "ESSENTIAL MEDICINES", values = emergencyProfile.essentialMedicines)
            }

            item {
                EmergencyCategory(title = "EMERGENCY CONTACTS", values = emergencyProfile.emergencyContacts)
            }
        } else {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Emergency access required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Only critical medical information is shown after an explicit emergency override. The event is recorded in the access history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!doctorContext) {
            emergencyRequests.filter { it.status == "PENDING" }.forEach { request ->
                item {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Emergency Access Request", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${request.doctorName} • ${request.organization}", color = Color.White)
                            Text("Reason: ${request.reason}", color = Color.White.copy(alpha = 0.82f))
                            Text("Requested: ${request.requestedAt}", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.resolveEmergencyAccessRequest(request.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)
                                ) { Text("Approve 1 hour") }
                                TextButton(onClick = { viewModel.resolveEmergencyAccessRequest(request.id, false) }) {
                                    Text("Deny", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (doctorContext && !accessWindowActive) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showRequestDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Request patient approval")
                    }
                    Button(
                        onClick = { showOverrideDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Emergency, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emergency Override")
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EMERGENCY ACCESS AUDIT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                emergencyLogs.forEach { log ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = log.doctorName, fontWeight = FontWeight.Bold)
                            Text(text = log.organization, fontSize = 12.sp)
                            Text(text = log.accessType, fontSize = 12.sp)
                            Text(text = "Reason: ${log.reason}", fontSize = 12.sp)
                            Text(text = "Scope: ${log.scope}", fontSize = 12.sp)
                            Text(text = "Access: ${log.duration} • ${log.timestamp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Emergency Access Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Clinician: ${doctorProfile.doctorName} • ${doctorProfile.regId}")
                    Text("Organization: ${doctorProfile.hospital}")
                    Text("Only emergency-critical information will be opened. This access will be logged.")
                    OutlinedTextField(
                        value = overrideReason,
                        onValueChange = { overrideReason = it },
                        label = { Text("Emergency reason") },
                        placeholder = { Text("Describe the immediate clinical need") },
                        minLines = 2
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = overrideConfirmed, onCheckedChange = { overrideConfirmed = it })
                        Text("I confirm this is a genuine emergency")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = overrideReason.trim().length >= 5 && overrideConfirmed,
                    onClick = {
                        viewModel.recordEmergencyOverride(overrideReason.trim())
                        showOverrideDialog = false
                    }
                ) { Text("Request Access") }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Emergency Access Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Doctor: ${doctorProfile.doctorName} • ${doctorProfile.regId}")
                    Text("Hospital/Clinic: ${doctorProfile.hospital}")
                    Text("Requested information: blood group, allergies, major conditions, active medicines and emergency contacts")
                    OutlinedTextField(
                        value = requestReason,
                        onValueChange = { requestReason = it },
                        label = { Text("Reason") },
                        placeholder = { Text("Emergency patient assessment") },
                        minLines = 2
                    )
                    Text("The patient must approve before the critical emergency view is available.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = requestReason.trim().length >= 5,
                    onClick = {
                        viewModel.requestEmergencyAccess(requestReason.trim())
                        showRequestDialog = false
                    }
                ) { Text("Send Request") }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EmergencyCardItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun EmergencyCategory(title: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        values.forEach { value ->
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Text(value, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
