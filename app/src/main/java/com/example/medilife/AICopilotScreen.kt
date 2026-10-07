package com.example.medilife

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.SmartToy
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
fun AICopilotScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.aiMessages.collectAsState()
    val careMateHistory by viewModel.careMateHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val explainState by viewModel.explainSheetState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedAttachments by remember { mutableStateOf<List<AttachmentInfo>>(emptyList()) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showHistoryDrawer by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val attachmentPickers = rememberChatAttachmentPickers { picked ->
        selectedAttachments = (selectedAttachments + picked).distinctBy { it.previewUri }.take(10)
    }

    val suggestedQuestions = listOf(
        "Explain my latest report",
        "What medicines am I taking?",
        "What changed recently?",
        "Explain my HbA1c results"
    )

    val robotOffset by rememberInfiniteTransition(label = "caremateRobotFloat").animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "caremateRobotFloat"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    LaunchedEffect(showHistoryDrawer) {
        if (showHistoryDrawer) drawerState.open() else drawerState.close()
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
                    modifier = Modifier.fillMaxHeight().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CareMate History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                viewModel.createNewCareMateChat()
                                showHistoryDrawer = false
                            }) {
                                Icon(Icons.Outlined.Add, contentDescription = "New chat")
                            }
                            TextButton(onClick = { showHistoryDrawer = false }) { Text("Close") }
                        }
                    }
                    careMateHistory.ifEmpty {
                        Text(
                            text = "No recent CareMate conversations yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    careMateHistory.forEach { item ->
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
                        text = "CareMate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.createNewCareMateChat() },
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "New chat")
                    }
                    IconButton(onClick = { showHistoryDrawer = true }) {
                        Icon(Icons.Outlined.Menu, contentDescription = "CareMate history")
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
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "CareMate does not diagnose or prescribe. It grounds answers on your MediLife records.",
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
                    ChatMessageBubble(
                        message = message,
                        onWhyClick = {
                            viewModel.openExplainability(
                                title = "CareMate Answer Grounding",
                                contextUsed = message.contextUsed,
                                confidence = message.confidence
                            )
                        }
                    )
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
                                text = "CareMate is checking your authorized records...",
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
                items(suggestedQuestions) { question ->
                    SuggestionChip(
                        onClick = { viewModel.sendPatientAIChat(question) },
                        label = { Text(question, fontSize = 11.sp) },
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
                    placeholder = { Text("Ask CareMate anything about your health...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() || selectedAttachments.isNotEmpty()) {
                            viewModel.sendPatientAIChat(inputText, attachments = selectedAttachments)
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

    explainState?.let { sheet ->
        ExplainabilityBottomSheet(
            insightTitle = sheet.title,
            contextUsed = sheet.contextUsed,
            confidence = sheet.confidence,
            limitations = sheet.limitations,
            onDismiss = { viewModel.closeExplainability() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMessageBubble(
    message: AIChatMessage,
    onWhyClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .border(
                    width = if (message.isUser) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DataProvenanceChip(provenance = message.provenance)
                        TextButton(
                            onClick = onWhyClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Why am I seeing this?",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
