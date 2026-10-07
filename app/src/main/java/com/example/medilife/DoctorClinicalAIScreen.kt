package com.example.medilife

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorClinicalAIScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.clinicalAiMessages.collectAsState()
    val clinicalHistory by viewModel.clinicalAiHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedAttachments by remember { mutableStateOf<List<AttachmentInfo>>(emptyList()) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showHistoryDrawer by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val attachmentPickers = rememberChatAttachmentPickers { picked ->
        selectedAttachments = (selectedAttachments + picked).distinctBy { it.previewUri }.take(10)
    }

    val suggestedClinicalQueries = listOf(
        "Generate Longitudinal Summary",
        "Check Drug Interactions",
        "Lab Trend Analysis (HbA1c)",
        "Missing Screening Flags"
    )

    val robotOffset by rememberInfiniteTransition(label = "clinicalRobotFloat").animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clinicalRobotFloat"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(showHistoryDrawer) {
        if (showHistoryDrawer) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showHistoryDrawer,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(max = 320.dp),
                drawerShape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clinical AI History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                viewModel.createNewClinicalAIChat()
                                showHistoryDrawer = false
                            }) {
                                Icon(Icons.Outlined.Add, contentDescription = "New chat")
                            }
                            TextButton(onClick = { showHistoryDrawer = false }) { Text("Close") }
                        }
                    }
                    clinicalHistory.ifEmpty {
                        Text(
                            text = "No recent Clinical AI conversations yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    clinicalHistory.forEach { item ->
                        Surface(
                            onClick = { showHistoryDrawer = false },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = item.date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(36.dp)
                            .offset(y = robotOffset.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Clinical AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.createNewClinicalAIChat() },
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "New chat")
                    }
                    IconButton(onClick = { showHistoryDrawer = true }) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Clinical AI history")
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Patient: Anjali Sharma • Authorized records: Medicines • Labs • History • Allergies",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ClinicalMessageBubble(message = message)
                }
                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text(
                                text = "Analyzing patient records and context...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(suggestedClinicalQueries) { query ->
                    SuggestionChip(
                        onClick = { viewModel.sendDoctorClinicalAIQuery(query) },
                        label = { Text(query, fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            AttachmentPreviewStrip(
                attachments = selectedAttachments,
                onRemove = { attachment -> selectedAttachments = selectedAttachments - attachment },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    IconButton(onClick = { showAttachmentMenu = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add attachment")
                    }
                    AttachmentOptionsMenu(
                        expanded = showAttachmentMenu,
                        onDismiss = { showAttachmentMenu = false },
                        onUploadDocument = attachmentPickers.uploadDocuments,
                        onChoosePhoto = attachmentPickers.choosePhotos,
                        onTakePhoto = attachmentPickers.takePhoto
                    )
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Clinical AI...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() || selectedAttachments.isNotEmpty()) {
                            viewModel.sendDoctorClinicalAIQuery(inputText, attachments = selectedAttachments)
                            inputText = ""
                            selectedAttachments = emptyList()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }

}
@Composable
private fun ClinicalMessageBubble(message: AIChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .border(
                    width = if (message.isUser) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                message.attachments.forEach { attachment ->
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if ((attachment.type == "PHOTO" || attachment.type == "CAMERA") && attachment.previewUri.isNotBlank()) {
                            AttachmentThumbnail(attachment.previewUri, attachment.name)
                        } else {
                            Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = Color.White)
                        }
                        Text(attachment.name, color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
                if (!message.isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DataProvenanceChip(provenance = message.provenance)
                }
            }
        }
    }
}
