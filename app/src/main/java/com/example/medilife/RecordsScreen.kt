package com.example.medilife

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.records.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedRecordId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Lab Reports", "Doctor Notes", "Prescriptions", "Vaccinations")

    val filteredRecords = if (selectedCategory == "All") {
        records
    } else {
        records.filter { it.type.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Health Records & History",
            subtitle = "Longitudinal medical history & verified documents"
        )

        // CATEGORY FILTER CHIPS
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 12.sp) }
                )
            }
        }

        if (filteredRecords.isEmpty()) {
            EmptyStateView(message = "No records found in this category.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredRecords) { record ->
                    RecordCard(
                        record = record,
                        isExpanded = expandedRecordId == record.id,
                        onToggleExpand = {
                            expandedRecordId = if (expandedRecordId == record.id) null else record.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordCard(
    record: MedicalRecord,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = document.startPage(pageInfo)
                val paint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                    isAntiAlias = true
                }
                val lines = listOf(
                    "MediLife - Medical Record",
                    "",
                    "Title: ${record.title}",
                    "Type: ${record.type}",
                    "Date: ${record.date}",
                    "Doctor: ${record.doctorName}",
                    "",
                    "Summary:",
                    record.summary
                )
                var y = 60f
                lines.forEach { line ->
                    // Wrap long lines to fit a printable page.
                    line.chunked(68).forEach { part ->
                        if (y < 800f) {
                            page.canvas.drawText(part, 40f, y, paint)
                            y += 24f
                        }
                    }
                }
                document.finishPage(page)
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    document.writeTo(output)
                } ?: throw IllegalStateException("Cannot create PDF output.")
                document.close()
                Toast.makeText(context, "PDF saved successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF export failed: ${e.localizedMessage ?: "unknown error"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Surface(
        onClick = onToggleExpand,
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = record.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "${record.type} • ${record.date}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
                            fontSize = 12.sp
                        )
                    }
                }
                DataProvenanceChip(provenance = record.provenance)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 13.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PROVIDER / DOCTOR",
                                style = MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = record.doctorName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val safeName = record.title.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)
                                pdfLauncher.launch("${safeName.ifBlank { "MediLife_Record" }}.pdf")
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White)
                        ) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
