package com.example.medilife

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class PendingAuthUser(
    val uid: String,
    val name: String,
    val email: String,
    val role: UserMode
)

class AuthRepository {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val currentUserUid: String?
        get() = auth.currentUser?.uid

    fun currentUserName(): String {
        return auth.currentUser?.displayName?.trim().orEmpty()
    }

    fun currentUserEmail(): String {
        return auth.currentUser?.email?.trim().orEmpty()
    }

    suspend fun signInWithEmail(
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
            val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val firebaseUser = result.user
                ?: throw IllegalStateException("Unable to read the Firebase user.")

            return loadExistingMediLifeUser(
                uid = firebaseUser.uid,
                requestedMode = requestedMode,
                fallbackName = firebaseUser.displayName ?: cleanEmail,
                fallbackEmail = firebaseUser.email ?: cleanEmail
            )
        } catch (e: FirebaseAuthInvalidUserException) {
            throw IllegalStateException("No MediLife account was found with this email.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw IllegalStateException("Incorrect email or password.")
        } catch (e: Exception) {
            throw IllegalStateException(
                e.message ?: "Unable to sign in. Please try again."
            )
        }
    }

    suspend fun signInWithGoogle(
        idToken: String,
        requestedMode: UserMode
    ): AuthenticatedUser {
        if (idToken.isBlank()) {
            throw IllegalArgumentException("Google sign-in did not return a valid ID token.")
        }

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: throw IllegalStateException("Unable to read the Google Firebase user.")

            return loadExistingMediLifeUser(
                uid = firebaseUser.uid,
                requestedMode = requestedMode,
                fallbackName = firebaseUser.displayName ?: "MediLife User",
                fallbackEmail = firebaseUser.email ?: ""
            )
        } catch (e: Exception) {
            if (e is IllegalStateException) throw e
            throw IllegalStateException(
                e.message ?: "Google sign-in failed. Please try again."
            )
        }
    }

    suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        requestedMode: UserMode
    ): PendingAuthUser {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        if (cleanName.isBlank()) {
            throw IllegalArgumentException("Please enter your full name.")
        }
        if (cleanEmail.isBlank()) {
            throw IllegalArgumentException("Please enter your email address.")
        }
        if (password.length < 6) {
            throw IllegalArgumentException("Password must contain at least 6 characters.")
        }

        try {
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val firebaseUser = result.user
                ?: throw IllegalStateException("Unable to create the Firebase account.")

            updateFirebaseDisplayName(cleanName)

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(
                    mapOf(
                        "uid" to firebaseUser.uid,
                        "name" to cleanName,
                        "email" to cleanEmail,
                        "role" to requestedMode.name,
                        "authProvider" to "PASSWORD"
                    )
                )
                .await()

            return PendingAuthUser(
                uid = firebaseUser.uid,
                name = cleanName,
                email = cleanEmail,
                role = requestedMode
            )
        } catch (e: Exception) {
            throw IllegalStateException(
                e.message ?: "Unable to create the MediLife account."
            )
        }
    }

    suspend fun registerWithGoogle(
        idToken: String,
        requestedMode: UserMode
    ): PendingAuthUser {
        if (idToken.isBlank()) {
            throw IllegalArgumentException("Google sign-up did not return a valid ID token.")
        }

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: throw IllegalStateException("Unable to read the Google Firebase user.")

            val existing = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (existing.exists()) {
                auth.signOut()
                throw IllegalStateException(
                    "This Google account already has a MediLife account. Please use Login."
                )
            }

            val name = firebaseUser.displayName?.trim().orEmpty()
            val email = firebaseUser.email?.trim().orEmpty()

            if (email.isBlank()) {
                auth.signOut()
                throw IllegalStateException(
                    "Google did not provide an email address. Please use email/password registration."
                )
            }

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(
                    mapOf(
                        "uid" to firebaseUser.uid,
                        "name" to name.ifBlank { "MediLife User" },
                        "email" to email,
                        "role" to requestedMode.name,
                        "authProvider" to "GOOGLE",
                        "onboardingComplete" to false
                    )
                )
                .await()

            return PendingAuthUser(
                uid = firebaseUser.uid,
                name = name.ifBlank { "MediLife User" },
                email = email,
                role = requestedMode
            )
        } catch (e: Exception) {
            if (e is IllegalStateException) throw e
            throw IllegalStateException(
                e.message ?: "Google sign-up failed. Please try again."
            )
        }
    }

    suspend fun completePatientOnboarding(
        uid: String,
        name: String,
        email: String,
        dob: String,
        address: String,
        maskedAadhaar: String,
        bloodGroup: String,
        allergies: List<String>,
        familyHistory: List<String>,
        emergencyContactName: String,
        emergencyContactPhone: String,
        chronicConditions: List<String>,
        geneticConditions: List<String> = emptyList(),
        currentMedicines: List<String> = emptyList(),
        insuranceProvider: String = "",
        insurancePolicyLast4: String = ""
    ): AuthenticatedUser {
        validateCommonPatientData(
            name = name,
            dob = dob,
            address = address,
            bloodGroup = bloodGroup,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone
        )

        val cleanAllergies = cleanList(allergies)
        val cleanFamilyHistory = cleanList(familyHistory)
        val cleanConditions = cleanList(chronicConditions)

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "name" to name.trim(),
                    "email" to email.trim(),
                    "role" to "PATIENT",
                    "onboardingComplete" to true
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()

        firestore.collection("users")
            .document(uid)
            .collection("patientProfile")
            .document("main")
            .set(
                mapOf(
                    "uid" to uid,
                    "name" to name.trim(),
                    "email" to email.trim(),
                    "dob" to dob.trim(),
                    "address" to address.trim(),
                    "maskedAadhaar" to maskedAadhaar.trim(),
                    "bloodGroup" to bloodGroup.trim().uppercase(),
                    "allergies" to cleanAllergies,
                    "familyHistory" to cleanFamilyHistory,
                    "chronicConditions" to cleanConditions,
                    "geneticConditions" to cleanList(geneticConditions),
                    "currentMedicines" to cleanList(currentMedicines),
                    "insuranceProvider" to insuranceProvider.trim(),
                    "insurancePolicyLast4" to insurancePolicyLast4.trim(),
                    "emergencyContactName" to emergencyContactName.trim(),
                    "emergencyContactPhone" to emergencyContactPhone.trim()
                )
            )
            .await()

        return loadExistingMediLifeUser(
            uid = uid,
            requestedMode = UserMode.PATIENT,
            fallbackName = name,
            fallbackEmail = email
        )
    }

    suspend fun completeDoctorOnboarding(
        uid: String,
        name: String,
        email: String,
        specialty: String,
        registrationId: String,
        hospital: String,
        phone: String,
        city: String
    ): AuthenticatedUser {
        if (name.isBlank()) throw IllegalArgumentException("Please enter the doctor's name.")
        if (specialty.isBlank()) throw IllegalArgumentException("Please enter the specialty.")
        if (registrationId.isBlank()) throw IllegalArgumentException("Please enter the registration ID.")
        if (hospital.isBlank()) throw IllegalArgumentException("Please enter the hospital or clinic.")
        if (phone.isBlank()) throw IllegalArgumentException("Please enter the contact phone number.")

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "name" to name.trim(),
                    "email" to email.trim(),
                    "role" to "DOCTOR",
                    "onboardingComplete" to true
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()

        firestore.collection("doctors")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "doctorName" to name.trim(),
                    "specialty" to specialty.trim(),
                    "regId" to registrationId.trim(),
                    "hospital" to hospital.trim(),
                    "contactEmail" to email.trim(),
                    "phone" to phone.trim(),
                    "city" to city.trim()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()

        return loadExistingMediLifeUser(
            uid = uid,
            requestedMode = UserMode.DOCTOR,
            fallbackName = name,
            fallbackEmail = email
        )
    }

    suspend fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            throw IllegalArgumentException("Please enter your email address first.")
        }
        auth.sendPasswordResetEmail(cleanEmail).await()
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun loadExistingMediLifeUser(
        uid: String,
        requestedMode: UserMode,
        fallbackName: String,
        fallbackEmail: String
    ): AuthenticatedUser {
        val userDocument = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        if (!userDocument.exists()) {
            auth.signOut()
            throw IllegalStateException(
                "Your Firebase account exists, but your MediLife profile was not found. Please Register first."
            )
        }

        val role = when (
            userDocument.getString("role")?.trim()?.uppercase()
        ) {
            "PATIENT" -> UserMode.PATIENT
            "DOCTOR" -> UserMode.DOCTOR
            else -> {
                auth.signOut()
                throw IllegalStateException(
                    "Your MediLife profile has no valid role."
                )
            }
        }

        if (role != requestedMode) {
            auth.signOut()
            val requestedLabel =
                if (requestedMode == UserMode.PATIENT) "Patient" else "Doctor"
            throw IllegalStateException(
                "This account is registered as ${role.name}. Please use the $requestedLabel login."
            )
        }

        val name = userDocument.getString("name")
            ?: fallbackName.ifBlank { "MediLife User" }
        val email = userDocument.getString("email")
            ?: fallbackEmail

        if (role == UserMode.PATIENT) {
            val profile = firestore.collection("users")
                .document(uid)
                .collection("patientProfile")
                .document("main")
                .get()
                .await()

            if (!profile.exists()) {
                auth.signOut()
                throw IllegalStateException(
                    "Patient onboarding is incomplete. Please Register/complete your profile first."
                )
            }

            val userProfile = UserProfile(
                uid = uid,
                name = profile.getString("name") ?: name,
                email = profile.getString("email") ?: email,
                mode = UserMode.PATIENT,
                isDoctor = false,
                maskedAadhaar = profile.getString("maskedAadhaar") ?: "",
                bloodGroup = profile.getString("bloodGroup") ?: "",
                dob = profile.getString("dob") ?: "",
                address = profile.getString("address") ?: "",
                emergencyContactName = profile.getString("emergencyContactName") ?: "",
                emergencyContactPhone = profile.getString("emergencyContactPhone") ?: "",
                allergies = readStringList(profile.get("allergies")),
                familyHistory = readStringList(profile.get("familyHistory"))
            )

            return AuthenticatedUser(
                userProfile = userProfile,
                doctorProfile = null,
                role = UserMode.PATIENT
            )
        }

        val doctorDocument = firestore.collection("doctors")
            .document(uid)
            .get()
            .await()

        if (!doctorDocument.exists()) {
            auth.signOut()
            throw IllegalStateException(
                "Doctor onboarding is incomplete. Please complete the doctor profile."
            )
        }

        val doctorProfile = DoctorProfile(
            uid = uid,
            doctorName = doctorDocument.getString("doctorName") ?: name,
            specialty = doctorDocument.getString("specialty")
                ?: doctorDocument.getString("Specialty")
                ?: "",
            regId = doctorDocument.getString("regId") ?: "",
            hospital = doctorDocument.getString("hospital") ?: "",
            contactEmail = doctorDocument.getString("contactEmail") ?: email
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
    }

    private suspend fun updateFirebaseDisplayName(name: String) {
        val user = auth.currentUser ?: return
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        user.updateProfile(request).await()
    }

    private fun validateCommonPatientData(
        name: String,
        dob: String,
        address: String,
        bloodGroup: String,
        emergencyContactName: String,
        emergencyContactPhone: String
    ) {
        if (name.isBlank()) throw IllegalArgumentException("Please enter your full name.")
        if (dob.isBlank()) throw IllegalArgumentException("Please enter your date of birth.")
        if (address.isBlank()) throw IllegalArgumentException("Please enter your address.")
        if (bloodGroup.isBlank()) throw IllegalArgumentException("Please select your blood group.")
        if (emergencyContactName.isBlank()) {
            throw IllegalArgumentException("Please enter an emergency contact name.")
        }
        if (emergencyContactPhone.isBlank()) {
            throw IllegalArgumentException("Please enter an emergency contact phone number.")
        }
    }

    private fun cleanList(items: List<String>): List<String> {
        return items
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun readStringList(value: Any?): List<String> {
        return (value as? List<*>)
            ?.mapNotNull { it?.toString()?.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }
}
