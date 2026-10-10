package com.example.medilife

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class AuthenticatedUser(
    val userProfile: UserProfile,
    val doctorProfile: DoctorProfile?,
    val role: UserMode
)

class FirebaseRepository {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val currentUid: String?
        get() = auth.currentUser?.uid

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Real Firebase Authentication login.
     *
     * requestedMode:
     * PATIENT -> user document must have role PATIENT
     * DOCTOR  -> user document must have role DOCTOR
     */
    suspend fun signIn(
        email: String,
        password: String,
        requestedMode: UserMode
    ): AuthenticatedUser {

        val cleanEmail = email.trim()

        if (cleanEmail.isBlank()) {
            throw IllegalArgumentException("Please enter your email address.")
        }

        if (password.isBlank()) {
            throw IllegalArgumentException("Please enter your password.")
        }

        try {
            // 1. Firebase Authentication
            val authResult = auth
                .signInWithEmailAndPassword(cleanEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase user was not created.")

            val uid = firebaseUser.uid

            // 2. Read users/{uid}
            val userDocument = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            if (!userDocument.exists()) {
                auth.signOut()

                throw IllegalStateException(
                    "Your Firebase account exists, but your MediLife profile was not found."
                )
            }

            val roleString = userDocument
                .getString("role")
                ?.trim()
                ?.uppercase()

            val role = when (roleString) {
                "PATIENT" -> UserMode.PATIENT
                "DOCTOR" -> UserMode.DOCTOR
                else -> {
                    auth.signOut()

                    throw IllegalStateException(
                        "Invalid MediLife role. Please contact the administrator."
                    )
                }
            }

            // 3. Prevent wrong login mode
            if (role != requestedMode) {
                auth.signOut()

                val expected = if (requestedMode == UserMode.PATIENT) {
                    "Patient"
                } else {
                    "Doctor"
                }

                throw IllegalStateException(
                    "This account is registered as $roleString. Please use the $expected login."
                )
            }

            val nameFromUser = userDocument.getString("name") ?: cleanEmail
            val emailFromUser = userDocument.getString("email") ?: cleanEmail

            // 4. Patient profile
            if (role == UserMode.PATIENT) {

                val profileDocument = firestore
                    .collection("users")
                    .document(uid)
                    .collection("patientProfile")
                    .document("main")
                    .get()
                    .await()

                if (!profileDocument.exists()) {
                    auth.signOut()

                    throw IllegalStateException(
                        "Patient profile is missing. Please complete patientProfile/main in Firestore."
                    )
                }

                val userProfile = UserProfile(
                    uid = uid,
                    name = profileDocument.getString("name")
                        ?: nameFromUser,
                    email = profileDocument.getString("email")
                        ?: emailFromUser,
                    mode = UserMode.PATIENT,
                    isDoctor = false,
                    maskedAadhaar = profileDocument.getString("maskedAadhaar")
                        ?: "",
                    bloodGroup = profileDocument.getString("bloodGroup")
                        ?: "",
                    dob = profileDocument.getString("dob")
                        ?: "",
                    address = profileDocument.getString("address")
                        ?: "",
                    emergencyContactName = profileDocument.getString(
                        "emergencyContactName"
                    ) ?: "",
                    emergencyContactPhone = profileDocument.getString(
                        "emergencyContactPhone"
                    ) ?: "",
                    allergies = profileDocument.get("allergies")
                        ?.let { value ->
                            (value as? List<*>)?.mapNotNull {
                                it?.toString()
                            } ?: emptyList()
                        }
                        ?: emptyList(),
                    familyHistory = profileDocument.get("familyHistory")
                        ?.let { value ->
                            (value as? List<*>)?.mapNotNull {
                                it?.toString()
                            } ?: emptyList()
                        }
                        ?: emptyList()
                )

                return AuthenticatedUser(
                    userProfile = userProfile,
                    doctorProfile = null,
                    role = UserMode.PATIENT
                )
            }

            // 5. Doctor profile
            val doctorDocument = firestore
                .collection("doctors")
                .document(uid)
                .get()
                .await()

            if (!doctorDocument.exists()) {
                auth.signOut()

                throw IllegalStateException(
                    "Doctor profile is missing. Please create doctors/$uid in Firestore."
                )
            }

            val doctorProfile = DoctorProfile(
                uid = uid,
                doctorName = doctorDocument.getString("doctorName")
                    ?: nameFromUser,
                specialty = doctorDocument.getString("specialty")
                    ?: doctorDocument.getString("Specialty")
                    ?: "",
                regId = doctorDocument.getString("regId")
                    ?: "",
                hospital = doctorDocument.getString("hospital")
                    ?: "",
                contactEmail = doctorDocument.getString("contactEmail")
                    ?: emailFromUser
            )

            val userProfile = UserProfile(
                uid = uid,
                name = doctorProfile.doctorName,
                email = doctorProfile.contactEmail,
                mode = UserMode.DOCTOR,
                isDoctor = true
            )

            return AuthenticatedUser(
                userProfile = userProfile,
                doctorProfile = doctorProfile,
                role = UserMode.DOCTOR
            )

        } catch (e: FirebaseAuthInvalidUserException) {
            throw IllegalStateException(
                "No Firebase account found with this email."
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw IllegalStateException(
                "Incorrect email or password."
            )
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(
                e.message ?: "Unable to sign in. Please try again."
            )
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun fetchPatientProfile(uid: String): UserProfile {

        val userDocument = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()

        val profileDocument = firestore
            .collection("users")
            .document(uid)
            .collection("patientProfile")
            .document("main")
            .get()
            .await()

        val userName = userDocument.getString("name") ?: ""
        val userEmail = userDocument.getString("email") ?: ""

        return UserProfile(
            uid = uid,
            name = profileDocument.getString("name") ?: userName,
            email = profileDocument.getString("email") ?: userEmail,
            mode = UserMode.PATIENT,
            isDoctor = false,
            maskedAadhaar = profileDocument.getString("maskedAadhaar") ?: "",
            bloodGroup = profileDocument.getString("bloodGroup") ?: "",
            dob = profileDocument.getString("dob") ?: "",
            address = profileDocument.getString("address") ?: "",
            emergencyContactName = profileDocument.getString(
                "emergencyContactName"
            ) ?: "",
            emergencyContactPhone = profileDocument.getString(
                "emergencyContactPhone"
            ) ?: "",
            allergies = profileDocument.get("allergies")
                ?.let { value ->
                    (value as? List<*>)?.mapNotNull {
                        it?.toString()
                    } ?: emptyList()
                }
                ?: emptyList(),
            familyHistory = profileDocument.get("familyHistory")
                ?.let { value ->
                    (value as? List<*>)?.mapNotNull {
                        it?.toString()
                    } ?: emptyList()
                }
                ?: emptyList()
        )
    }

    suspend fun fetchDoctorProfile(uid: String): DoctorProfile {

        val doc = firestore
            .collection("doctors")
            .document(uid)
            .get()
            .await()

        if (!doc.exists()) {
            throw IllegalStateException(
                "Doctor profile not found."
            )
        }

        return DoctorProfile(
            uid = uid,
            doctorName = doc.getString("doctorName") ?: "",
            specialty = doc.getString("specialty")
                ?: doc.getString("Specialty")
                ?: "",
            regId = doc.getString("regId") ?: "",
            hospital = doc.getString("hospital") ?: "",
            contactEmail = doc.getString("contactEmail") ?: ""
        )
    }

    // ----------------------------------------------------------------
    // Existing demo/sample data used by the current MediLife UI.
    // We keep these so the existing screens continue working.
    // ----------------------------------------------------------------

    fun getSampleHealthMetrics(): List<HealthMetric> {
        return listOf(
            HealthMetric(
                name = "Steps",
                value = "7,420",
                unit = "steps",
                status = "Good",
                provenance = DataProvenance.CONNECTED_HEALTH
            ),
            HealthMetric(
                name = "Heart Rate",
                value = "72",
                unit = "bpm",
                status = "Normal",
                provenance = DataProvenance.CONNECTED_HEALTH
            ),
            HealthMetric(
                name = "Sleep",
                value = "7.5",
                unit = "hrs",
                status = "Optimal",
                provenance = DataProvenance.CONNECTED_HEALTH
            ),
            HealthMetric(
                name = "Blood Pressure",
                value = "120/80",
                unit = "mmHg",
                status = "Normal",
                provenance = DataProvenance.PATIENT_ENTERED
            )
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
            CareMateChatHistoryItem(
                id = "c1",
                title = "HbA1c & Fasting Glucose Explanation",
                date = "Oct 5, 2026",
                preview = "Explanation of 6.2% prediabetes range result"
            ),
            CareMateChatHistoryItem(
                id = "c2",
                title = "Metformin Dosage & Food Instructions",
                date = "Oct 3, 2026",
                preview = "Guidelines on taking Metformin 500mg with meals"
            ),
            CareMateChatHistoryItem(
                id = "c3",
                title = "Penicillin Allergy & Safe Alternatives",
                date = "Sep 28, 2026",
                preview = "Listing safe antibiotic alternatives for allergic patients"
            ),
            CareMateChatHistoryItem(
                id = "c4",
                title = "Daily Step Goal & Heart Rate Trends",
                date = "Sep 20, 2026",
                preview = "7,420 average step count activity summary"
            )
        )
    }

    fun getSampleClinicalAIHistory(): List<CareMateChatHistoryItem> {
        return listOf(
            CareMateChatHistoryItem(
                id = "ca1",
                title = "Longitudinal Summary Review",
                date = "Oct 5, 2026",
                preview = "Prediabetes trend with adherence and risk flags"
            ),
            CareMateChatHistoryItem(
                id = "ca2",
                title = "Medication Interaction Review",
                date = "Oct 2, 2026",
                preview = "Review of active medications and allergy constraints"
            ),
            CareMateChatHistoryItem(
                id = "ca3",
                title = "Lab Trend Explanation",
                date = "Sep 28, 2026",
                preview = "HbA1c and fasting glucose trend interpretation"
            )
        )
    }

    fun getSampleEmergencyMedicalProfile(): EmergencyMedicalProfile {
        return EmergencyMedicalProfile(
            id = "emergency_main",
            bloodGroup = "O+",
            criticalAllergies = listOf("Penicillin", "Dust Mites"),
            majorConditions = listOf("Prediabetes", "Mild Hypertension"),
            essentialMedicines = listOf(
                "Metformin 500mg",
                "Atorvastatin 10mg"
            ),
            emergencyContacts = listOf(
                "Rahul Sharma • +91 9876543210"
            ),
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
            DoctorPatientRecord(
                id = "p1",
                name = "Anjali Sharma",
                maskedId = "XXXX XXXX 4821",
                dob = "1995-04-12",
                bloodGroup = "O+",
                lastTreatmentDate = "2026-10-06",
                isTreatmentActive = true,
                activeConditions = "Prediabetes & Mild Hypertension"
            ),
            DoctorPatientRecord(
                id = "p2",
                name = "Arjun Kumar",
                maskedId = "XXXX XXXX 9102",
                dob = "1988-11-20",
                bloodGroup = "A+",
                lastTreatmentDate = "2026-10-04",
                isTreatmentActive = true,
                activeConditions = "Type 2 Diabetes"
            ),
            DoctorPatientRecord(
                id = "p3",
                name = "Meera Reddy",
                maskedId = "XXXX XXXX 3381",
                dob = "1992-03-05",
                bloodGroup = "B+",
                lastTreatmentDate = "2026-10-01",
                isTreatmentActive = true,
                activeConditions = "Thyroid Management"
            ),
            DoctorPatientRecord(
                id = "p4",
                name = "Rajesh Verma",
                maskedId = "XXXX XXXX 1140",
                dob = "1980-07-14",
                bloodGroup = "O+",
                lastTreatmentDate = "2026-09-28",
                isTreatmentActive = false,
                activeConditions = "Post-Surgical Follow-up (Completed)"
            ),
            DoctorPatientRecord(
                id = "p5",
                name = "Sunita Kapoor",
                maskedId = "XXXX XXXX 7729",
                dob = "1975-01-29",
                bloodGroup = "AB+",
                lastTreatmentDate = "2026-09-15",
                isTreatmentActive = false,
                activeConditions = "Acute Bronchitis (Recovered)"
            )
        )
    }
    // ===============================
// AUTHENTICATION
// ===============================

    sealed class AuthResult {
        data class Success(
            val uid: String,
            val role: String,
            val isNewUser: Boolean = false
        ) : AuthResult()

        data class NeedsOnboarding(
            val uid: String,
            val role: String,
            val name: String,
            val email: String
        ) : AuthResult()

        data class Error(
            val message: String
        ) : AuthResult()
    }
}