package com.example.medilife

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUid: String?
        get() = try {
            auth.currentUser?.uid ?: "demo_uid_123"
        } catch (e: Exception) {
            "demo_uid_123"
        }

    fun isUserSignedIn(): Boolean {
        return try {
            auth.currentUser != null
        } catch (e: Exception) {
            true // Default to demo user
        }
    }

    suspend fun signInDemoUser(): UserProfile {
        return UserProfile(
            uid = currentUid ?: "demo_uid_123",
            name = "Anjali Sharma",
            email = "anjali@medilife.ai",
            mode = UserMode.PATIENT,
            isDoctor = true
        )
    }

    suspend fun fetchPatientProfile(uid: String): UserProfile {
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("patientProfile").document("main").get().await()
            if (doc.exists()) {
                UserProfile(
                    uid = uid,
                    name = doc.getString("name") ?: "Anjali Sharma",
                    email = doc.getString("email") ?: "anjali@medilife.ai",
                    maskedAadhaar = doc.getString("maskedAadhaar") ?: "XXXX XXXX 4821",
                    bloodGroup = doc.getString("bloodGroup") ?: "O+",
                    dob = doc.getString("dob") ?: "1995-04-12"
                )
            } else {
                getSamplePatientProfile(uid)
            }
        } catch (e: Exception) {
            getSamplePatientProfile(uid)
        }
    }

    suspend fun fetchDoctorProfile(uid: String): DoctorProfile {
        return try {
            val doc = firestore.collection("doctors").document(uid).get().await()
            if (doc.exists()) {
                DoctorProfile(
                    uid = uid,
                    doctorName = doc.getString("doctorName") ?: "Dr. Ananya Rao",
                    specialty = doc.getString("specialty") ?: "General Physician",
                    regId = doc.getString("regId") ?: "MCI-847291"
                )
            } else {
                getSampleDoctorProfile(uid)
            }
        } catch (e: Exception) {
            getSampleDoctorProfile(uid)
        }
    }

    fun getSampleHealthMetrics(): List<HealthMetric> {
        return listOf(
            HealthMetric(name = "Steps", value = "7,420", unit = "steps", status = "Good", provenance = DataProvenance.CONNECTED_HEALTH),
            HealthMetric(name = "Heart Rate", value = "72", unit = "bpm", status = "Normal", provenance = DataProvenance.CONNECTED_HEALTH),
            HealthMetric(name = "Sleep", value = "7.5", unit = "hrs", status = "Optimal", provenance = DataProvenance.CONNECTED_HEALTH),
            HealthMetric(name = "Blood Pressure", value = "120/80", unit = "mmHg", status = "Normal", provenance = DataProvenance.PATIENT_ENTERED)
        )
    }

    fun getSampleMedicines(): List<Medicine> {
        return listOf(
            Medicine(
                id = "med_1",
                name = "Metformin",
                strength = "500 mg",
                frequency = "Twice Daily",
                nextDose = "8:00 PM Today",
                status = "PENDING",
                prescribingDoctor = "Dr. Ananya Rao",
                condition = "Blood Sugar Management"
            ),
            Medicine(
                id = "med_2",
                name = "Atorvastatin",
                strength = "10 mg",
                frequency = "Once Daily",
                nextDose = "10:00 PM Today",
                status = "PENDING",
                prescribingDoctor = "Dr. Ananya Rao",
                condition = "Cholesterol Control"
            ),
            Medicine(
                id = "med_3",
                name = "Vitamin D3",
                strength = "60,000 IU",
                frequency = "Weekly",
                nextDose = "Sunday Morning",
                status = "TAKEN",
                prescribingDoctor = "Dr. Ananya Rao",
                condition = "Vitamin D Deficiency"
            )
        )
    }

    fun getSampleRecords(): List<MedicalRecord> {
        return listOf(
            MedicalRecord(
                id = "rec_1",
                title = "Comprehensive Metabolic Panel & HbA1c",
                date = "Oct 2, 2026",
                type = "Lab Report",
                doctorName = "Dr. Ananya Rao",
                summary = "HbA1c level is 6.2% (Prediabetes). Fasting glucose 108 mg/dL. Renal function within normal limits.",
                provenance = DataProvenance.ORIGINAL_RECORD
            ),
            MedicalRecord(
                id = "rec_2",
                title = "Cardiology Follow-up Note",
                date = "Sep 18, 2026",
                type = "Doctor Note",
                doctorName = "Dr. Ananya Rao",
                summary = "Patient reports mild fatigue. BP stable at 120/80 mmHg. Advised continued diet control and daily 30-min exercise.",
                provenance = DataProvenance.DOCTOR_AUTHORED
            ),
            MedicalRecord(
                id = "rec_3",
                title = "Thyroid Profile (TSH, T3, T4)",
                date = "Aug 10, 2026",
                type = "Lab Report",
                doctorName = "Apollo Diagnostics",
                summary = "TSH: 2.4 uIU/mL (Normal). Free T4: 1.2 ng/dL.",
                provenance = DataProvenance.ORIGINAL_RECORD
            )
        )
    }

    fun getSampleAppointments(): List<Appointment> {
        return listOf(
            Appointment(
                id = "apt_1",
                doctorName = "Dr. Ananya Rao",
                specialty = "Cardiologist & General Physician",
                dateTime = "Tomorrow, 10:30 AM",
                type = "In-Person Consultation",
                location = "MediLife Wellness Center, Room 302"
            )
        )
    }

    fun getSampleConsents(): List<ConsentGrant> {
        return listOf(
            ConsentGrant(
                id = "grant_1",
                doctorName = "Dr. Ananya Rao",
                scopes = listOf("Lab Reports", "Medicines", "Allergies"),
                durationDays = 30,
                status = "APPROVED",
                requestedAt = "Oct 1, 2026"
            ),
            ConsentGrant(
                id = "grant_2",
                doctorName = "Dr. Vikram Sethi (Endocrinologist)",
                scopes = listOf("Medical History", "Lab Reports"),
                durationDays = 14,
                status = "PENDING",
                requestedAt = "Oct 5, 2026"
            )
        )
    }

    fun getSampleAccessHistory(): List<AccessHistoryItem> {
        return listOf(
            AccessHistoryItem(
                accessedBy = "Dr. Ananya Rao",
                accessType = "Viewed Lab Report",
                scope = "HbA1c Report",
                timestamp = "Oct 2, 2026, 4:15 PM"
            ),
            AccessHistoryItem(
                accessedBy = "Dr. Vikram Sethi",
                accessType = "Reviewed medication summary",
                scope = "Active Medicines & Allergies",
                timestamp = "Today, 11:30 AM"
            )
        )
    }

    fun getSampleCareMateHistory(): List<CareMateChatHistoryItem> {
        return listOf(
            CareMateChatHistoryItem(id = "c1", title = "HbA1c & Fasting Glucose Explanation", date = "Oct 5, 2026", preview = "Explanation of 6.2% prediabetes range result"),
            CareMateChatHistoryItem(id = "c2", title = "Metformin Dosage & Food Instructions", date = "Oct 3, 2026", preview = "Guidelines on taking Metformin 500mg with meals"),
            CareMateChatHistoryItem(id = "c3", title = "Penicillin Allergy & Safe Alternatives", date = "Sep 28, 2026", preview = "Listing safe antibiotic alternatives for allergic patients"),
            CareMateChatHistoryItem(id = "c4", title = "Daily Step Goal & Heart Rate Trends", date = "Sep 20, 2026", preview = "7,420 average step count activity summary")
        )
    }

    fun getSampleClinicalAIHistory(): List<CareMateChatHistoryItem> {
        return listOf(
            CareMateChatHistoryItem(id = "ca1", title = "Longitudinal Summary Review", date = "Oct 5, 2026", preview = "Prediabetes trend with adherence and risk flags"),
            CareMateChatHistoryItem(id = "ca2", title = "Medication Interaction Review", date = "Oct 2, 2026", preview = "Review of active medications and allergy constraints"),
            CareMateChatHistoryItem(id = "ca3", title = "Lab Trend Explanation", date = "Sep 28, 2026", preview = "HbA1c and fasting glucose trend interpretation")
        )
    }

    fun getSampleEmergencyMedicalProfile(): EmergencyMedicalProfile {
        return EmergencyMedicalProfile(
            id = "emergency_main",
            bloodGroup = "O+",
            criticalAllergies = listOf("Penicillin", "Dust Mites"),
            majorConditions = listOf("Prediabetes", "Mild Hypertension"),
            essentialMedicines = listOf("Metformin 500mg", "Atorvastatin 10mg"),
            emergencyContacts = listOf("Rahul Sharma • +91 9876543210"),
            enabled = true,
            lastUpdated = "Today, 9:45 AM"
        )
    }

    fun getSampleEmergencyAccessLogs(): List<EmergencyAccessLog> {
        return listOf(
            EmergencyAccessLog(
                id = "e1",
                doctorName = "Dr. Ananya Rao",
                patientName = "Anjali Sharma",
                reason = "Unconscious / Unresponsive Patient",
                duration = "1 Hour Break-Glass Access",
                timestamp = "Today, 6:45 PM",
                status = "AUDITED & LOGGED",
                scope = "Critical allergies • Blood group • Essential medicines",
                accessType = "Temporary Emergency Clinical Access"
            ),
            EmergencyAccessLog(
                id = "e2",
                doctorName = "Dr. Vikram Sethi",
                patientName = "Anjali Sharma",
                reason = "Medication Verification",
                duration = "15 Minutes",
                timestamp = "Yesterday, 9:10 AM",
                status = "EXPIRED",
                scope = "Current medication list",
                accessType = "Verified Emergency Clinician Access"
            )
        )
    }

    fun getSampleDoctorPatients(): List<DoctorPatientRecord> {
        return listOf(
            // Active Treatment Patients (Sorted by latest treatment date)
            DoctorPatientRecord(id = "p1", name = "Anjali Sharma", maskedId = "XXXX XXXX 4821", dob = "1995-04-12", bloodGroup = "O+", lastTreatmentDate = "2026-10-06", isTreatmentActive = true, activeConditions = "Prediabetes & Mild Hypertension"),
            DoctorPatientRecord(id = "p2", name = "Arjun Kumar", maskedId = "XXXX XXXX 9102", dob = "1988-11-20", bloodGroup = "A+", lastTreatmentDate = "2026-10-04", isTreatmentActive = true, activeConditions = "Type 2 Diabetes"),
            DoctorPatientRecord(id = "p3", name = "Meera Reddy", maskedId = "XXXX XXXX 3381", dob = "1992-03-05", bloodGroup = "B+", lastTreatmentDate = "2026-10-01", isTreatmentActive = true, activeConditions = "Thyroid Management"),

            // Completed Treatment Patients (Sorted by completion date)
            DoctorPatientRecord(id = "p4", name = "Rajesh Verma", maskedId = "XXXX XXXX 1140", dob = "1980-07-14", bloodGroup = "O+", lastTreatmentDate = "2026-09-28", isTreatmentActive = false, activeConditions = "Post-Surgical Follow-up (Completed)"),
            DoctorPatientRecord(id = "p5", name = "Sunita Kapoor", maskedId = "XXXX XXXX 7729", dob = "1975-01-29", bloodGroup = "AB+", lastTreatmentDate = "2026-09-15", isTreatmentActive = false, activeConditions = "Acute Bronchitis (Recovered)")
        )
    }

    private fun getSamplePatientProfile(uid: String) = UserProfile(
        uid = uid,
        name = "Anjali Sharma",
        email = "anjali@medilife.ai",
        mode = UserMode.PATIENT,
        isDoctor = true
    )

    private fun getSampleDoctorProfile(uid: String) = DoctorProfile(
        uid = uid,
        doctorName = "Dr. Ananya Rao",
        specialty = "General Physician & Cardiologist"
    )
}
