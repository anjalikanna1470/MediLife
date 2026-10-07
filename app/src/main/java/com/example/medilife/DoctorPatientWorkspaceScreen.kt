package com.example.medilife

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DoctorPatientWorkspaceScreen(
    viewModel: AppViewModel,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val records by viewModel.records.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Overview", "History", "Medicines", "Allergies", "Reports")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // PATIENT AUTHORIZED HEADER
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                    Column {
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "DOB: ${userProfile.dob} • Blood: ${userProfile.bloodGroup} • ${userProfile.maskedAadhaar}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // CONSENT SCOPE BADGE
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(14.dp))
                Text(
                    text = "Active Consent Scope: Medical Records, Medicines, Lab Reports, Allergies",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1B5E20),
                    fontSize = 10.sp
                )
            }
        }

        // WORKSPACE TABS
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // TAB CONTENT
        when (selectedTab) {
            0 -> OverviewTabContent(userProfile, medicines, records, onNavigateTo)
            1 -> HistoryTabContent(records)
            2 -> MedicinesTabContent(medicines)
            3 -> AllergiesTabContent(userProfile)
            4 -> ReportsTabContent(records)
        }
    }
}

@Composable
private fun OverviewTabContent(
    profile: UserProfile,
    medicines: List<Medicine>,
    records: List<MedicalRecord>,
    onNavigateTo: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Button(
                onClick = { onNavigateTo("doctor_clinical_ai") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analyze Patient with Doctor Clinical AI")
            }
        }

        item {
            SectionHeader(title = "Clinical Summary")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "PRIMARY CONDITION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(text = "Prediabetes (HbA1c 6.2% on Oct 2, 2026)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "ACTIVE PRESCRIPTIONS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(text = medicines.joinToString { "${it.name} ${it.strength}" }, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun HistoryTabContent(records: List<MedicalRecord>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(records) { record ->
            CompactRecordRow(record = record, onClick = {})
        }
    }
}

@Composable
private fun MedicinesTabContent(medicines: List<Medicine>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(medicines) { med ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "${med.name} (${med.strength})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Prescribed for: ${med.condition} by ${med.prescribingDoctor}", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AllergiesTabContent(profile: UserProfile) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "CRITICAL ALLERGIES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        profile.allergies.forEach { allergy ->
            Surface(
                color = Color(0xFFFFF1F0),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ $allergy",
                    color = Color(0xFFCF1322),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ReportsTabContent(records: List<MedicalRecord>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(records.filter { it.type == "Lab Report" }) { record ->
            CompactRecordRow(record = record, onClick = {})
        }
    }
}

@Composable
private fun CompactRecordRow(record: MedicalRecord, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                DataProvenanceChip(provenance = record.provenance)
            }
            Text(text = record.summary, style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
