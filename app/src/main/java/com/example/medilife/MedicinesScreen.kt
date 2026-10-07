package com.example.medilife

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
fun MedicinesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val medicines by viewModel.medicines.collectAsState()
    var expandedMedId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Medication Management",
            subtitle = "Track daily doses & adherence instructions"
        )

        if (medicines.isEmpty()) {
            EmptyStateView(message = "No medicines added yet.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(medicines) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        isExpanded = expandedMedId == medicine.id,
                        onExpandToggle = {
                            expandedMedId = if (expandedMedId == medicine.id) null else medicine.id
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateMedicineStatus(medicine.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicineCard(
    medicine: Medicine,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Surface(
        onClick = onExpandToggle,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${medicine.name} (${medicine.strength})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${medicine.frequency} • ${medicine.timing}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    DataProvenanceChip(provenance = medicine.provenance)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Bar (Mark Taken, Snooze, Skip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onStatusChange("TAKEN") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (medicine.status == "TAKEN") "Taken" else "Mark Taken", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onStatusChange("SNOOZED") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                    Text("Snooze", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onStatusChange("SKIPPED") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                    Text("Skip", fontSize = 12.sp)
                }
            }

            // EXPANDABLE DETAILS
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = Color.White.copy(alpha = 0.25f))

                    DetailLabelValue("Food Instructions", medicine.foodInstructions)
                    DetailLabelValue("Condition", medicine.condition)
                    DetailLabelValue("Prescribed By", medicine.prescribingDoctor)
                    DetailLabelValue("Purpose", medicine.purpose)
                    DetailLabelValue("Precautions", medicine.precautions)

                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Missed Dose Guidance: ${medicine.missedDoseGuidance}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLabelValue(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}
