package com.example.medilife

import java.util.UUID

enum class UserMode {
    PATIENT, DOCTOR
}

enum class AppearanceMode {
    SYSTEM, LIGHT, DARK
}

enum class DataProvenance(val label: String) {
    ORIGINAL_RECORD("Original Medical Record"),
    DOCTOR_AUTHORED("Doctor Authored"),
    PATIENT_ENTERED("Patient Entered"),
    AI_GENERATED("AI Generated"),
    CONNECTED_HEALTH("Connected Health Data")
}

data class UserProfile(
    val uid: String = "demo_uid_123",
    val name: String = "Anjali Sharma",
    val email: String = "anjali@medilife.ai",
    val mode: UserMode = UserMode.PATIENT,
    val isDoctor: Boolean = false,
    val maskedAadhaar: String = "XXXX XXXX 4821",
    val bloodGroup: String = "O+",
    val dob: String = "1995-04-12",
    val address: String = "Hyderabad, Telangana",
    val emergencyContactName: String = "Rahul Sharma (Brother)",
    val emergencyContactPhone: String = "+91 9876543210",
    val allergies: List<String> = listOf("Penicillin", "Dust Mites"),
    val familyHistory: List<String> = listOf("Type 2 Diabetes (Maternal)", "Hypertension (Paternal)")
)

data class DoctorProfile(
    val uid: String = "doc_uid_456",
    val doctorName: String = "Dr. Ananya Rao",
    val specialty: String = "General Physician & Cardiologist",
    val regId: String = "MCI-847291",
    val hospital: String = "MediLife Wellness Center",
    val contactEmail: String = "dr.ananya@medilife.org"
)

data class HealthMetric(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Steps",
    val value: String = "7,420",
    val unit: String = "steps",
    val status: String = "Normal",
    val provenance: DataProvenance = DataProvenance.CONNECTED_HEALTH,
    val timestamp: String = "Today, 2:30 PM"
)

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Metformin",
    val strength: String = "500 mg",
    val frequency: String = "Twice Daily",
    val timing: String = "After Breakfast & Dinner",
    val foodInstructions: String = "Take with meals",
    val nextDose: String = "8:00 PM Today",
    val status: String = "PENDING", // PENDING, TAKEN, SNOOZED, SKIPPED
    val prescribingDoctor: String = "Dr. Ananya Rao",
    val condition: String = "Blood Sugar Management",
    val purpose: String = "Helps maintain optimal glycemic control",
    val precautions: String = "Avoid excessive alcohol intake",
    val sideEffects: String = "Mild stomach upset during initial days",
    val missedDoseGuidance: String = "Take as soon as remembered unless near next dose time. Consult clinician if unsure.",
    val provenance: DataProvenance = DataProvenance.DOCTOR_AUTHORED
)

data class MedicalRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "HbA1c & Lipid Profile Test",
    val date: String = "Oct 2, 2026",
    val type: String = "Lab Report",
    val doctorName: String = "Dr. Ananya Rao",
    val summary: String = "HbA1c level is 6.2% (Prediabetes range). Total cholesterol is 185 mg/dL.",
    val fileUrl: String = "",
    val provenance: DataProvenance = DataProvenance.ORIGINAL_RECORD
)

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val doctorName: String = "Dr. Ananya Rao",
    val specialty: String = "Cardiologist",
    val dateTime: String = "Tomorrow at 10:30 AM",
    val type: String = "In-Person Consultation",
    val location: String = "MediLife Clinic, Room 302",
    val status: String = "Confirmed"
)

data class ConsentGrant(
    val id: String = UUID.randomUUID().toString(),
    val patientUid: String = "",
    val patientName: String = "Priya Sharma",
    val doctorUid: String = "",
    val doctorName: String = "Dr. Ananya Rao",
    val scopes: List<String> = listOf("Medical Records", "Medicines", "Lab Reports", "Allergies"),
    val durationDays: Int = 30,
    val status: String = "APPROVED", // PENDING, APPROVED, DENIED, REVOKED
    val requestedAt: String = "Oct 5, 2026"
)

data class AccessHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val accessedBy: String = "Dr. Ananya Rao",
    val accessType: String = "Record View",
    val scope: String = "Lab Reports & Medicines",
    val timestamp: String = "Oct 5, 2026, 4:15 PM",
    val status: String = "Active Consent"
)

data class AttachmentInfo(
    val name: String,
    val type: String,
    val previewUri: String = "",
    val mimeType: String? = null
)

data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: String = "Just now",
    val attachment: AttachmentInfo? = null,
    val attachments: List<AttachmentInfo> = attachment?.let(::listOf) ?: emptyList(),
    val provenance: DataProvenance = DataProvenance.AI_GENERATED,
    val contextUsed: String = "Based on recent HbA1c lab report and active Metformin prescription",
    val confidence: String = "High",
    val limitations: String = "Informational assistance only. Consult your doctor for medical advice."
)

data class CareMateChatHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Blood Test Questions",
    val date: String = "Oct 5, 2026",
    val preview: String = "HbA1c level analysis and diet recommendations"
)

data class EmergencyMedicalProfile(
    val id: String = "emergency_main",
    val bloodGroup: String = "O+",
    val criticalAllergies: List<String> = listOf("Penicillin", "Dust Mites"),
    val majorConditions: List<String> = listOf("Prediabetes", "Mild Hypertension"),
    val essentialMedicines: List<String> = listOf("Metformin 500mg", "Atorvastatin 10mg"),
    val emergencyContacts: List<String> = listOf("Rahul Sharma • +91 9876543210"),
    val enabled: Boolean = true,
    val lastUpdated: String = "Today, 9:45 AM"
)

data class EmergencyAuthorizedContact(
    val id: String,
    val name: String,
    val relationship: String,
    val allowedScopes: Set<String>,
    val expiresOn: String = "No expiry",
    val active: Boolean = true
)

data class EmergencyAccessRequest(
    val id: String = UUID.randomUUID().toString(),
    val doctorName: String,
    val organization: String,
    val reason: String,
    val requestedAt: String,
    val status: String = "PENDING",
    val expiresAtMillis: Long = 0L
)

data class DoctorPatientRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Anjali Sharma",
    val maskedId: String = "XXXX XXXX 4821",
    val dob: String = "1995-04-12",
    val bloodGroup: String = "O+",
    val lastTreatmentDate: String = "2026-10-06",
    val isTreatmentActive: Boolean = true,
    val activeConditions: String = "Prediabetes & Mild Hypertension"
)

enum class EmergencyReason(val label: String) {
    UNCONSCIOUS_PATIENT("Unconscious / Unresponsive Patient"),
    CRITICAL_SYMPTOMS("Critical Acute Symptoms"),
    UNKNOWN_MEDICAL_HISTORY("Unknown Medical History"),
    MEDICATION_ALLERGY_VERIFICATION("Medication / Allergy Verification"),
    OTHER("Other Emergency Clinical Need")
}

data class EmergencyAccessLog(
    val id: String = UUID.randomUUID().toString(),
    val doctorName: String = "Dr. Ananya Rao",
    val patientName: String = "Anjali Sharma",
    val reason: String = "Unconscious / Unresponsive Patient",
    val duration: String = "1 Hour Break-Glass Access",
    val timestamp: String = "Today, 6:45 PM",
    val status: String = "AUDITED & LOGGED",
    val scope: String = "Critical allergies • Blood group • Essential medicines",
    val accessType: String = "Temporary Emergency Clinical Access",
    val organization: String = "MediLife Wellness Center"
)
