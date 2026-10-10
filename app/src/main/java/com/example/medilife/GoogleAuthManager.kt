package com.example.medilife

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class GoogleAccountData(
    val idToken: String,
    val name: String
)

class GoogleAuthManager(context: Context) {

    private val credentialManager = CredentialManager.create(context)

    private val activityContext = context

    suspend fun getGoogleAccount(): GoogleAccountData {

        val webClientId = runCatching {
            activityContext.getString(R.string.default_web_client_id)
        }.getOrElse {
            throw IllegalStateException(
                "default_web_client_id is missing. " +
                        "Make sure google-services.json contains the Web client OAuth client " +
                        "and the Google Services plugin is configured."
            )
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activityContext,
            request = request
        )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            val googleCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            return GoogleAccountData(
                idToken = googleCredential.idToken,
                name = googleCredential.displayName.orEmpty()
            )
        }

        throw IllegalStateException(
            "Google did not return a supported sign-in credential."
        )
    }
}