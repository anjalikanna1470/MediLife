package com.example.medilife

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DoctorPatientsScreen(
    viewModel: AppViewModel,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val doctorPatients by viewModel.doctorPatients.collectAsState()

    val normalizedQuery = searchQuery.trim().lowercase()
    val activePatients = doctorPatients
        .filter { it.isTreatmentActive }
        .filter { if (normalizedQuery.isEmpty()) true else it.name.lowercase().contains(normalizedQuery) }
        .sortedByDescending { it.lastTreatmentDate }
    val completedPatients = doctorPatients
        .filter { !it.isTreatmentActive }
        .filter { if (normalizedQuery.isEmpty()) true else it.name.lowercase().contains(normalizedQuery) }
        .sortedByDescending { it.lastTreatmentDate }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SectionHeader(
                title = "Patient Directory",
                subtitle = "Active and completed treatment records sorted by treatment date"
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search patient by name") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Text(
                text = "ACTIVE TREATMENT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        items(activePatients.size) { index ->
            PatientRow(patient = activePatients[index], onNavigateTo = onNavigateTo)
        }

        item {
            Text(
                text = "COMPLETED TREATMENT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        items(completedPatients.size) { index ->
            PatientRow(patient = completedPatients[index], onNavigateTo = onNavigateTo)
        }
    }
}

@Composable
private fun PatientRow(
    patient: DoctorPatientRecord,
    onNavigateTo: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = Color.White
                    )
                    Text(
                        text = if (patient.isTreatmentActive) "Last treatment: ${patient.lastTreatmentDate}" else "Treatment completed: ${patient.lastTreatmentDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                    Text(
                        text = patient.activeConditions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 11.sp,
                        maxLines = 2
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
                Text("Open", fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}
