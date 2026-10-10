package com.example.medilife

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.ContactEmergency
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onNavigateTo: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val doctorProfile by viewModel.doctorProfile.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val appearanceMode by viewModel.appearanceMode.collectAsState()
    val authorizedContacts by viewModel.emergencyAuthorizedContacts.collectAsState()
    var showSignOutConfirmation by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactRelationship by remember { mutableStateOf("") }
    val context = LocalContext.current
    val networkPreferences = remember(context) {
        context.getSharedPreferences("medilife_network", android.content.Context.MODE_PRIVATE)
    }
    var backendUrl by remember {
        mutableStateOf(networkPreferences.getString("backend_base_url", "http://10.0.2.2:8000") ?: "http://10.0.2.2:8000")
    }
    var backendSaved by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (currentMode == UserMode.DOCTOR) "Professional account preferences" else "Personal health account preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsGroup(title = if (currentMode == UserMode.DOCTOR) "Professional Account" else "Account") {
            if (currentMode == UserMode.DOCTOR) {
                SettingsRow(Icons.Outlined.Work, "Professional Profile", "${doctorProfile.specialty} • ${doctorProfile.hospital}")
                SettingsRow(Icons.Outlined.AccountCircle, "Clinic Information", "Manage your professional details")
                SettingsRow(Icons.Outlined.Notifications, "Availability", "Appointment and availability preferences")
            } else {
                SettingsRow(Icons.Outlined.AccountCircle, "Account Information", userProfile.email)
                SettingsRow(Icons.Outlined.Person, "Personal Details", userProfile.name)
                SettingsRow(Icons.Outlined.VerifiedUser, "Identity & Verification", "Government ID •••• ${userProfile.maskedAadhaar.takeLast(4)}")
            }
        }

        if (currentMode == UserMode.PATIENT) {
            SettingsGroup(title = "Emergency Access") {
                SettingsRow(
                    Icons.Outlined.ContactEmergency,
                    "Emergency Medical ID",
                    "Blood group, allergies, medicines and contacts",
                    onClick = { onNavigateTo("emergency") }
                )
                SettingsRow(
                    Icons.Outlined.FamilyRestroom,
                    "Emergency Access History",
                    "Review emergency access events",
                    onClick = { onNavigateTo("emergency") }
                )
                Text(
                    "Authorized people can only see the categories selected below. Revoke access at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                authorizedContacts.forEach { contact ->
                    EmergencyContactPermissions(contact = contact, viewModel = viewModel)
                }
                OutlinedButton(
                    onClick = { showAddContactDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    androidx.compose.material3.Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                    Text("Add authorized contact", modifier = Modifier.padding(start = 8.dp))
                }
            }

            SettingsGroup(title = "Privacy & Security") {
                SettingsRow(Icons.Outlined.PrivacyTip, "Privacy Controls", "Manage how your health information is shared")
                SettingsRow(Icons.Outlined.Security, "Data Access History", "See who accessed your records", onClick = { onNavigateTo("profile") })
                SettingsRow(Icons.Outlined.VerifiedUser, "Consent Management", "Review and revoke data access", onClick = { onNavigateTo("consent") })
                SettingsRow(Icons.Outlined.Security, "Connected Health Data", "Manage connected devices and sources")
            }
        } else {
            SettingsGroup(title = "Professional Privacy & Access") {
                SettingsRow(Icons.Outlined.PrivacyTip, "Privacy & Security", "Professional account controls")
                SettingsRow(Icons.Outlined.Security, "Access Management", "Review patient access and consent", onClick = { onNavigateTo("consent") })
            }
        }

        SettingsGroup(title = "Backend Connection") {
            Text(
                "AI document analysis needs your computer's backend server to be reachable on the same Wi-Fi network.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = backendUrl,
                onValueChange = { backendUrl = it; backendSaved = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Backend base URL") },
                placeholder = { Text("http://192.168.1.5:8000") },
                singleLine = true
            )
            Text(
                "Physical phone: use your PC's IPv4 address, not 10.0.2.2. Android Emulator: use http://10.0.2.2:8000. Keep the backend running and allow port 8000 through Windows Firewall.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    val cleaned = backendUrl.trim().trimEnd('/')
                    if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
                        networkPreferences.edit().putString("backend_base_url", cleaned).apply()
                        backendUrl = cleaned
                        backendSaved = true
                    } else {
                        backendSaved = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save backend address") }
            if (backendSaved) {
                Text("Backend address saved. Try the attachment again.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }

        SettingsGroup(title = "App Preferences") {
            Text("Appearance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppearanceMode.entries.forEach { mode ->
                    FilterChip(
                        selected = appearanceMode == mode,
                        onClick = { viewModel.setAppearanceMode(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            SettingsRow(Icons.Outlined.Notifications, "Notifications", "Health reminders and account alerts")
            SettingsRow(Icons.Outlined.Language, "Language", "English")
            SettingsRow(Icons.Outlined.AccessibilityNew, "Accessibility", "Display and interaction preferences")
        }

        SettingsGroup(title = "Support") {
            SettingsRow(Icons.Outlined.SupportAgent, "Help & Support", "Get help with MediLife")
            SettingsRow(Icons.Outlined.SettingsSuggest, "About MediLife", "App information and policies")
        }

        OutlinedButton(
            onClick = { showSignOutConfirmation = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            title = { Text("Sign out of MediLife?") },
            text = { Text("You will need to sign in again to access your health records.") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutConfirmation = false
                    onSignOut()
                }) { Text("Sign Out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Add emergency contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newContactRelationship,
                        onValueChange = { newContactRelationship = it },
                        label = { Text("Relationship or profession") },
                        singleLine = true
                    )
                    Text("No health information is shared until you select permissions.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newContactName.isNotBlank() && newContactRelationship.isNotBlank(),
                    onClick = {
                        viewModel.addEmergencyContact(newContactName, newContactRelationship)
                        newContactName = ""
                        newContactRelationship = ""
                        showAddContactDialog = false
                    }
                ) { Text("Add contact") }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) {
            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 20.sp)
        }
    }
}

@Composable
private fun EmergencyContactPermissions(
    contact: EmergencyAuthorizedContact,
    viewModel: AppViewModel
) {
    val scopes = listOf("Blood Group", "Allergies", "Active Medicines", "Major Conditions", "Recent Reports", "Full Medical History")
    var expiryMenuExpanded by remember(contact.id) { mutableStateOf(false) }
    val expiryOptions = listOf("No expiry", "Oct 30, 2026", "Temporary • 24 hours")

    Surface(
        color = if (contact.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(contact.name, color = if (contact.active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Text(contact.relationship, color = if (contact.active) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Access expires: ${contact.expiresOn}", color = if (contact.active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { expiryMenuExpanded = true }, enabled = contact.active) { Text("Change", color = if (contact.active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
                DropdownMenu(expanded = expiryMenuExpanded, onDismissRequest = { expiryMenuExpanded = false }) {
                    expiryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.updateEmergencyExpiry(contact.id, option)
                                expiryMenuExpanded = false
                            }
                        )
                    }
                }
            }
            scopes.forEach { scope ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = scope in contact.allowedScopes,
                        onCheckedChange = { viewModel.updateEmergencyPermission(contact.id, scope, it) },
                        enabled = contact.active,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.onPrimary,
                            checkmarkColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            disabledCheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        scope,
                        color = if (contact.active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            TextButton(
                onClick = { viewModel.revokeEmergencyContact(contact.id) },
                enabled = contact.active,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (contact.active) "Revoke access" else "Access revoked", color = if (contact.active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}