package com.example.medilife

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DoctorDashboardScreen(
    viewModel: AppViewModel,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val doctorProfile by viewModel.doctorProfile.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val consents by viewModel.consents.collectAsState()
    val doctorPatients by viewModel.doctorPatients.collectAsState()
    val modeOptions = listOf(UserMode.PATIENT to "Patient Mode", UserMode.DOCTOR to "Doctor Mode")

    val pendingRequests = consents.filter { it.status == "PENDING" }
    val activePatients = doctorPatients
        .filter { it.isTreatmentActive }
        .sortedByDescending { it.lastTreatmentDate }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // DOCTOR WELCOME HEADER
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = doctorProfile.doctorName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${doctorProfile.specialty}\n• ${doctorProfile.hospital}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    modeOptions.forEach { (mode, label) ->
                        val selected = currentMode == mode
                        Surface(
                            onClick = {
                                viewModel.switchMode(mode)
                                if (mode == UserMode.PATIENT) onNavigateTo("home")
                                else onNavigateTo("doctor_dashboard")
                            },
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // CLINICAL SUMMARY STATS CARDS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStatCard(
                    title = "Active Patients",
                    count = "${activePatients.size} Active",
                    icon = Icons.Outlined.People,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("doctor_patients") }
                )
                DashboardStatCard(
                    title = "Access Requests",
                    count = "${pendingRequests.size} Pending",
                    icon = Icons.Outlined.Key,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("consent") }
                )
            }
        }

        // CLINICAL COPILOT ENTRY CARD
        item {
            Surface(
                onClick = { onNavigateTo("doctor_clinical_ai") },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Clinical AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Review longitudinal summaries, drug interaction flags & lab trends",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // AUTHORIZED PATIENT WORKSPACE QUICK LAUNCH
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "Active Patients",
                    actionText = "View All",
                    onActionClick = { onNavigateTo("doctor_patients") }
                )

                activePatients.take(3).forEach { patient ->
                    Surface(
                        onClick = { onNavigateTo("doctor_patient_workspace") },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = patient.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Last treatment: ${patient.lastTreatmentDate} • ${patient.activeConditions}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { onNavigateTo("doctor_patient_workspace") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 86.dp, minHeight = 48.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Open", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.82f), fontSize = 11.sp)
            Text(text = count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
