package com.example.medilife

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class DocumentAnalysisResult(
    val filename: String,
    val documentType: String,
    val date: String,
    val summary: String,
    val diagnoses: String,
    val medications: String,
    val labValues: String,
    val followUp: String,
    val uncertainFields: String
)

object DocumentAnalysisClient {
    // Android emulator -> backend running on the Windows host.
    // For a physical phone, replace this with your computer's LAN IP.
    private const val BASE_URL = "http://10.0.2.2:8000"
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun analyze(
        context: Context,
        attachment: AttachmentInfo,
        userId: String,
        language: String,
        question: String
    ): DocumentAnalysisResult {
        val uri = Uri.parse(attachment.previewUri)
        val mimeType = attachment.mimeType
            ?: context.contentResolver.getType(uri)
            ?: guessMimeType(attachment.name)
        if (mimeType !in listOf("application/pdf", "image/jpeg", "image/png", "image/webp")) {
            throw IllegalArgumentException("Supported formats are PDF, JPG, PNG, and WEBP. This file is $mimeType.")
        }

        val tempFile = File.createTempFile("medilife_upload_", safeExtension(attachment.name), context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IOException("Cannot open ${attachment.name}. Please choose the file again.")

            if (tempFile.length() > 15L * 1024 * 1024) {
                throw IllegalArgumentException("File is larger than 15 MB. Please choose a smaller file.")
            }

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart("language", language)
                .addFormDataPart("question", question)
                .addFormDataPart(
                    "file",
                    attachment.name,
                    tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/documents/analyze")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val detail = runCatching {
                        JSONObject(responseText).optString("detail", responseText)
                    }.getOrDefault(responseText)
                    throw IOException("Backend error (${response.code}): $detail")
                }
                val json = JSONObject(responseText)
                return DocumentAnalysisResult(
                    filename = json.optString("filename", attachment.name),
                    documentType = json.optString("document_type"),
                    date = json.optString("date"),
                    summary = json.optString("summary"),
                    diagnoses = json.optJSONArray("diagnoses_or_findings").toStringList(),
                    medications = json.optJSONArray("medications").toDisplayList(),
                    labValues = json.optJSONArray("lab_values").toDisplayList(),
                    followUp = json.optJSONArray("follow_up_items").toStringList(),
                    uncertainFields = json.optJSONArray("uncertain_fields").toStringList()
                )
            }
        } finally {
            tempFile.delete()
        }
    }

    private fun guessMimeType(filename: String): String = when (filename.substringAfterLast('.', "").lowercase()) {
        "pdf" -> "application/pdf"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }

    private fun safeExtension(filename: String): String {
        val ext = filename.substringAfterLast('.', "bin").take(8).filter { it.isLetterOrDigit() }
        return ".$ext"
    }

    private fun org.json.JSONArray?.toStringList(): String {
        if (this == null) return ""
        return (0 until length()).mapNotNull { i ->
            when (val value = opt(i)) {
                is JSONObject -> value.toString()
                JSONObject.NULL -> null
                else -> value?.toString()
            }
        }.filter { it.isNotBlank() }.joinToString("\n• ", prefix = if (length() > 0) "• " else "")
    }

    private fun org.json.JSONArray?.toDisplayList(): String = toStringList()
}
