package com.example.medilife

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

data class ChatAttachmentPickers(
    val uploadDocuments: () -> Unit,
    val choosePhotos: () -> Unit,
    val takePhoto: () -> Unit
)

@Composable
fun rememberChatAttachmentPickers(
    onAttachmentsSelected: (List<AttachmentInfo>) -> Unit
): ChatAttachmentPickers {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        onAttachmentsSelected(uris.map { context.attachmentInfo(it, "DOCUMENT") })
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        onAttachmentsSelected(uris.map { context.attachmentInfo(it, "PHOTO") })
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val uri = pendingCameraUri
        if (captured && uri != null) {
            onAttachmentsSelected(listOf(context.attachmentInfo(uri, "CAMERA")))
        } else if (uri != null) {
            context.contentResolver.delete(uri, null, null)
        }
    }

    return remember(context, documentPicker, photoPicker, camera, onAttachmentsSelected) {
        ChatAttachmentPickers(
            uploadDocuments = {
                documentPicker.launch(arrayOf("application/pdf", "image/*", "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            },
            choosePhotos = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            takePhoto = {
                val captureDirectory = File(context.cacheDir, "captures").apply { mkdirs() }
                val imageFile = File.createTempFile("medilife_capture_", ".jpg", captureDirectory)
                val cameraUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                pendingCameraUri = cameraUri
                camera.launch(cameraUri)
            }
        )
    }
}

private fun Context.attachmentInfo(uri: Uri, type: String): AttachmentInfo {
    val displayName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    return AttachmentInfo(
        name = displayName ?: uri.lastPathSegment ?: "Medical attachment",
        type = type,
        previewUri = uri.toString(),
        mimeType = contentResolver.getType(uri)
    )
}