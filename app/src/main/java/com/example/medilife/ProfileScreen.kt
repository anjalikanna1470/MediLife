package com.example.medilife

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val doctorProfile by viewModel.doctorProfile.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val accessHistory by viewModel.accessHistory.collectAsState()
    val displayName = if (currentMode == UserMode.DOCTOR) doctorProfile.doctorName else userProfile.name
    val displaySubtitle = if (currentMode == UserMode.DOCTOR) "${doctorProfile.specialty} • ${doctorProfile.hospital}" else userProfile.email

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PROFILE HEADER
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = displaySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (userProfile.isDoctor) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(UserMode.PATIENT, UserMode.DOCTOR).forEach { mode ->
                                val selected = currentMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        viewModel.switchMode(mode)
                                        onNavigateTo(if (mode == UserMode.PATIENT) "home" else "doctor_dashboard")
                                    },
                                    label = {
                                        Text(
                                            text = if (mode == UserMode.PATIENT) "Patient Mode" else "Doctor Mode",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (currentMode == UserMode.DOCTOR) {
                        OutlinedButton(
                            onClick = { onNavigateTo("settings") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Professional Settings")
                        }
                    }
                }
            }
        }

        // PERSONAL / HEALTH DETAILS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Personal Information")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileInfoRow("Blood Group", userProfile.bloodGroup)
                        ProfileInfoRow("Identity Proof", "Government Verified ID (${userProfile.maskedAadhaar})")
                        ProfileInfoRow("Date of Birth", userProfile.dob)
                        ProfileInfoRow("Allergies", userProfile.allergies.joinToString(", "))
                        ProfileInfoRow("Genetic / Family Risk", userProfile.familyHistory.joinToString(" • "))
                    }
                }
            }
        }

        // EMERGENCY CONTACT SHORTCUT
        item {
            Button(
                onClick = { onNavigateTo("emergency") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F0), contentColor = Color(0xFFCF1322)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Emergency, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Emergency Medical ID", fontWeight = FontWeight.Bold)
            }
        }

        // PRIVACY & ACCESS HISTORY
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "Privacy & Security",
                    subtitle = "Transparent log of authorized access and review activity"
                )

                accessHistory.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.accessedBy,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Text(
                                    text = "${item.accessType} • ${item.scope}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = item.timestamp,
                                style = MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}
