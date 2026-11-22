package io.github.hakunm.deepseekharness.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Queue
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import io.github.hakunm.deepseekharness.ApprovalUiState
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.PendingImageAttachment
import io.github.hakunm.deepseekharness.QuestionUiState
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.data.AgentPreset
import io.github.hakunm.deepseekharness.data.ChatDisplayItem
import io.github.hakunm.deepseekharness.data.ChatItemKind
import io.github.hakunm.deepseekharness.data.CommandDescriptor
import io.github.hakunm.deepseekharness.data.ModelSelection
import io.github.hakunm.deepseekharness.data.ModelView
import io.github.hakunm.deepseekharness.data.PendingApproval
import io.github.hakunm.deepseekharness.data.PendingQuestion
import io.github.hakunm.deepseekharness.data.QuestionAnswerItem
import io.github.hakunm.deepseekharness.data.SessionQueuedItem
import io.github.hakunm.deepseekharness.data.TodoItem
import io.github.hakunm.deepseekharness.data.displayItems
import io.github.hakunm.deepseekharness.data.permissionSelect
import io.github.hakunm.deepseekharness.data.todoItems
import io.github.hakunm.deepseekharness.ui.glass.CompactSegmentedControl
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.Calendar
import kotlin.math.max

@Composable
fun ChatScreen(
    state: HarnessState,
    viewModel: HarnessViewModel,
    wide: Boolean,
    onOpenDrawer: (() -> Unit)? = null,
    onCreateSession: () -> Unit = {},
    onCreateWorkspace: () -> Unit = {},
    onJumpToFiles: () -> Unit = {},
    onJumpToSettings: () -> Unit = {},
) {
    val selectedId = state.selectedSessionId
    BackHandler(enabled = !wide && selectedId != null) {
        viewModel.selectSession(null)
    }

    if (selectedId == null) {
        EmptyChatHome(
            state = state,
            viewModel = viewModel,
            onOpenDrawer = onOpenDrawer,
            onCreateSession = onCreateSession,
            onCreateWorkspace = onCreateWorkspace,
            onJumpToFiles = onJumpToFiles,
            onJumpToSettings = onJumpToSettings,
        )
    } else {
        ChatDetail(
            state = state,
            viewModel = viewModel,
            onOpenDrawer = onOpenDrawer,
            onCreateSession = onCreateSession,
            onJumpToFiles = onJumpToFiles,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Approved Prototype Screen 1:
 * 95% serene porcelain (#F6F8FB) / velvet night (#171715) canvas,
 * featherlight borderless top bar, centered DeepSeek Whale + time-aware greeting + workspace pill,
 * and floating bottom pill composer (`+` / input / `/` / `↑`).
 */
@Composable
private fun EmptyChatHome(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onOpenDrawer: (() -> Unit)?,
    onCreateSession: () -> Unit,
    onCreateWorkspace: () -> Unit,
    onJumpToFiles: () -> Unit,
    onJumpToSettings: () -> Unit,
) {
    val context = LocalContext.current
    val palette = LocalGlassPalette.current
    val canWrite = "chat.write" in state.device?.scopes.orEmpty()

    var draft by rememberSaveable { mutableStateOf("") }
    var homeWorkspaceId by rememberSaveable(state.chatWorkspaces.size) {
        mutableStateOf(state.chatWorkspaces.firstOrNull()?.id.orEmpty())
    }
    var homePresetId by rememberSaveable(state.agentPresets.size) {
        mutableStateOf(state.agentPresets.firstOrNull { it.isDefault && it.available }?.id.orEmpty())
    }
    var homePermission by rememberSaveable { mutableStateOf("workspace-write") }
    var addSheetOpen by remember { mutableStateOf(false) }
    var workspaceMenu by remember { mutableStateOf(false) }
    var presetSheetOpen by remember { mutableStateOf(false) }

    val selectedWorkspace = state.chatWorkspaces.find { it.id == homeWorkspaceId }
        ?: state.chatWorkspaces.firstOrNull()
    val hostLabel = remember(state.endpoint) { extractHostLabel(state.endpoint) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { picked ->
            decodeAndAttachImage(context, picked)?.let { (name, mime, bytes) ->
                viewModel.addPendingImage(name, mime, bytes)
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { bmp ->
            encodeCameraBitmap(bmp)?.let { bytes ->
                viewModel.addPendingImage("camera_${System.currentTimeMillis()}.jpg", "image/jpeg", bytes)
            }
        }
    }

    val defaultHomeCommands = remember {
        listOf(
            CommandDescriptor("plan", ""),
            CommandDescriptor("compact", ""),
            CommandDescriptor("goal", ""),
            CommandDescriptor("permission", ""),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
            .imePadding(),
    ) {
        MinimalTopBar(
            title = "",
            onNavigationClick = onOpenDrawer,
            trailing = {
                TopBarIconAction(
                    onClick = onCreateSession,
                    contentDescription = stringResource(R.string.new_session),
                    enabled = canWrite && !state.busy,
                ) {
                    NewChatOutlineIcon(size = 19.dp, tint = MaterialTheme.colorScheme.onBackground)
                }
            },
        )

        // Serene 95%-Whitespace Center Hero
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WhaleMark(size = 44.dp)

                Text(
                    text = timeAwareGreeting(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Box {
                    Row(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(CircleShape)
                            .background(palette.pillBackground, CircleShape)
                            .clickable {
                                if (state.chatWorkspaces.isEmpty()) onCreateWorkspace()
                                else workspaceMenu = true
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = hostLabel,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = "|",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Text(
                            text = selectedWorkspace?.title ?: stringResource(R.string.new_workspace),
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp),
                        )
                    }

                    DropdownMenu(expanded = workspaceMenu, onDismissRequest = { workspaceMenu = false }) {
                        state.chatWorkspaces.forEach { ws ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(ws.title)
                                        Text(
                                            "/${ws.path}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (ws.id == selectedWorkspace?.id) {
                                        Icon(Icons.Outlined.Check, null, Modifier.size(16.dp))
                                    }
                                },
                                onClick = {
                                    homeWorkspaceId = ws.id
                                    workspaceMenu = false
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_workspace)) },
                            leadingIcon = { Icon(Icons.Outlined.Add, null) },
                            onClick = {
                                workspaceMenu = false
                                onCreateWorkspace()
                            },
                        )
                    }
                }
            }
        }

        // Bottom Floating Pill Composer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CommandSuggestionsCard(
                value = draft,
                commands = state.commands.ifEmpty { defaultHomeCommands },
                onSelect = { draft = it },
            )
            MinimalPillComposer(
                value = draft,
                onValueChange = { draft = it },
                pendingImages = state.pendingImages,
                canWrite = canWrite,
                busy = state.busy,
                sessionRunning = false,
                onOpenAddSheet = { addSheetOpen = true },
                onSlashClick = {
                    draft = if (draft.startsWith("/")) "" else "/"
                },
                onRemoveImage = viewModel::removePendingImage,
                onCancelRun = {},
                onSteer = {},
                onSend = {
                    if (selectedWorkspace == null) {
                        onCreateWorkspace()
                    } else {
                        val toSend = draft
                        draft = ""
                        viewModel.sendFromHome(
                            workspaceId = selectedWorkspace.id,
                            preset = homePresetId.ifBlank { null },
                            text = toSend,
                            initialPermission = homePermission.takeUnless { it == "workspace-write" },
                        )
                    }
                },
            )
        }
    }

    if (addSheetOpen) {
        val activeProviderModel = state.providerSettings?.providers?.firstOrNull { it.active }?.displayName
            ?: state.agentPresets.find { it.id == homePresetId }?.name
            ?: stringResource(R.string.server_default)
        AddToChatSheet(
            workspaceLabel = selectedWorkspace?.let { "${it.title} (/${it.path})" }
                ?: stringResource(R.string.no_chat_workspaces),
            modelLabel = activeProviderModel,
            currentPermission = homePermission,
            permissionOptions = listOf("read-only", "workspace-write", "danger-full-access"),
            canWrite = canWrite && !state.busy,
            onCamera = {
                addSheetOpen = false
                cameraLauncher.launch(null)
            },
            onPhotos = {
                addSheetOpen = false
                photoPicker.launch("image/*")
            },
            onFiles = {
                addSheetOpen = false
                onJumpToFiles()
            },
            onWorkspaceClick = {
                addSheetOpen = false
                onCreateSession()
            },
            onModelClick = {
                addSheetOpen = false
                if (state.agentPresets.isNotEmpty()) presetSheetOpen = true else onJumpToSettings()
            },
            onPermissionSelect = { preset ->
                homePermission = preset
            },
            onClose = { addSheetOpen = false },
        )
    }

    if (presetSheetOpen) {
        HomePresetSheet(
            presets = state.agentPresets,
            selectedId = homePresetId,
            onSelect = {
                homePresetId = it
                presetSheetOpen = false
            },
            onJumpToSettings = {
                presetSheetOpen = false
                onJumpToSettings()
            },
            onClose = { presetSheetOpen = false },
        )
    }
}

/**
 * Approved Prototype Screen 4 & 5:
 * Active coding conversation with borderless assistant Markdown, soft pill user bubble,
 * whisper-quiet collapsible thinking line, pill tool chips, single-time approval card, and floating pill composer.
 */
@Composable
private fun ChatDetail(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onOpenDrawer: (() -> Unit)?,
    onCreateSession: () -> Unit,
    onJumpToFiles: () -> Unit,
    modifier: Modifier,
) {
    val selected = state.sessions.find { it.id == state.selectedSessionId }
    val palette = LocalGlassPalette.current
    if (selected == null) {
        Box(modifier.background(palette.canvasBackground), contentAlignment = Alignment.Center) {
            EmptyState(Icons.Outlined.Description, stringResource(R.string.select_session))
        }
        return
    }

    val context = LocalContext.current
    val canWrite = "chat.write" in state.device?.scopes.orEmpty()
    var message by rememberSaveable(selected.id) { mutableStateOf("") }
    var addSheetOpen by remember { mutableStateOf(false) }
    var modelSheet by remember(selected.id) { mutableStateOf(false) }
    var tasksExpanded by rememberSaveable(selected.id) { mutableStateOf(false) }
    var editingQueueItem by remember { mutableStateOf<SessionQueuedItem?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { picked ->
            decodeAndAttachImage(context, picked)?.let { (name, mime, bytes) ->
                viewModel.addPendingImage(name, mime, bytes)
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { bmp ->
            encodeCameraBitmap(bmp)?.let { bytes ->
                viewModel.addPendingImage("camera_${System.currentTimeMillis()}.jpg", "image/jpeg", bytes)
            }
        }
    }

    val liveItems = state.liveChat?.takeIf { it.sessionId == selected.id }?.displayItems().orEmpty()
    val displayItems = state.history?.displayItems().orEmpty() + liveItems
    val todos = state.history?.todoItems().orEmpty()
    val queuedItems = state.sessionState?.queue.orEmpty()
    val listState = rememberLazyListState()

    LaunchedEffect(displayItems.size, state.liveChat?.revision) {
        if (displayItems.isNotEmpty()) {
            if (liveItems.isNotEmpty()) listState.scrollToItem(displayItems.lastIndex)
            else listState.animateScrollToItem(displayItems.lastIndex)
        }
    }

    val sessionTitle = selected.title ?: selected.workspaceTitle ?: selected.cwd.ifBlank { selected.id.take(12) }

    Column(
        modifier = modifier
            .background(palette.canvasBackground)
            .imePadding(),
    ) {
        MinimalTopBar(
            title = sessionTitle,
            onNavigationClick = onOpenDrawer,
            statusDotColor = if (selected.running) MaterialTheme.colorScheme.primary else null,
            trailing = {
                TopBarIconAction(
                    onClick = onJumpToFiles,
                    contentDescription = stringResource(R.string.nav_code),
                ) {
                    Icon(Icons.Outlined.Code, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onBackground)
                }
                TopBarIconAction(
                    onClick = { viewModel.selectSession(null) },
                    contentDescription = stringResource(R.string.new_chat_cta),
                ) {
                    NewChatOutlineIcon(size = 18.5.dp, tint = MaterialTheme.colorScheme.onBackground)
                }
            },
        )

        if (todos.isNotEmpty()) {
            TodoMinimalBar(
                todos = todos,
                expanded = tasksExpanded,
                onToggle = { tasksExpanded = !tasksExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }

        // Conversation Stream on Pure Calm Canvas
        if (state.history != null && displayItems.isEmpty() && !selected.running) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WhaleMark(size = 42.dp)
                    Text(
                        text = timeAwareGreeting(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.pillBackground, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = extractHostLabel(state.endpoint),
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("|", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text(
                            text = selected.workspaceTitle ?: selected.cwd.ifBlank { "workspace" },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 14.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.history?.hasMore == true) {
                    item("load-older") {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TextButton(onClick = viewModel::loadOlderHistory) {
                                Text(stringResource(R.string.older_messages), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                items(displayItems, key = { it.id }) { item ->
                    ChatItem(item)
                }
            }
        }

        // Bottom Floating Approval / Question / Queue / Pill Composer Zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (queuedItems.isNotEmpty()) {
                QueuedTurnsMinimalCard(
                    items = queuedItems,
                    canWrite = canWrite && !state.busy,
                    onEdit = { editingQueueItem = it },
                    onSteer = { viewModel.steerQueueItem(it.id) },
                    onRemove = { viewModel.removeQueueItem(it.id) },
                )
            }

            val approvals = state.approvalState
            val questions = state.questionState
            when {
                approvals is ApprovalUiState.Pending && approvals.items.isNotEmpty() -> {
                    ApprovalMinimalCard(
                        approval = approvals.items.first(),
                        count = approvals.items.size,
                        deciding = false,
                        canWrite = canWrite,
                        onReject = { viewModel.decideApproval(approvals.items.first(), false) },
                        onAllowOnce = { viewModel.decideApproval(approvals.items.first(), true) },
                    )
                }
                approvals is ApprovalUiState.Deciding -> {
                    ApprovalMinimalCard(
                        approval = approvals.item,
                        count = approvals.items.size,
                        deciding = true,
                        canWrite = canWrite,
                        onReject = {},
                        onAllowOnce = {},
                    )
                }
                questions is QuestionUiState.Pending && questions.items.isNotEmpty() -> {
                    QuestionMinimalCard(
                        pending = questions.items.first(),
                        count = questions.items.size,
                        submitting = false,
                        canWrite = canWrite,
                        onSubmit = { answers -> viewModel.answerQuestion(questions.items.first(), answers) },
                    )
                }
                questions is QuestionUiState.Submitting -> {
                    QuestionMinimalCard(
                        pending = questions.item,
                        count = questions.items.size,
                        submitting = true,
                        canWrite = canWrite,
                        onSubmit = {},
                    )
                }
                approvals is ApprovalUiState.Loading -> {
                    GroupedCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        bordered = true,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 1.8.dp)
                            Text(stringResource(R.string.approval_loading), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                else -> {
                    CommandSuggestionsCard(
                        value = message,
                        commands = state.commands,
                        onSelect = { message = it },
                    )
                    MinimalPillComposer(
                        value = message,
                        onValueChange = { message = it },
                        pendingImages = state.pendingImages,
                        canWrite = canWrite,
                        busy = state.busy,
                        sessionRunning = selected.running,
                        onOpenAddSheet = { addSheetOpen = true },
                        onSlashClick = {
                            message = if (message.startsWith("/")) "" else "/"
                        },
                        onRemoveImage = viewModel::removePendingImage,
                        onCancelRun = viewModel::cancelRun,
                        onSteer = {
                            val toSend = message
                            message = ""
                            viewModel.sendMessage(toSend, true)
                        },
                        onSend = {
                            val toSend = message
                            message = ""
                            viewModel.sendMessage(toSend, false)
                        },
                    )
                }
            }
        }
    }

    if (addSheetOpen) {
        val currentModel = state.sessionModels?.current
        val agentName = state.agentPresets.find { it.id == selected.agentPreset }?.name ?: selected.agentPreset
        val modelSummary = buildString {
            append((currentModel?.model ?: agentName).orEmpty().ifBlank { stringResource(R.string.model) })
            currentModel?.reasoningEffort?.takeIf(String::isNotBlank)?.let { append(" · $it") }
        }
        val permSelect = state.history?.permissionSelect(state.permissionCatalog)
        val permOptions = permSelect?.options
            ?.map { it.value }
            ?.filterNot { it == "custom" }
            ?.ifEmpty { null }
            ?: listOf("read-only", "workspace-write", "danger-full-access")

        AddToChatSheet(
            workspaceLabel = selected.workspaceTitle ?: selected.cwd.ifBlank { "DSH Workspace" },
            modelLabel = modelSummary,
            currentPermission = permSelect?.currentValue ?: "workspace-write",
            permissionOptions = permOptions,
            canWrite = canWrite && !state.busy,
            onCamera = {
                addSheetOpen = false
                cameraLauncher.launch(null)
            },
            onPhotos = {
                addSheetOpen = false
                photoPicker.launch("image/*")
            },
            onFiles = {
                addSheetOpen = false
                onJumpToFiles()
            },
            onWorkspaceClick = {
                addSheetOpen = false
                onCreateSession()
            },
            onModelClick = {
                addSheetOpen = false
                modelSheet = true
            },
            onPermissionSelect = { preset ->
                viewModel.selectPermissionPreset(preset)
            },
            onClose = { addSheetOpen = false },
        )
    }

    if (modelSheet) {
        ModelSheet(state, viewModel) { modelSheet = false }
    }

    editingQueueItem?.let { item ->
        var draft by remember(item.id) { mutableStateOf(item.displayText()) }
        AlertDialog(
            onDismissRequest = { editingQueueItem = null },
            title = { Text(stringResource(R.string.queue_edit_title), style = MaterialTheme.typography.titleLarge) },
            text = { GlassOutlinedField(draft, { draft = it }, stringResource(R.string.message_hint), singleLine = false) },
            confirmButton = {
                Button(
                    onClick = { viewModel.editQueueItem(item.id, draft.trim()); editingQueueItem = null },
                    enabled = draft.isNotBlank(),
                    shape = CircleShape,
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { editingQueueItem = null }) { Text(stringResource(R.string.close)) } },
        )
    }
}

/**
 * Approved Prototype Bottom Floating Pill Composer:
 * 48dp capsule (#FFFFFF in Light, #232320 in Dark, 1dp hairline border) with:
 * - Left 35dp circular `+` button
 * - Clean inline BasicTextField
 * - Right 35dp circular `/` command button + 35dp solid CTA circular `↑` Send (or `■` Stop) button
 */
@Composable
private fun MinimalPillComposer(
    value: String,
    onValueChange: (String) -> Unit,
    pendingImages: List<PendingImageAttachment>,
    canWrite: Boolean,
    busy: Boolean,
    sessionRunning: Boolean,
    onOpenAddSheet: () -> Unit,
    onSlashClick: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onCancelRun: () -> Unit,
    onSteer: () -> Unit,
    onSend: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val hasPayload = value.isNotBlank() || pendingImages.isNotEmpty()
    val multilineOrImages = pendingImages.isNotEmpty() || '\n' in value || value.length > 36
    val outerShape = if (multilineOrImages) RoundedCornerShape(24.dp) else CircleShape

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .clip(outerShape)
            .background(palette.composerBackground, outerShape)
            .border(1.dp, palette.composerBorder, outerShape)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (pendingImages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                pendingImages.forEach { image ->
                    PendingImageChip(image = image, onRemove = { onRemoveImage(image.id) })
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Left 35dp circular `+` button
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(palette.circleButton, CircleShape)
                    .clickable(enabled = canWrite && !busy, role = Role.Button, onClick = onOpenAddSheet)
                    .semantics { contentDescription = "+" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_to_chat),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            // Middle text input (Modifier.weight(1f) prevents right-button clipping)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 35.dp, max = 108.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_with_deepseek),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                )
            }

            // Steer pill when session is actively running and user has typed a steering message
            if (sessionRunning && hasPayload) {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(palette.accentSoft, CircleShape)
                        .clickable(enabled = canWrite && !busy, onClick = onSteer)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.steer),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.accentInk,
                    )
                }
            }

            // Right 35dp circular `/` command button
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(palette.circleButton, CircleShape)
                    .clickable(enabled = canWrite && !busy, role = Role.Button, onClick = onSlashClick)
                    .semantics { contentDescription = "/" },
                contentAlignment = Alignment.Center,
            ) {
                SlashCommandIcon(size = 16.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Right 35dp solid CTA circular `↑` Send or `■` Stop button
            if (sessionRunning && !hasPayload) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(palette.ctaBackground, CircleShape)
                        .clickable(enabled = canWrite, role = Role.Button, onClick = onCancelRun)
                        .semantics { contentDescription = "Stop" },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Stop,
                        contentDescription = stringResource(R.string.cancel_run),
                        modifier = Modifier.size(17.dp),
                        tint = palette.ctaForeground,
                    )
                }
            } else {
                val sendEnabled = hasPayload && canWrite && !busy
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .alpha(if (sendEnabled) 1f else 0.36f)
                        .clip(CircleShape)
                        .background(palette.ctaBackground, CircleShape)
                        .clickable(enabled = sendEnabled, role = Role.Button, onClick = onSend),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.ArrowUpward,
                        contentDescription = stringResource(R.string.send),
                        modifier = Modifier.size(17.dp),
                        tint = palette.ctaForeground,
                    )
                }
            }
        }
    }
}

/**
 * Approved Prototype Screen 2 (`添加到对话` Bottom Sheet):
 * Top 28dp rounded sheet in #EFF3F8 / #1E1E1C with:
 * - 3-column rounded card grid: 相机 / 照片 / 工作区文件
 * - Grouped rounded cards: 所属工作区, 模型与深度思考, 沙箱权限 (with full-access risk dialog)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToChatSheet(
    workspaceLabel: String,
    modelLabel: String,
    currentPermission: String,
    permissionOptions: List<String>,
    canWrite: Boolean,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    onWorkspaceClick: () -> Unit,
    onModelClick: () -> Unit,
    onPermissionSelect: (String) -> Unit,
    onClose: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var permissionMenu by remember { mutableStateOf(false) }
    var confirmFullAccess by remember { mutableStateOf(false) }
    var acknowledged by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = palette.sheetBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 2.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(palette.switchOff, CircleShape),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header Row: Close X + Centered Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.close),
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = stringResource(R.string.add_to_chat),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            // 3-Column Rounded Card Grid: Camera / Photos / Workspace Files
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AddTileCard(
                    icon = Icons.Outlined.PhotoCamera,
                    label = stringResource(R.string.camera),
                    enabled = canWrite,
                    onClick = onCamera,
                    modifier = Modifier.weight(1f),
                )
                AddTileCard(
                    icon = Icons.Outlined.Image,
                    label = stringResource(R.string.photos),
                    enabled = canWrite,
                    onClick = onPhotos,
                    modifier = Modifier.weight(1f),
                )
                AddTileCard(
                    icon = Icons.Outlined.UploadFile,
                    label = stringResource(R.string.workspace_files),
                    enabled = true,
                    onClick = onFiles,
                    modifier = Modifier.weight(1f),
                )
            }

            // Card 1: Target Workspace
            GroupedCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                onClick = onWorkspaceClick,
            ) {
                SheetOptionRow(
                    iconContent = {
                        WorkspaceStackIcon(size = 17.5.dp, tint = MaterialTheme.colorScheme.onBackground)
                    },
                    title = stringResource(R.string.target_workspace),
                    subtitle = workspaceLabel,
                    showChevron = true,
                )
            }

            // Card 2: Model & Thinking + Sandbox Permission
            GroupedCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().clickable(onClick = onModelClick)) {
                        SheetOptionRow(
                            iconContent = {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            },
                            title = stringResource(R.string.deep_thinking_model),
                            subtitle = modelLabel,
                            showChevron = true,
                        )
                    }

                    HorizontalDivider(Modifier.padding(start = 62.dp), color = palette.divider)

                    Box(Modifier.fillMaxWidth()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canWrite) { permissionMenu = true },
                        ) {
                            SheetOptionRow(
                                iconContent = {
                                    Icon(
                                        permissionIcon(currentPermission),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onBackground,
                                    )
                                },
                                title = stringResource(R.string.sandbox_permission),
                                valueText = permissionLabel(currentPermission, null),
                                showChevron = true,
                            )
                        }

                        DropdownMenu(
                            expanded = permissionMenu,
                            onDismissRequest = { permissionMenu = false },
                        ) {
                            permissionOptions.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(permissionLabel(preset, null)) },
                                    leadingIcon = { Icon(permissionIcon(preset), null) },
                                    trailingIcon = {
                                        if (preset == currentPermission) Icon(Icons.Outlined.Check, null)
                                    },
                                    onClick = {
                                        permissionMenu = false
                                        if (preset == "danger-full-access") {
                                            acknowledged = false
                                            confirmFullAccess = true
                                        } else {
                                            onPermissionSelect(preset)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmFullAccess) {
        AlertDialog(
            onDismissRequest = { confirmFullAccess = false; acknowledged = false },
            icon = { Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.permission_full_access_title), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.permission_full_access_warning), style = MaterialTheme.typography.bodySmall)
                    Row(
                        Modifier.fillMaxWidth().clickable { acknowledged = !acknowledged },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
                        Text(
                            stringResource(R.string.permission_full_access_ack),
                            Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmFullAccess = false; acknowledged = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmFullAccess = false
                        acknowledged = false
                        onPermissionSelect("danger-full-access")
                    },
                    enabled = acknowledged,
                    shape = CircleShape,
                ) {
                    Text(stringResource(R.string.enable_full_access))
                }
            },
        )
    }
}

@Composable
private fun AddTileCard(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalGlassPalette.current
    GroupedCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.45f),
        cornerRadius = 16.dp,
        onClick = if (enabled) onClick else null,
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(palette.circleButton, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = label,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SheetOptionRow(
    iconContent: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    showChevron: Boolean = false,
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.circleButton, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            iconContent()
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
        if (!valueText.isNullOrBlank()) {
            Text(
                text = valueText,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showChevron) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Approved Prototype Screen 4 Conversation Items:
 * - USER: Right-aligned soft borderless pill bubble
 * - ASSISTANT: Directly on the clean canvasBackground (zero heavy card box!)
 * - REASONING / CONTEXT: Whisper-quiet borderless single-line toggle + subtle 2dp left line when expanded
 * - TOOL: Compact pill chip
 */
@Composable
private fun ChatItem(item: ChatDisplayItem) {
    val palette = LocalGlassPalette.current
    when (item.kind) {
        ChatItemKind.USER -> {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                val shape = RoundedCornerShape(20.dp)
                Box(
                    modifier = Modifier
                        .widthIn(max = 285.dp)
                        .clip(shape)
                        .background(palette.userBubbleTint, shape)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    SelectionContainer {
                        Text(
                            text = item.body,
                            color = palette.userBubbleText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        ChatItemKind.ASSISTANT -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .padding(vertical = 2.dp),
            ) {
                Markdown(
                    content = item.body,
                    colors = markdownColor(
                        text = MaterialTheme.colorScheme.onBackground,
                        codeBackground = palette.cardBackground,
                        inlineCodeBackground = palette.pillBackground,
                        dividerColor = palette.divider,
                    ),
                    typography = calmMarkdownTypography(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        ChatItemKind.TOOL -> {
            val label = buildString {
                val base = item.title ?: stringResource(R.string.tool_call)
                append(base)
                item.body.takeIf(String::isNotBlank)?.let { summary ->
                    val firstLine = summary.lineSequence().first().trim()
                    if (firstLine.isNotEmpty() && !firstLine.equals(base, ignoreCase = true)) {
                        append("  ")
                        append(firstLine.take(44))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.pillBackground, CircleShape)
                    .padding(horizontal = 11.dp, vertical = 4.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Outlined.Code,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        else -> {
            ActivityWhisperRow(item)
        }
    }
}

@Composable
private fun ActivityWhisperRow(item: ChatDisplayItem) {
    val palette = LocalGlassPalette.current
    val expandable = item.body.isNotBlank()
    var expanded by remember(item.id) { mutableStateOf(false) }
    val headerLabel = when (item.kind) {
        ChatItemKind.REASONING -> stringResource(
            if (expanded) R.string.collapse_thinking else R.string.expand_thinking,
        )
        ChatItemKind.CONTEXT -> buildString {
            append(stringResource(R.string.context_injection))
            item.title?.takeIf(String::isNotBlank)?.let { append(" · $it") }
        }
        else -> item.title.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .then(if (expandable) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(vertical = 3.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = headerLabel,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium,
                color = palette.accentInk,
            )
            if (expandable) {
                Icon(
                    if (expanded) Icons.Outlined.ExpandMore else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = palette.accentInk,
                )
            }
        }

        AnimatedVisibility(expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 4.dp, bottom = 2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(palette.cardBorder, CircleShape),
                )
                Markdown(
                    content = item.body,
                    colors = markdownColor(
                        text = MaterialTheme.colorScheme.onSurfaceVariant,
                        codeBackground = palette.cardBackground,
                        inlineCodeBackground = palette.pillBackground,
                        dividerColor = palette.divider,
                    ),
                    typography = calmMarkdownTypography(muted = true),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                )
            }
        }
    }
}

/**
 * Approved Prototype Screen 5:
 * Floating 20dp rounded card for single-time command approval, preserving full-access risk confirmation.
 */
@Composable
private fun ApprovalMinimalCard(
    approval: PendingApproval,
    count: Int,
    deciding: Boolean,
    canWrite: Boolean,
    onReject: () -> Unit,
    onAllowOnce: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val fullAccess = approval.risk == "full-access"
    var showRiskDialog by rememberSaveable(approval.id) { mutableStateOf(false) }
    var acknowledged by rememberSaveable(approval.id) { mutableStateOf(false) }

    GroupedCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        bordered = true,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    if (fullAccess) Icons.Outlined.WarningAmber else Icons.Outlined.Build,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = if (fullAccess) palette.warningText else MaterialTheme.colorScheme.primary,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.approval_required),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (count > 1) stringResource(R.string.approval_count, approval.toolName, count) else approval.toolName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (deciding) CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 1.8.dp)
            }

            approval.reason?.takeIf(String::isNotBlank)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }

            approval.detail?.takeIf(String::isNotBlank)?.let { detail ->
                SelectionContainer {
                    Text(
                        text = detail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 104.dp)
                            .verticalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(11.dp))
                            .background(palette.pillBackground, RoundedCornerShape(11.dp))
                            .padding(horizontal = 11.dp, vertical = 9.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryPillButton(
                    onClick = onReject,
                    enabled = canWrite && !deciding,
                    height = 38.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.reject), style = MaterialTheme.typography.labelLarge)
                }
                PrimaryPillButton(
                    onClick = {
                        if (fullAccess) {
                            acknowledged = false
                            showRiskDialog = true
                        } else {
                            onAllowOnce()
                        }
                    },
                    enabled = canWrite && !deciding,
                    height = 38.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.allow_once), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showRiskDialog) {
        AlertDialog(
            onDismissRequest = { showRiskDialog = false; acknowledged = false },
            icon = { Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.confirm_full_access), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.full_access_warning), style = MaterialTheme.typography.bodySmall)
                    Row(
                        Modifier.fillMaxWidth().clickable { acknowledged = !acknowledged },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
                        Text(
                            stringResource(R.string.full_access_ack),
                            Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRiskDialog = false; acknowledged = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRiskDialog = false; onAllowOnce() },
                    enabled = acknowledged,
                    shape = CircleShape,
                ) {
                    Text(stringResource(R.string.confirm_allow_once))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionMinimalCard(
    pending: PendingQuestion,
    count: Int,
    submitting: Boolean,
    canWrite: Boolean,
    onSubmit: (List<QuestionAnswerItem>) -> Unit,
) {
    val palette = LocalGlassPalette.current
    val selectedById = remember(pending.id) { mutableStateMapOf<String, List<String>>() }
    val customById = remember(pending.id) { mutableStateMapOf<String, String>() }
    val canSubmit = pending.questions.all { q ->
        selectedById[q.id].orEmpty().isNotEmpty() || customById[q.id].orEmpty().trim().isNotEmpty()
    }

    GroupedCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        bordered = true,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.question_required), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (count > 1) {
                        Text(
                            stringResource(R.string.question_count, count),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (submitting) CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 1.8.dp)
            }

            pending.questions.forEach { item ->
                val chosen = selectedById[item.id].orEmpty()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.header?.takeIf(String::isNotBlank)?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(item.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    item.detail?.takeIf(String::isNotBlank)?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    if (item.options.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.options.forEach { opt ->
                                val active = opt.label in chosen
                                Surface(
                                    onClick = {
                                        selectedById[item.id] = if (item.multiSelect) {
                                            if (active) chosen - opt.label else chosen + opt.label
                                        } else {
                                            if (active) emptyList() else listOf(opt.label)
                                        }
                                    },
                                    shape = CircleShape,
                                    color = if (active) palette.ctaBackground else palette.pillBackground,
                                    contentColor = if (active) palette.ctaForeground else MaterialTheme.colorScheme.onBackground,
                                ) {
                                    Text(
                                        opt.label,
                                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }
                    }
                    GlassOutlinedField(
                        customById[item.id].orEmpty(),
                        { customById[item.id] = it },
                        stringResource(R.string.question_custom_answer),
                    )
                }
            }

            PrimaryPillButton(
                onClick = {
                    val answers = pending.questions.map { q ->
                        QuestionAnswerItem(
                            q.id,
                            selectedById[q.id].orEmpty(),
                            customById[q.id]?.trim()?.takeIf(String::isNotEmpty),
                        )
                    }
                    onSubmit(answers)
                },
                enabled = canWrite && !submitting && canSubmit,
                loading = submitting,
                height = 40.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.submit_answer), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun QueuedTurnsMinimalCard(
    items: List<SessionQueuedItem>,
    canWrite: Boolean,
    onEdit: (SessionQueuedItem) -> Unit,
    onSteer: (SessionQueuedItem) -> Unit,
    onRemove: (SessionQueuedItem) -> Unit,
) {
    GroupedCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        bordered = true,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.Queue, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.queued_messages, items.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.displayText().ifBlank { item.id },
                        Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    IconButton(onClick = { onSteer(item) }, enabled = canWrite, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Outlined.SubdirectoryArrowRight, stringResource(R.string.queue_steer), Modifier.size(14.dp))
                    }
                    IconButton(onClick = { onEdit(item) }, enabled = canWrite, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.queue_edit), Modifier.size(14.dp))
                    }
                    IconButton(onClick = { onRemove(item) }, enabled = canWrite, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.queue_remove), Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoMinimalBar(
    todos: List<TodoItem>,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalGlassPalette.current
    val completeCount = todos.count { it.status == "completed" }
    GroupedCard(
        modifier = modifier,
        cornerRadius = 14.dp,
        bordered = true,
    ) {
        Column(Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(Icons.Outlined.CheckCircle, null, Modifier.size(14.dp), tint = palette.accentEmerald)
                Text(stringResource(R.string.current_tasks), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(R.string.task_progress, completeCount, todos.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Icon(
                    if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.ExpandMore,
                    null,
                    Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.heightIn(max = 136.dp).verticalScroll(rememberScrollState())) {
                    HorizontalDivider(color = palette.divider)
                    todos.forEach { todo ->
                        val (icon, tint, statusLabel) = when (todo.status) {
                            "completed" -> Triple(Icons.Outlined.CheckCircle, palette.accentEmerald, stringResource(R.string.task_completed))
                            "in_progress" -> Triple(Icons.Outlined.Edit, MaterialTheme.colorScheme.primary, stringResource(R.string.task_in_progress))
                            else -> Triple(Icons.Outlined.RadioButtonUnchecked, MaterialTheme.colorScheme.onSurfaceVariant, stringResource(R.string.task_pending))
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Icon(icon, statusLabel, Modifier.size(13.5.dp).padding(top = 2.dp), tint = tint)
                            Text(
                                todo.content,
                                modifier = Modifier.weight(1f),
                                color = if (todo.status == "completed") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandSuggestionsCard(
    value: String,
    commands: List<CommandDescriptor>,
    onSelect: (String) -> Unit,
) {
    val query = value.removePrefix("/")
    val suggestions = if (value.startsWith("/") && query.none { it.isWhitespace() }) {
        commands.filter { it.name.startsWith(query, ignoreCase = true) }.take(5)
    } else emptyList()

    AnimatedVisibility(visible = suggestions.isNotEmpty()) {
        GroupedCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            bordered = true,
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            Column {
                Text(
                    stringResource(R.string.available_commands),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                suggestions.forEach { command ->
                    val supportingText = localizedCommandDescription(command) ?: command.description.ifBlank {
                        command.input?.hint?.let { stringResource(R.string.command_input_hint, it) }.orEmpty()
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect("/${command.name} ") }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        SlashCommandIcon(size = 15.dp, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text("/${command.name}", style = MaterialTheme.typography.labelMedium)
                            if (supportingText.isNotEmpty()) {
                                Text(
                                    supportingText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingImageChip(image: PendingImageAttachment, onRemove: () -> Unit) {
    val bitmap = remember(image.id) {
        runCatching { BitmapFactory.decodeByteArray(image.previewBytes, 0, image.previewBytes.size) }.getOrNull()
    }
    Box(Modifier.size(46.dp)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = image.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
            )
        } else {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Image, null, Modifier.size(16.dp)) }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(16.dp)
                .background(Color.Black.copy(alpha = 0.68f), CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Close, stringResource(R.string.remove_image), Modifier.size(10.dp), tint = Color.White)
        }
    }
}

@Composable
private fun localizedCommandDescription(command: CommandDescriptor): String? = when (command.name) {
    "compact" -> stringResource(R.string.command_compact_description)
    "export" -> stringResource(R.string.command_export_description)
    "feedback" -> stringResource(R.string.command_feedback_description)
    "goal" -> stringResource(R.string.command_goal_description)
    "permission" -> stringResource(R.string.command_permission_description)
    "plan" -> stringResource(R.string.command_plan_description)
    else -> null
}

@Composable
private fun permissionLabel(value: String?, fallback: String?): String = when (value) {
    "read-only" -> stringResource(R.string.permission_read_only)
    "workspace-write" -> stringResource(R.string.permission_workspace_write)
    "danger-full-access" -> stringResource(R.string.permission_full_access)
    "custom" -> stringResource(R.string.permission_custom)
    else -> fallback ?: value.orEmpty()
}

private fun permissionIcon(value: String?): ImageVector = when (value) {
    "workspace-write" -> Icons.Outlined.Edit
    "danger-full-access" -> Icons.Outlined.WarningAmber
    else -> Icons.Outlined.Lock
}

@Composable
private fun calmMarkdownTypography(muted: Boolean = false): com.mikepenz.markdown.model.MarkdownTypography {
    val bodyStyle = if (muted) {
        MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp)
    } else {
        MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp)
    }
    return markdownTypography(
        h1 = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold),
        h2 = TextStyle(fontSize = 15.5.sp, lineHeight = 21.5.sp, fontWeight = FontWeight.SemiBold),
        h3 = TextStyle(fontSize = 14.5.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
        h4 = TextStyle(fontSize = 14.sp, lineHeight = 19.5.sp, fontWeight = FontWeight.Medium),
        h5 = TextStyle(fontSize = 13.5.sp, lineHeight = 18.5.sp, fontWeight = FontWeight.Medium),
        h6 = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        text = bodyStyle,
        code = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 17.5.sp),
        inlineCode = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, lineHeight = 18.sp),
        quote = bodyStyle,
        paragraph = bodyStyle,
        ordered = bodyStyle,
        bullet = bodyStyle,
        list = bodyStyle,
        table = MaterialTheme.typography.bodySmall,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomePresetSheet(
    presets: List<AgentPreset>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onJumpToSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = palette.sheetBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.agent_mode), style = MaterialTheme.typography.headlineSmall)
            GroupedCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.fillMaxWidth()) {
                    presets.forEachIndexed { idx, preset ->
                        AgentPresetRow(
                            preset = preset,
                            selected = preset.id == selectedId,
                            enabled = preset.available,
                            onClick = { onSelect(preset.id) },
                        )
                        if (idx != presets.lastIndex) {
                            HorizontalDivider(color = palette.divider)
                        }
                    }
                }
            }
            SecondaryPillButton(
                onClick = onJumpToSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.model_providers), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ModelSheet(state: HarnessState, viewModel: HarnessViewModel, onClose: () -> Unit) {
    val palette = LocalGlassPalette.current
    val sessionModels = state.sessionModels
    val session = state.sessions.find { it.id == state.selectedSessionId }
    val currentPreset = session?.agentPreset ?: state.agentPresets.firstOrNull { it.isDefault && it.available }?.id
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = palette.sheetBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(stringResource(R.string.session_configuration), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.session_configuration_subtitle),
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(0.68f)) {
                if (state.agentPresets.isNotEmpty()) {
                    item("agent-heading") {
                        Text(
                            stringResource(R.string.agent_mode),
                            Modifier.padding(top = 3.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    items(state.agentPresets, key = { "agent:${it.id}" }) { preset ->
                        AgentPresetRow(
                            preset = preset,
                            selected = preset.id == currentPreset,
                            enabled = session?.blank == true && preset.available &&
                                "chat.write" in state.device?.scopes.orEmpty() && !state.busy,
                            onClick = { viewModel.selectAgentPreset(preset.id) },
                        )
                    }
                    item("agent-lock-note") {
                        Text(
                            if (session?.blank == true) stringResource(R.string.agent_change_before_first_message)
                            else stringResource(R.string.agent_locked_after_start),
                            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        HorizontalDivider(color = palette.divider)
                    }
                }
                if (sessionModels == null) {
                    item("model-loading") {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 1.8.dp)
                        }
                    }
                } else {
                    sessionModels.groups.forEach { group ->
                        item(group.id) {
                            Text(
                                group.name,
                                Modifier.padding(top = 12.dp, bottom = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        items(group.models, key = { "${group.id}:${it.id}" }) { model ->
                            val selected = sessionModels.current.provider == group.id && sessionModels.current.model == model.id
                            ModelRow(group.id, model, selected, sessionModels.current, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentPresetRow(preset: AgentPreset, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (selected) palette.cardBackground else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (preset.isDefault) stringResource(R.string.agent_default_format, preset.name) else preset.name,
                color = if (enabled || selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
            )
            preset.description?.let {
                Text(
                    it,
                    Modifier.padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        if (selected) Icon(Icons.Outlined.Check, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelRow(providerId: String, model: ModelView, selected: Boolean, current: ModelSelection, viewModel: HarnessViewModel) {
    val palette = LocalGlassPalette.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                viewModel.selectModel(ModelSelection(providerId, model.id, model.reasoning?.defaultEffort ?: current.reasoningEffort))
            }
            .background(if (selected) palette.cardBackground else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(model.name, style = MaterialTheme.typography.titleSmall)
                model.description?.let {
                    Text(
                        it,
                        Modifier.padding(top = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (selected) Icon(Icons.Outlined.Check, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
        }
        if (selected && !model.reasoning?.efforts.isNullOrEmpty()) {
            Text(
                stringResource(R.string.reasoning_effort),
                Modifier.padding(top = 8.dp, bottom = 5.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                model.reasoning.efforts.forEach { effort ->
                    val active = current.reasoningEffort == effort.id
                    Surface(
                        onClick = { viewModel.selectModel(ModelSelection(providerId, model.id, effort.id)) },
                        shape = CircleShape,
                        color = if (active) palette.ctaBackground else palette.pillBackground,
                        contentColor = if (active) palette.ctaForeground else MaterialTheme.colorScheme.onBackground,
                        border = if (active) null else BorderStroke(0.6.dp, palette.cardBorder),
                    ) {
                        Text(effort.name, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
    HorizontalDivider(color = palette.divider.copy(alpha = 0.6f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewSessionSheet(
    state: HarnessState,
    viewModel: HarnessViewModel,
    initialCreateWorkspace: Boolean = false,
    onCreated: () -> Unit = {},
    onClose: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    var workspaceId by remember(state.chatWorkspaces) { mutableStateOf(state.chatWorkspaces.firstOrNull()?.id.orEmpty()) }
    var createWorkspace by remember(initialCreateWorkspace) {
        mutableStateOf(initialCreateWorkspace || state.chatWorkspaces.isEmpty())
    }
    var presetId by remember(state.agentPresets) {
        mutableStateOf(state.agentPresets.firstOrNull { it.isDefault && it.available }?.id.orEmpty())
    }
    var workspaceMenu by remember { mutableStateOf(false) }
    var rootMenu by remember { mutableStateOf(false) }
    var presetMenu by remember { mutableStateOf(false) }
    LaunchedEffect(createWorkspace) {
        if (createWorkspace && state.workspacePickerDirectory == null && state.workspacePickerError == null) {
            viewModel.prepareWorkspacePicker()
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = palette.sheetBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 22.dp),
        ) {
            Text(stringResource(R.string.new_session), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(10.dp))
            CompactSegmentedControl(
                items = listOf(stringResource(R.string.existing_workspace), stringResource(R.string.new_workspace)),
                selectedIndex = if (createWorkspace) 1 else 0,
                onSelect = { createWorkspace = it == 1 },
            )

            if (!createWorkspace) {
                PickerHeading(stringResource(R.string.dhs_workspace))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { workspaceMenu = true },
                        enabled = state.chatWorkspaces.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(13.dp),
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, Modifier.size(16.dp))
                        Text(
                            state.chatWorkspaces.find { it.id == workspaceId }?.title ?: stringResource(R.string.no_chat_workspaces),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = workspaceMenu, onDismissRequest = { workspaceMenu = false }) {
                        state.chatWorkspaces.forEach { workspace ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(workspace.title)
                                        Text(
                                            "/${workspace.path}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                },
                                onClick = { workspaceId = workspace.id; workspaceMenu = false },
                            )
                        }
                    }
                }
            } else {
                PickerHeading(stringResource(R.string.authorized_root))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { rootMenu = true },
                        enabled = state.roots.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(13.dp),
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, Modifier.size(16.dp))
                        Text(
                            state.roots.find { it.id == state.workspacePickerRootId }?.label ?: stringResource(R.string.no_roots),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = rootMenu, onDismissRequest = { rootMenu = false }) {
                        state.roots.forEach { root ->
                            DropdownMenuItem(
                                text = { Text(root.label) },
                                onClick = { viewModel.selectWorkspacePickerRoot(root.id); rootMenu = false },
                            )
                        }
                    }
                }
                PickerHeading(stringResource(R.string.workspace_directory))
                GroupedCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { viewModel.loadWorkspacePickerDirectory(state.workspacePickerPath.substringBeforeLast('/', "")) },
                                enabled = state.workspacePickerPath.isNotEmpty() && !state.busy,
                                modifier = Modifier.size(30.dp),
                            ) { Icon(Icons.Outlined.ArrowUpward, stringResource(R.string.go_up), Modifier.size(15.dp)) }
                            Text(
                                "/${state.workspacePickerPath}",
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            )
                        }
                        val directories = state.workspacePickerDirectory?.entries.orEmpty().filter { it.kind == "directory" }
                        if (state.workspacePickerDirectory == null && state.workspacePickerError != null) {
                            Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                                WarningBanner(localizeError(state.workspacePickerError))
                            }
                        } else if (directories.isEmpty()) {
                            Text(
                                stringResource(R.string.no_subdirectories),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        } else {
                            HorizontalDivider(color = palette.divider)
                            directories.take(8).forEachIndexed { idx, directory ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.busy) { viewModel.loadWorkspacePickerDirectory(directory.path) }
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.Folder, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(directory.name, Modifier.weight(1f).padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium)
                                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (idx != minOf(directories.lastIndex, 7)) {
                                    HorizontalDivider(Modifier.padding(start = 40.dp), color = palette.divider)
                                }
                            }
                        }
                    }
                }
            }

            if (state.agentPresets.isNotEmpty()) {
                PickerHeading(stringResource(R.string.agent_mode))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { presetMenu = true },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(13.dp),
                    ) {
                        Icon(Icons.Outlined.Build, null, Modifier.size(15.dp))
                        Text(
                            state.agentPresets.find { it.id == presetId }?.name ?: stringResource(R.string.server_default),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = presetMenu, onDismissRequest = { presetMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.server_default)) }, onClick = { presetId = ""; presetMenu = false })
                        state.agentPresets.forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(preset.name)
                                        preset.description?.let {
                                            Text(
                                                it,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                },
                                enabled = preset.available,
                                onClick = { presetId = preset.id; presetMenu = false },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            PrimaryPillButton(
                onClick = {
                    if (createWorkspace) {
                        viewModel.createWorkspaceAndSession(
                            requireNotNull(state.workspacePickerRootId),
                            state.workspacePickerPath,
                            presetId,
                        )
                    } else {
                        viewModel.createSession(workspaceId, presetId)
                    }
                    onCreated()
                },
                enabled = !state.busy && if (createWorkspace) {
                    state.workspacePickerRootId != null && state.workspacePickerDirectory != null
                } else workspaceId.isNotBlank(),
                loading = state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(if (createWorkspace) R.string.create_workspace_and_session else R.string.create_session),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun PickerHeading(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 12.dp, bottom = 5.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun timeAwareGreeting(): String {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    return when (hour) {
        in 0..5 -> stringResource(R.string.greeting_night_owl)
        in 6..11 -> stringResource(R.string.greeting_morning)
        in 12..17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
}

private fun extractHostLabel(endpoint: String): String {
    if (endpoint.isBlank()) return "DSH"
    return runCatching {
        val uri = URI(endpoint.trim())
        val host = uri.host ?: return@runCatching endpoint.removeSuffix("/api/v1")
        if (uri.port > 0 && uri.port != 80 && uri.port != 443) "$host:${uri.port}" else host
    }.getOrDefault(endpoint.removeSuffix("/api/v1").take(22))
}

private fun encodeCameraBitmap(bitmap: Bitmap): ByteArray? = runCatching {
    val maxSide = max(bitmap.width, bitmap.height)
    val scaled = if (maxSide > 1920) {
        val ratio = 1920f / maxSide
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    } else bitmap
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 88, out)
    if (scaled !== bitmap) scaled.recycle()
    out.toByteArray()
}.getOrNull()

private fun decodeAndAttachImage(context: Context, uri: Uri): Triple<String, String, ByteArray>? {
    val resolver = context.contentResolver
    val rawBytes = runCatching { resolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return null
    val rawName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    } ?: "image.jpg"
    val mime = resolver.getType(uri)?.lowercase().orEmpty()
    val supported = setOf("image/png", "image/jpeg", "image/webp", "image/gif")
    if (mime in supported && rawBytes.size <= 3 * 1024 * 1024) {
        return Triple(rawName, mime, rawBytes)
    }
    val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return null
    val maxSide = max(decoded.width, decoded.height)
    val scaled = if (maxSide > 1920) {
        val ratio = 1920f / maxSide
        Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * ratio).toInt().coerceAtLeast(1),
            (decoded.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    } else decoded
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 86, out)
    if (scaled !== decoded) scaled.recycle()
    decoded.recycle()
    return Triple(rawName.substringBeforeLast('.') + ".jpg", "image/jpeg", out.toByteArray())
}
