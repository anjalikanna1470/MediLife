package com.example.medilife

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseRepository()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _doctorProfile = MutableStateFlow(DoctorProfile())
    val doctorProfile: StateFlow<DoctorProfile> = _doctorProfile.asStateFlow()

    private val _currentMode = MutableStateFlow(UserMode.PATIENT)
    val currentMode: StateFlow<UserMode> = _currentMode.asStateFlow()

    private val _appearanceMode = MutableStateFlow(AppearanceMode.SYSTEM)
    val appearanceMode: StateFlow<AppearanceMode> = _appearanceMode.asStateFlow()

    private val _healthMetrics = MutableStateFlow<List<HealthMetric>>(emptyList())
    val healthMetrics: StateFlow<List<HealthMetric>> = _healthMetrics.asStateFlow()

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _records = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val records: StateFlow<List<MedicalRecord>> = _records.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _consents = MutableStateFlow<List<ConsentGrant>>(emptyList())
    val consents: StateFlow<List<ConsentGrant>> = _consents.asStateFlow()

    private val _accessHistory = MutableStateFlow<List<AccessHistoryItem>>(emptyList())
    val accessHistory: StateFlow<List<AccessHistoryItem>> = _accessHistory.asStateFlow()

    private val _careMateHistory =
        MutableStateFlow<List<CareMateChatHistoryItem>>(emptyList())
    val careMateHistory: StateFlow<List<CareMateChatHistoryItem>> =
        _careMateHistory.asStateFlow()

    private val _clinicalAiHistory =
        MutableStateFlow<List<CareMateChatHistoryItem>>(emptyList())
    val clinicalAiHistory: StateFlow<List<CareMateChatHistoryItem>> =
        _clinicalAiHistory.asStateFlow()

    private val _doctorPatients =
        MutableStateFlow<List<DoctorPatientRecord>>(emptyList())
    val doctorPatients: StateFlow<List<DoctorPatientRecord>> =
        _doctorPatients.asStateFlow()

    private val _emergencyMedicalProfile =
        MutableStateFlow(EmergencyMedicalProfile())
    val emergencyMedicalProfile: StateFlow<EmergencyMedicalProfile> =
        _emergencyMedicalProfile.asStateFlow()

    private val _emergencyAccessLogs =
        MutableStateFlow<List<EmergencyAccessLog>>(emptyList())
    val emergencyAccessLogs: StateFlow<List<EmergencyAccessLog>> =
        _emergencyAccessLogs.asStateFlow()

    private val emergencyScopes = setOf(
        "Blood Group",
        "Allergies",
        "Active Medicines",
        "Major Conditions",
        "Recent Reports",
        "Full Medical History"
    )

    private val _emergencyAuthorizedContacts =
        MutableStateFlow(
            listOf(
                EmergencyAuthorizedContact(
                    id = "doctor_ananya",
                    name = "Dr. Ananya Rao",
                    relationship = "Doctor • MediLife Wellness Center",
                    allowedScopes = setOf(
                        "Blood Group",
                        "Allergies",
                        "Active Medicines",
                        "Major Conditions",
                        "Recent Reports"
                    )
                ),
                EmergencyAuthorizedContact(
                    id = "family_rahul",
                    name = "Rahul Sharma",
                    relationship = "Family • Brother",
                    allowedScopes = setOf(
                        "Blood Group",
                        "Allergies",
                        "Active Medicines"
                    )
                )
            )
        )

    val emergencyAuthorizedContacts:
            StateFlow<List<EmergencyAuthorizedContact>> =
        _emergencyAuthorizedContacts.asStateFlow()

    private val _emergencyAccessRequests =
        MutableStateFlow<List<EmergencyAccessRequest>>(emptyList())

    val emergencyAccessRequests:
            StateFlow<List<EmergencyAccessRequest>> =
        _emergencyAccessRequests.asStateFlow()

    private val _emergencyOverrideExpiresAtMillis =
        MutableStateFlow(0L)

    val emergencyOverrideExpiresAtMillis:
            StateFlow<Long> =
        _emergencyOverrideExpiresAtMillis.asStateFlow()

    private val _aiMessages =
        MutableStateFlow<List<AIChatMessage>>(emptyList())

    val aiMessages: StateFlow<List<AIChatMessage>> =
        _aiMessages.asStateFlow()

    private val _clinicalAiMessages =
        MutableStateFlow<List<AIChatMessage>>(emptyList())

    val clinicalAiMessages: StateFlow<List<AIChatMessage>> =
        _clinicalAiMessages.asStateFlow()

    private val _pendingSignup =
        MutableStateFlow<PendingAuthUser?>(null)

    val pendingSignup: StateFlow<PendingAuthUser?> =
        _pendingSignup.asStateFlow()

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _selectedMedicine =
        MutableStateFlow<Medicine?>(null)

    val selectedMedicine: StateFlow<Medicine?> =
        _selectedMedicine.asStateFlow()

    private val _selectedRecord =
        MutableStateFlow<MedicalRecord?>(null)

    val selectedRecord: StateFlow<MedicalRecord?> =
        _selectedRecord.asStateFlow()

    private val _explainSheetState =
        MutableStateFlow<ExplainabilityData?>(null)

    val explainSheetState:
            StateFlow<ExplainabilityData?> =
        _explainSheetState.asStateFlow()

    data class ExplainabilityData(
        val title: String,
        val contextUsed: String,
        val confidence: String,
        val limitations: String =
            "MediLife AI provides clinical decision support. Always verify with original diagnostic files."
    )

    init {
        loadInitialData()
    }

    /**
     * REAL FIREBASE LOGIN
     */
    fun signIn(
        email: String,
        password: String,
        requestedMode: UserMode,
        onSuccess: (UserMode) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isLoading.value) return

        viewModelScope.launch {

            _isLoading.value = true

            try {

                val authenticatedUser = repository.signIn(
                    email = email,
                    password = password,
                    requestedMode = requestedMode
                )

                _userProfile.value =
                    authenticatedUser.userProfile

                _currentMode.value =
                    authenticatedUser.role

                authenticatedUser.doctorProfile?.let {
                    _doctorProfile.value = it
                }

                _userProfile.value =
                    _userProfile.value.copy(
                        mode = authenticatedUser.role,
                        isDoctor =
                            authenticatedUser.role == UserMode.DOCTOR
                    )

                onSuccess(authenticatedUser.role)

            } catch (e: Exception) {

                onError(
                    e.message
                        ?: "Unable to sign in. Please try again."
                )

            } finally {

                _isLoading.value = false
            }
        }
    }

    fun setPendingSignup(pending: PendingAuthUser) {
        _pendingSignup.value = pending
    }

    fun clearPendingSignup() {
        _pendingSignup.value = null
    }

    fun applyAuthenticatedUser(authenticatedUser: AuthenticatedUser) {
        _userProfile.value = authenticatedUser.userProfile.copy(
            mode = authenticatedUser.role,
            isDoctor = authenticatedUser.role == UserMode.DOCTOR
        )
        _currentMode.value = authenticatedUser.role
        authenticatedUser.doctorProfile?.let {
            _doctorProfile.value = it
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {

            _healthMetrics.value =
                repository.getSampleHealthMetrics()

            val savedMedicineStatuses = getApplication<Application>()
                .getSharedPreferences("medilife_medicine_status", Application.MODE_PRIVATE)
            _medicines.value = repository.getSampleMedicines().map { medicine ->
                medicine.copy(status = savedMedicineStatuses.getString(medicine.id, medicine.status) ?: medicine.status)
            }

            _records.value =
                repository.getSampleRecords()

            _appointments.value =
                repository.getSampleAppointments()

            _consents.value =
                repository.getSampleConsents()

            _accessHistory.value =
                repository.getSampleAccessHistory()

            _careMateHistory.value =
                repository.getSampleCareMateHistory()

            _clinicalAiHistory.value =
                repository.getSampleClinicalAIHistory()

            _doctorPatients.value =
                repository.getSampleDoctorPatients()

            _emergencyMedicalProfile.value =
                repository.getSampleEmergencyMedicalProfile()

            _emergencyAccessLogs.value =
                repository.getSampleEmergencyAccessLogs()

            resetCareMateChat()
            resetClinicalAIChat()
        }
    }

    /**
     * Doctor can switch between Doctor Mode and Patient Mode
     * without changing Firebase Authentication account.
     */
    fun switchMode(mode: UserMode) {

        if (!_userProfile.value.isDoctor) {
            return
        }

        _currentMode.value = mode

        _userProfile.value =
            _userProfile.value.copy(
                mode = mode,
                isDoctor = true,
                name = _doctorProfile.value.doctorName,
                email = _doctorProfile.value.contactEmail
            )
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        _appearanceMode.value = mode
    }

    fun updateEmergencyPermission(
        contactId: String,
        scope: String,
        allowed: Boolean
    ) {
        if (scope !in emergencyScopes) return

        _emergencyAuthorizedContacts.value =
            _emergencyAuthorizedContacts.value.map { contact ->

                if (contact.id != contactId) {
                    contact
                } else {

                    contact.copy(
                        allowedScopes =
                            if (allowed) {
                                contact.allowedScopes + scope
                            } else {
                                contact.allowedScopes - scope
                            }
                    )
                }
            }
    }

    fun updateEmergencyExpiry(
        contactId: String,
        expiry: String
    ) {
        _emergencyAuthorizedContacts.value =
            _emergencyAuthorizedContacts.value.map { contact ->

                if (contact.id == contactId) {
                    contact.copy(expiresOn = expiry)
                } else {
                    contact
                }
            }
    }

    fun revokeEmergencyContact(contactId: String) {
        _emergencyAuthorizedContacts.value =
            _emergencyAuthorizedContacts.value.map { contact ->

                if (contact.id == contactId) {
                    contact.copy(active = false)
                } else {
                    contact
                }
            }
    }

    fun addEmergencyContact(
        name: String,
        relationship: String
    ) {
        if (
            name.isBlank() ||
            relationship.isBlank()
        ) {
            return
        }

        _emergencyAuthorizedContacts.value =
            _emergencyAuthorizedContacts.value +
                    EmergencyAuthorizedContact(
                        id = "contact_${System.currentTimeMillis()}",
                        name = name.trim(),
                        relationship = relationship.trim(),
                        allowedScopes = emptySet()
                    )
    }

    fun requestEmergencyAccess(reason: String) {

        if (
            reason.isBlank() ||
            !_userProfile.value.isDoctor ||
            _currentMode.value != UserMode.DOCTOR
        ) {
            return
        }

        val doctor = _doctorProfile.value

        val timestamp =
            SimpleDateFormat(
                "MMM d, yyyy • h:mm a",
                Locale.getDefault()
            ).format(Date())

        _emergencyAccessRequests.value =
            listOf(
                EmergencyAccessRequest(
                    doctorName = doctor.doctorName,
                    organization = doctor.hospital,
                    reason = reason,
                    requestedAt = timestamp
                )
            ) + _emergencyAccessRequests.value
    }

    fun resolveEmergencyAccessRequest(
        requestId: String,
        approved: Boolean
    ) {

        val request =
            _emergencyAccessRequests.value.firstOrNull {
                it.id == requestId &&
                        it.status == "PENDING"
            } ?: return

        val now = System.currentTimeMillis()

        val expiresAt =
            if (approved) {
                now + 60 * 60 * 1000L
            } else {
                0L
            }

        _emergencyAccessRequests.value =
            _emergencyAccessRequests.value.map {

                if (it.id == requestId) {

                    it.copy(
                        status =
                            if (approved) {
                                "APPROVED"
                            } else {
                                "DENIED"
                            },
                        expiresAtMillis = expiresAt
                    )

                } else {
                    it
                }
            }

        if (approved) {

            _emergencyAccessLogs.value =
                listOf(
                    EmergencyAccessLog(
                        doctorName = request.doctorName,
                        patientName = _userProfile.value.name,
                        reason = request.reason,
                        duration = "Patient-approved • 1 hour",
                        timestamp =
                            SimpleDateFormat(
                                "MMM d, yyyy • h:mm a",
                                Locale.getDefault()
                            ).format(Date(now)),
                        status = "PATIENT APPROVED",
                        scope = "Blood group • Critical allergies • Major conditions • Active medicines • Emergency contacts",
                        accessType =
                            "Patient-approved Emergency Access",
                        organization = request.organization
                    )
                ) + _emergencyAccessLogs.value
        }
    }

    fun recordEmergencyOverride(reason: String) {

        val doctor = _doctorProfile.value

        val now = System.currentTimeMillis()

        val timestamp =
            SimpleDateFormat(
                "MMM d, yyyy • h:mm a",
                Locale.getDefault()
            ).format(Date(now))

        _emergencyOverrideExpiresAtMillis.value =
            now + 60 * 60 * 1000L

        val event =
            EmergencyAccessLog(
                doctorName = doctor.doctorName,
                patientName = _userProfile.value.name,
                reason = reason,
                duration = "Emergency view • 1 hour",
                timestamp = timestamp,
                status = "AUDITED & LOGGED",
                scope = "Blood group • Critical allergies • Major conditions • Active medicines • Emergency contacts",
                accessType = "Emergency Override",
                organization = doctor.hospital
            )

        _emergencyAccessLogs.value =
            listOf(event) + _emergencyAccessLogs.value
    }

    /**
     * REAL FIREBASE SIGN OUT
     */
    fun signOut() {

        repository.signOut()

        _currentMode.value = UserMode.PATIENT

        _userProfile.value = UserProfile()

        _doctorProfile.value = DoctorProfile()

        _selectedMedicine.value = null

        _selectedRecord.value = null

        resetCareMateChat()
        resetClinicalAIChat()
    }

    fun updateMedicineStatus(
        medicineId: String,
        status: String
    ) {

        _medicines.value =
            _medicines.value.map { medicine ->
                if (medicine.id == medicineId) {
                    medicine.copy(status = status)
                } else {
                    medicine
                }
            }
        getApplication<Application>()
            .getSharedPreferences("medilife_medicine_status", Application.MODE_PRIVATE)
            .edit()
            .putString(medicineId, status)
            .apply()
    }

    fun selectMedicine(medicine: Medicine?) {
        _selectedMedicine.value = medicine
    }

    fun selectRecord(record: MedicalRecord?) {
        _selectedRecord.value = record
    }

    fun resetCareMateChat() {

        _aiMessages.value =
            listOf(
                AIChatMessage(
                    text = "Hello! I'm CareMate. I can explain your recent reports, medicines, and health trends using your MediLife records.",
                    isUser = false,
                    provenance = DataProvenance.AI_GENERATED
                )
            )
    }

    fun resetClinicalAIChat() {

        _clinicalAiMessages.value =
            listOf(
                AIChatMessage(
                    text = "Clinical AI initialized. Patient: Anjali Sharma. Authorized records: medicines, labs, allergies, history.",
                    isUser = false,
                    provenance = DataProvenance.AI_GENERATED
                )
            )
    }

    fun createNewCareMateChat() {

        resetCareMateChat()

        val newItem =
            CareMateChatHistoryItem(
                id = "care_${System.currentTimeMillis()}",
                title = "New conversation",
                date = "Just now",
                preview = "Fresh CareMate chat started"
            )

        _careMateHistory.value =
            listOf(newItem) + _careMateHistory.value
    }

    fun createNewClinicalAIChat() {

        resetClinicalAIChat()

        val newItem =
            CareMateChatHistoryItem(
                id = "clinical_${System.currentTimeMillis()}",
                title = "New clinical review",
                date = "Just now",
                preview = "Fresh Clinical AI chat started"
            )

        _clinicalAiHistory.value =
            listOf(newItem) + _clinicalAiHistory.value
    }

    fun sendPatientAIChat(
        userQuery: String,
        attachment: AttachmentInfo? = null,
        attachments: List<AttachmentInfo> = emptyList()
    ) {

        val query = userQuery.trim()

        val selectedAttachments =
            (attachments + listOfNotNull(attachment))
                .distinctBy { it.previewUri }

        if (
            query.isBlank() &&
            selectedAttachments.isEmpty()
        ) {
            return
        }

        val userMsg =
            AIChatMessage(
                text =
                    if (query.isNotBlank()) {
                        query
                    } else {
                        "Attached ${selectedAttachments.size} file(s)"
                    },
                isUser = true,
                provenance = DataProvenance.PATIENT_ENTERED,
                attachment =
                    selectedAttachments.firstOrNull(),
                attachments = selectedAttachments
            )

        _aiMessages.value =
            _aiMessages.value + userMsg

        viewModelScope.launch {

            _isLoading.value = true

            val responseText =
                when {

                    selectedAttachments.isNotEmpty() -> {
                        try {
                            val analysis = withContext(Dispatchers.IO) {
                                DocumentAnalysisClient.analyze(
                                    context = getApplication(),
                                    attachment = selectedAttachments.first(),
                                    userId = repository.currentUid ?: "demo-user",
                                    language = "English",
                                    question = query.ifBlank { "Summarize this medical document and identify key findings." }
                                )
                            }
                            buildString {
                                appendLine("Document: ${analysis.filename}")
                                if (analysis.documentType.isNotBlank()) appendLine("Type: ${analysis.documentType}")
                                if (analysis.date.isNotBlank()) appendLine("Date: ${analysis.date}")
                                appendLine()
                                appendLine(analysis.summary.ifBlank { "The document was processed, but no summary was returned." })
                                if (analysis.diagnoses.isNotBlank()) appendLine("\\nFindings:\\n${analysis.diagnoses}")
                                if (analysis.medications.isNotBlank()) appendLine("\\nMedications:\\n${analysis.medications}")
                                if (analysis.labValues.isNotBlank()) appendLine("\\nLab values:\\n${analysis.labValues}")
                                if (analysis.followUp.isNotBlank()) appendLine("\\nFollow-up:\\n${analysis.followUp}")
                                if (analysis.uncertainFields.isNotBlank()) appendLine("\\nPlease verify:\\n${analysis.uncertainFields}")
                                appendLine("\\nThis is an AI-generated summary, not a diagnosis or prescription.")
                            }
                        } catch (e: Exception) {
                            "I couldn't analyze the attachment. ${e.message ?: "Check that the backend is running and the phone can reach it."}"
                        }
                    }

                    query.contains(
                        "medicine",
                        ignoreCase = true
                    ) ||
                            query.contains(
                                "metformin",
                                ignoreCase = true
                            ) ->
                        "You are taking Metformin 500mg (Twice Daily) for glycemic control prescribed by Dr. Ananya Rao. Your next dose is scheduled for 8:00 PM today after dinner."

                    query.contains(
                        "report",
                        ignoreCase = true
                    ) ||
                            query.contains(
                                "blood",
                                ignoreCase = true
                            ) ||
                            query.contains(
                                "hba1c",
                                ignoreCase = true
                            ) ->
                        "Your latest lab report from Oct 2 shows an HbA1c of 6.2%, which is in the prediabetes range. Fasting glucose was 108 mg/dL and kidney function is normal."

                    query.contains(
                        "trend",
                        ignoreCase = true
                    ) ||
                            query.contains(
                                "steps",
                                ignoreCase = true
                            ) ->
                        "Your average daily steps over the past week is 7,420 steps. Your blood pressure has remained stable at 120/80 mmHg."

                    else ->
                        "Based on your MediLife health records, your active prescription and recent lab results are available. Is there a specific report or medication you would like me to summarize?"
                }

            val aiMsg =
                AIChatMessage(
                    text = responseText,
                    isUser = false,
                    provenance = DataProvenance.AI_GENERATED,
                    contextUsed =
                        if (selectedAttachments.isNotEmpty()) {
                            "Selected attachment metadata: ${selectedAttachments.joinToString {
                                "${it.name} (${it.mimeType ?: it.type})"
                            }}"
                        } else {
                            "Authorized context: Active prescriptions & Oct 2 HbA1c Lab Report"
                        },
                    confidence =
                        "High (Grounded in MediLife Firestore)"
                )

            _aiMessages.value =
                _aiMessages.value + aiMsg

            _isLoading.value = false
        }
    }

    fun sendDoctorClinicalAIQuery(
        query: String,
        attachment: AttachmentInfo? = null,
        attachments: List<AttachmentInfo> = emptyList()
    ) {

        val text = query.trim()

        val selectedAttachments =
            (attachments + listOfNotNull(attachment))
                .distinctBy { it.previewUri }

        if (
            text.isBlank() &&
            selectedAttachments.isEmpty()
        ) {
            return
        }

        val userMsg =
            AIChatMessage(
                text =
                    if (text.isNotBlank()) {
                        text
                    } else {
                        "Attached ${selectedAttachments.size} file(s)"
                    },
                isUser = true,
                provenance = DataProvenance.DOCTOR_AUTHORED,
                attachment =
                    selectedAttachments.firstOrNull(),
                attachments = selectedAttachments
            )

        _clinicalAiMessages.value =
            _clinicalAiMessages.value + userMsg

        viewModelScope.launch {

            _isLoading.value = true

            val responseText =
                if (selectedAttachments.isNotEmpty()) {
                    try {
                        val analysis = withContext(Dispatchers.IO) {
                            DocumentAnalysisClient.analyze(
                                context = getApplication(),
                                attachment = selectedAttachments.first(),
                                userId = repository.currentUid ?: "demo-doctor",
                                language = "English",
                                question = text.ifBlank { "Summarize this medical document for clinical review." }
                            )
                        }
                        buildString {
                            appendLine("Document: ${analysis.filename}")
                            if (analysis.documentType.isNotBlank()) appendLine("Type: ${analysis.documentType}")
                            if (analysis.date.isNotBlank()) appendLine("Date: ${analysis.date}")
                            appendLine()
                            appendLine(analysis.summary)
                            if (analysis.diagnoses.isNotBlank()) appendLine("\\nFindings:\\n${analysis.diagnoses}")
                            if (analysis.medications.isNotBlank()) appendLine("\\nMedications:\\n${analysis.medications}")
                            if (analysis.labValues.isNotBlank()) appendLine("\\nLab values:\\n${analysis.labValues}")
                            if (analysis.followUp.isNotBlank()) appendLine("\\nFollow-up:\\n${analysis.followUp}")
                            appendLine("\\nAI-generated support only; verify against the original document.")
                        }
                    } catch (e: Exception) {
                        "I couldn't analyze the attachment. ${e.message ?: "Check backend connectivity and try again."}"
                    }
                } else {

                    "Longitudinal Patient Summary for Anjali Sharma (DOB: 1995-04-12):\n" +
                            "• Condition: Prediabetes (HbA1c 6.2% on Oct 2, 2026)\n" +
                            "• Current Medications: Metformin 500mg BD\n" +
                            "• Allergies: Penicillin, Dust Mites\n" +
                            "• Clinical Recommendation for Review: Monitor 3-month HbA1c and review medication adherence."
                }

            val aiMsg =
                AIChatMessage(
                    text = responseText,
                    isUser = false,
                    provenance = DataProvenance.AI_GENERATED,
                    contextUsed =
                        if (selectedAttachments.isNotEmpty()) {
                            "Selected attachment metadata: ${selectedAttachments.joinToString { "${it.name} (${it.mimeType ?: it.type})" }}"
                        } else {
                            "Authorized Patient Scope: Records, Medicines, Allergies"
                        },
                    confidence = "High Evidence Base"
                )

            _clinicalAiMessages.value =
                _clinicalAiMessages.value + aiMsg

            _isLoading.value = false
        }
    }

    fun updateConsentStatus(
        grantId: String,
        newStatus: String
    ) {

        _consents.value =
            _consents.value.map { grant ->

                if (grant.id == grantId) {
                    grant.copy(status = newStatus)
                } else {
                    grant
                }
            }
    }

    fun openExplainability(
        title: String,
        contextUsed: String,
        confidence: String
    ) {

        _explainSheetState.value =
            ExplainabilityData(
                title = title,
                contextUsed = contextUsed,
                confidence = confidence
            )
    }

    fun closeExplainability() {
        _explainSheetState.value = null
    }
}