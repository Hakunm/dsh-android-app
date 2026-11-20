package io.github.hakunm.deepseekharness.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.material.icons.outlined.ModelTraining
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Queue
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.Unarchive
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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.RoundedRectangle
import com.mikepenz.markdown.m3.Markdown
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
import io.github.hakunm.deepseekharness.data.ChatSession
import io.github.hakunm.deepseekharness.data.CommandDescriptor
import io.github.hakunm.deepseekharness.data.ModelSelection
import io.github.hakunm.deepseekharness.data.ModelView
import io.github.hakunm.deepseekharness.data.PendingApproval
import io.github.hakunm.deepseekharness.data.PendingQuestion
import io.github.hakunm.deepseekharness.data.PermissionSelect
import io.github.hakunm.deepseekharness.data.QuestionAnswerItem
import io.github.hakunm.deepseekharness.data.SessionQueuedItem
import io.github.hakunm.deepseekharness.data.TodoItem
import io.github.hakunm.deepseekharness.data.displayItems
import io.github.hakunm.deepseekharness.data.permissionSelect
import io.github.hakunm.deepseekharness.data.todoItems
import io.github.hakunm.deepseekharness.ui.glass.CompactFilterTabs
import io.github.hakunm.deepseekharness.ui.glass.CompactSearchPill
import io.github.hakunm.deepseekharness.ui.glass.CompactSegmentedControl
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassButton
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassFloatingBar
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassIconButton
import io.github.hakunm.deepseekharness.ui.glass.SquircleIconBadge
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import java.io.ByteArrayOutputStream
import kotlin.math.max

@Composable
fun ChatScreen(
    state: HarnessState,
    viewModel: HarnessViewModel,
    wide: Boolean,
    bottomDockInset: Dp = 0.dp,
    onCreateSession: () -> Unit = {},
) {
    val selected = state.selectedSessionId
    if (wide) {
        Row(
            Modifier.fillMaxSize().padding(end = 10.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SessionPane(
                state = state,
                viewModel = viewModel,
                bottomDockInset = bottomDockInset,
                modifier = Modifier.width(340.dp).fillMaxHeight(),
            )
            ChatDetail(
                state = state,
                viewModel = viewModel,
                onBack = null,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    } else if (selected == null) {
        SessionPane(
            state = state,
            viewModel = viewModel,
            bottomDockInset = bottomDockInset,
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
        )
    } else {
        ChatDetail(
            state = state,
            viewModel = viewModel,
            onBack = { viewModel.selectSession(null) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SessionPane(
    state: HarnessState,
    viewModel: HarnessViewModel,
    bottomDockInset: Dp,
    modifier: Modifier,
) {
    val palette = LocalGlassPalette.current
    val canWrite = "chat.write" in state.device?.scopes.orEmpty()
    var filterTab by rememberSaveable { mutableIntStateOf(0) } // 0=All, 1=Pinned, 2=Running, 3=Archived
    var selectedWorkspaceId by rememberSaveable { mutableStateOf<String?>(null) }

    val searchResults = state.sessionSearchResults
    val searchActive = searchResults != null
    val rawSessions: List<Pair<ChatSession, String?>> = if (searchResults != null) {
        searchResults.items.map { it.session to it.snippet }
    } else {
        state.sessions.map { it to null }
    }
    val displayedSessions = rawSessions.filter { (session, _) ->
        val matchesTab = when (filterTab) {
            1 -> session.pinned && !session.archived
            2 -> (session.running || session.pendingInteraction != null) && !session.archived
            3 -> session.archived
            else -> !session.archived || state.showArchived
        }
        val matchesWorkspace = selectedWorkspaceId == null || session.workspaceId == selectedWorkspaceId
        matchesTab && matchesWorkspace
    }

    Column(
        modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        // Ultra-Compact Single-Row Top Search Bar (modeled after reference UI!)
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrandMark(size = 32.dp)
            CompactSearchPill(
                query = state.sessionSearchQuery,
                onQueryChange = viewModel::setSessionSearchQuery,
                onSearch = { viewModel.searchSessions() },
                onClear = viewModel::clearSessionSearch,
                placeholder = stringResource(R.string.search_sessions_hint),
                searching = state.searchingSessions,
                clearContentDescription = stringResource(R.string.clear_search),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = viewModel::refreshDashboard),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Refresh, stringResource(R.string.refresh), Modifier.size(19.dp))
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 3.dp)
                        .size(7.dp)
                        .background(
                            if (state.eventsConnected) palette.successText else MaterialTheme.colorScheme.outline,
                            CircleShape,
                        ),
                )
            }
        }

        // Sleek Lightweight Horizontal Text Tabs (全部 | 置顶 | 运行中 | 归档)
        CompactFilterTabs(
            items = listOf(
                stringResource(R.string.tab_all),
                stringResource(R.string.tab_pinned),
                stringResource(R.string.tab_running),
                stringResource(R.string.tab_archived),
            ),
            selectedIndex = filterTab,
            onSelect = { idx ->
                filterTab = idx
                val wantsArchived = idx == 3
                if (wantsArchived && !state.showArchived) viewModel.toggleShowArchived()
                else if (!wantsArchived && state.showArchived) viewModel.toggleShowArchived()
            },
            modifier = Modifier.padding(horizontal = 2.dp),
            trailing = {
                Text(
                    stringResource(R.string.session_count, displayedSessions.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            },
        )

        // Compact Workspace Quick-Filter Chips (matches reference UI horizontal pill row!)
        if (state.chatWorkspaces.isNotEmpty() && !searchActive) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WorkspaceFilterChip(
                    label = stringResource(R.string.all_workspaces),
                    selected = selectedWorkspaceId == null,
                    dotColor = palette.accentBlue,
                    onClick = { selectedWorkspaceId = null },
                )
                state.chatWorkspaces.forEachIndexed { idx, ws ->
                    val dot = when (idx % 4) {
                        0 -> palette.accentCyan
                        1 -> palette.accentEmerald
                        2 -> palette.accentIndigo
                        else -> palette.accentAmber
                    }
                    WorkspaceFilterChip(
                        label = ws.title,
                        selected = selectedWorkspaceId == ws.id,
                        dotColor = dot,
                        onClick = { selectedWorkspaceId = if (selectedWorkspaceId == ws.id) null else ws.id },
                    )
                }
            }
        }

        if (searchActive) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.search_results_count, displayedSessions.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextButton(onClick = viewModel::clearSessionSearch, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text(stringResource(R.string.clear_search), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (displayedSessions.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    Icons.Outlined.ChatBubbleOutline,
                    stringResource(if (searchActive) R.string.no_search_results else R.string.no_sessions),
                )
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(top = 2.dp, bottom = bottomDockInset + 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(displayedSessions, key = { (session, _) -> session.id }) { (session, snippet) ->
                    SessionCompactCard(
                        session = session,
                        snippet = snippet,
                        selected = state.selectedSessionId == session.id,
                        enabled = !state.busy && canWrite,
                        onClick = { viewModel.selectSession(session.id) },
                        onTogglePin = { viewModel.togglePinSession(session) },
                        onRename = { viewModel.renameSession(session.id, it) },
                        onFork = { viewModel.forkSession(session.id) },
                        onArchive = { viewModel.archiveSession(session.id) },
                        onUnarchive = { viewModel.unarchiveSession(session.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceFilterChip(
    label: String,
    selected: Boolean,
    dotColor: Color,
    onClick: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedRectangle(9.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else palette.chipBackground,
                RoundedRectangle(9.dp),
            )
            .border(
                0.6.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else palette.cardBorder,
                RoundedRectangle(9.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).background(dotColor, CircleShape))
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun SessionCompactCard(
    session: ChatSession,
    snippet: String?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: (String) -> Unit,
    onFork: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val displayTitle = session.title ?: session.workspaceTitle ?: session.cwd.ifBlank { session.id.take(12) }
    var menuExpanded by remember { mutableStateOf(false) }
    var renameOpen by remember { mutableStateOf(false) }
    var archiveOpen by remember { mutableStateOf(false) }
    var titleDraft by remember(session.id, displayTitle) { mutableStateOf(displayTitle) }

    val (badgeIcon, c1, c2) = when {
        session.pendingInteraction == "approval" -> Triple(Icons.Outlined.WarningAmber, palette.accentAmber, palette.accentCoral)
        session.pendingInteraction == "question" -> Triple(Icons.AutoMirrored.Outlined.HelpOutline, palette.accentCyan, palette.accentBlue)
        session.running -> Triple(Icons.Outlined.ChatBubbleOutline, palette.accentEmerald, palette.accentCyan)
        session.pinned -> Triple(Icons.Outlined.PushPin, palette.accentBlue, palette.accentIndigo)
        else -> Triple(Icons.Outlined.ChatBubbleOutline, palette.accentBlue, palette.accentCyan)
    }

    CrispCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 15.dp,
        borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else palette.cardBorder,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SquircleIconBadge(
                icon = badgeIcon,
                startColor = c1,
                endColor = c2,
                size = 36.dp,
                iconSize = 18.dp,
                cornerRadius = 11.dp,
            )
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        displayTitle,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (session.pinned) {
                        Icon(Icons.Outlined.PushPin, stringResource(R.string.pinned), Modifier.size(12.dp), tint = palette.accentBlue)
                    }
                    if (session.archived) {
                        SmallTagPill(stringResource(R.string.archived))
                    }
                }
                Row(
                    Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (session.pendingInteraction) {
                        "approval" -> SmallTagPill(stringResource(R.string.awaiting_approval), palette.warningTint, palette.warningText)
                        "question" -> SmallTagPill(
                            stringResource(R.string.awaiting_question),
                            palette.accentCyan.copy(alpha = 0.14f),
                            palette.accentBlue,
                        )
                        else -> StatusPill(
                            label = stringResource(if (session.running) R.string.session_running else R.string.session_idle),
                            active = session.running,
                        )
                    }
                    session.workspaceTitle?.takeIf { it != displayTitle }?.let { ws ->
                        Text(
                            ws,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                if (!snippet.isNullOrBlank()) {
                    Text(
                        snippet,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }, enabled = enabled, modifier = Modifier.size(30.dp)) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        stringResource(R.string.session_actions),
                        Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(if (session.pinned) R.string.unpin_session else R.string.pin_session)) },
                        leadingIcon = { Icon(Icons.Outlined.PushPin, null) },
                        onClick = { menuExpanded = false; onTogglePin() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename_session)) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                        onClick = { titleDraft = displayTitle; renameOpen = true; menuExpanded = false },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.fork_session)) },
                        leadingIcon = { Icon(Icons.Outlined.SubdirectoryArrowRight, null) },
                        onClick = { menuExpanded = false; onFork() },
                    )
                    if (session.archived) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.unarchive_session)) },
                            leadingIcon = { Icon(Icons.Outlined.Unarchive, null) },
                            onClick = { menuExpanded = false; onUnarchive() },
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.archive_session)) },
                            leadingIcon = { Icon(Icons.Outlined.Archive, null) },
                            onClick = { archiveOpen = true; menuExpanded = false },
                        )
                    }
                }
            }
        }
    }

    if (renameOpen) RenameDialog(
        title = stringResource(R.string.rename_session),
        value = titleDraft,
        onValueChange = { titleDraft = it },
        onConfirm = { onRename(titleDraft); renameOpen = false },
        onDismiss = { renameOpen = false },
    )
    if (archiveOpen) AlertDialog(
        onDismissRequest = { archiveOpen = false },
        title = { Text(stringResource(R.string.archive_session_title)) },
        text = { Text(stringResource(R.string.archive_session_warning)) },
        confirmButton = {
            Button(onClick = { onArchive(); archiveOpen = false }, shape = CircleShape) {
                Text(stringResource(R.string.archive_session))
            }
        },
        dismissButton = { TextButton(onClick = { archiveOpen = false }) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
private fun ChatDetail(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onBack: (() -> Unit)?,
    modifier: Modifier,
) {
    val selected = state.sessions.find { it.id == state.selectedSessionId }
    if (selected == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            EmptyState(Icons.Outlined.ChatBubbleOutline, stringResource(R.string.select_session))
        }
        return
    }
    val context = LocalContext.current
    val palette = LocalGlassPalette.current
    var message by remember(selected.id) { mutableStateOf("") }
    var modelSheet by remember(selected.id) { mutableStateOf(false) }
    var tasksExpanded by rememberSaveable(selected.id) { mutableStateOf(false) }
    var editingQueueItem by remember { mutableStateOf<SessionQueuedItem?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { picked ->
            decodeAndAttachImage(context, picked)?.let { (name, mime, bytes) ->
                viewModel.addPendingImage(name, mime, bytes)
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

    // Dedicated LayerBackdrop capturing the crisp message stream so floating header & composer refract vividly!
    val chatFeedBackdrop = rememberLayerBackdrop()

    Box(modifier.imePadding()) {
        // Layer 1: Clean Message Feed captured into chatFeedBackdrop
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.canvasBackground)
                .layerBackdrop(chatFeedBackdrop),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = if (todos.isNotEmpty()) 136.dp else 94.dp,
                    bottom = 148.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.history?.hasMore == true) {
                    item("load-older") {
                        Box(Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                            TextButton(onClick = viewModel::loadOlderHistory) {
                                Text(stringResource(R.string.older_messages), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                if (state.history != null && displayItems.isEmpty()) {
                    item("empty-chat") {
                        Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            EmptyState(Icons.Outlined.ChatBubbleOutline, stringResource(R.string.no_readable_messages))
                        }
                    }
                }
                items(displayItems, key = { it.id }) { item -> ChatItem(item) }
            }
        }

        // Layer 2: Compact Floating Crystal Liquid Glass Header + Collapsible Todo Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FloatingConversationHeader(
                session = selected,
                state = state,
                viewModel = viewModel,
                onBack = onBack,
                backdrop = chatFeedBackdrop,
            )
            if (todos.isNotEmpty()) {
                TodoGlassPanel(
                    todos = todos,
                    expanded = tasksExpanded,
                    onToggle = { tasksExpanded = !tasksExpanded },
                    backdrop = chatFeedBackdrop,
                )
            }
        }

        // Layer 3: Compact Floating Crystal Liquid Glass Composer / Approval / Question Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (queuedItems.isNotEmpty()) {
                QueuedTurnsGlassStrip(
                    items = queuedItems,
                    canWrite = "chat.write" in state.device?.scopes.orEmpty() && !state.busy,
                    backdrop = chatFeedBackdrop,
                    onEdit = { editingQueueItem = it },
                    onSteer = { viewModel.steerQueueItem(it.id) },
                    onRemove = { viewModel.removeQueueItem(it.id) },
                )
            }

            val approvals = state.approvalState
            val questions = state.questionState
            when {
                approvals is ApprovalUiState.Pending && approvals.items.isNotEmpty() -> {
                    ApprovalGlassPanel(
                        approval = approvals.items.first(),
                        count = approvals.items.size,
                        deciding = false,
                        canWrite = "chat.write" in state.device?.scopes.orEmpty(),
                        backdrop = chatFeedBackdrop,
                        onReject = { viewModel.decideApproval(approvals.items.first(), false) },
                        onAllowOnce = { viewModel.decideApproval(approvals.items.first(), true) },
                    )
                }
                approvals is ApprovalUiState.Deciding -> {
                    ApprovalGlassPanel(
                        approval = approvals.item,
                        count = approvals.items.size,
                        deciding = true,
                        canWrite = "chat.write" in state.device?.scopes.orEmpty(),
                        backdrop = chatFeedBackdrop,
                        onReject = {},
                        onAllowOnce = {},
                    )
                }
                questions is QuestionUiState.Pending && questions.items.isNotEmpty() -> {
                    QuestionGlassPanel(
                        pending = questions.items.first(),
                        count = questions.items.size,
                        submitting = false,
                        canWrite = "chat.write" in state.device?.scopes.orEmpty(),
                        backdrop = chatFeedBackdrop,
                        onSubmit = { answers -> viewModel.answerQuestion(questions.items.first(), answers) },
                    )
                }
                questions is QuestionUiState.Submitting -> {
                    QuestionGlassPanel(
                        pending = questions.item,
                        count = questions.items.size,
                        submitting = true,
                        canWrite = "chat.write" in state.device?.scopes.orEmpty(),
                        backdrop = chatFeedBackdrop,
                        onSubmit = {},
                    )
                }
                approvals is ApprovalUiState.Loading -> {
                    ApprovalLoadingGlass(chatFeedBackdrop)
                }
                else -> {
                    FloatingGlassComposer(
                        value = message,
                        onValueChange = { message = it },
                        state = state,
                        backdrop = chatFeedBackdrop,
                        onAttachImage = { imagePicker.launch("image/*") },
                        onRemoveImage = viewModel::removePendingImage,
                        onModelClick = { modelSheet = true },
                        onPermissionSelect = viewModel::selectPermissionPreset,
                        onSteer = { viewModel.sendMessage(message, true); message = "" },
                        onSend = { viewModel.sendMessage(message, false); message = "" },
                    )
                }
            }
        }
    }

    if (modelSheet) ModelSheet(state, viewModel) { modelSheet = false }
    editingQueueItem?.let { item ->
        var draft by remember(item.id) { mutableStateOf(item.displayText()) }
        AlertDialog(
            onDismissRequest = { editingQueueItem = null },
            title = { Text(stringResource(R.string.queue_edit_title)) },
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

@Composable
private fun FloatingConversationHeader(
    session: ChatSession,
    state: HarnessState,
    viewModel: HarnessViewModel,
    onBack: (() -> Unit)?,
    backdrop: Backdrop,
) {
    val palette = LocalGlassPalette.current
    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth(),
        capsule = true,
        backdrop = backdrop,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (onBack != null) {
                LiquidGlassIconButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    backdrop = backdrop,
                    size = 32.dp,
                    iconSize = 17.dp,
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 2.dp)) {
                Text(
                    session.title ?: session.workspaceTitle ?: session.cwd.ifBlank { session.id },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        Modifier.size(6.dp).background(
                            if (state.eventsConnected) palette.successText else MaterialTheme.colorScheme.outline,
                            CircleShape,
                        ),
                    )
                    Text(
                        stringResource(if (state.eventsConnected) R.string.online else R.string.offline),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LiquidGlassIconButton(
                onClick = viewModel::refreshHistory,
                icon = Icons.Outlined.Refresh,
                contentDescription = stringResource(R.string.refresh),
                enabled = !state.busy,
                backdrop = backdrop,
                size = 32.dp,
                iconSize = 17.dp,
            )
            AnimatedVisibility(session.running, enter = fadeIn(), exit = fadeOut()) {
                LiquidGlassIconButton(
                    onClick = viewModel::cancelRun,
                    icon = Icons.Outlined.Cancel,
                    contentDescription = stringResource(R.string.cancel_run),
                    enabled = "chat.write" in state.device?.scopes.orEmpty(),
                    backdrop = backdrop,
                    tint = MaterialTheme.colorScheme.error,
                    size = 32.dp,
                    iconSize = 17.dp,
                )
            }
        }
    }
}

@Composable
private fun QueuedTurnsGlassStrip(
    items: List<SessionQueuedItem>,
    canWrite: Boolean,
    backdrop: Backdrop,
    onEdit: (SessionQueuedItem) -> Unit,
    onSteer: (SessionQueuedItem) -> Unit,
    onRemove: (SessionQueuedItem) -> Unit,
) {
    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        backdrop = backdrop,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.Queue, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.queued_messages, items.size), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            items.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.displayText().ifBlank { item.id }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { onSteer(item) }, enabled = canWrite, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.SubdirectoryArrowRight, stringResource(R.string.queue_steer), Modifier.size(15.dp))
                    }
                    IconButton(onClick = { onEdit(item) }, enabled = canWrite, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.queue_edit), Modifier.size(15.dp))
                    }
                    IconButton(onClick = { onRemove(item) }, enabled = canWrite, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.queue_remove), Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovalLoadingGlass(backdrop: Backdrop) {
    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth(),
        capsule = true,
        backdrop = backdrop,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.approval_loading), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ApprovalGlassPanel(
    approval: PendingApproval,
    count: Int,
    deciding: Boolean,
    canWrite: Boolean,
    backdrop: Backdrop,
    onReject: () -> Unit,
    onAllowOnce: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val fullAccess = approval.risk == "full-access"
    var showRiskDialog by rememberSaveable(approval.id) { mutableStateOf(false) }
    var acknowledged by rememberSaveable(approval.id) { mutableStateOf(false) }

    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        backdrop = backdrop,
        surfaceTint = if (fullAccess) palette.warningTint.copy(alpha = 0.78f) else palette.glassCrystalSurface,
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (fullAccess) Icons.Outlined.WarningAmber else Icons.Outlined.Build,
                    null,
                    Modifier.size(18.dp),
                    tint = if (fullAccess) palette.warningText else MaterialTheme.colorScheme.primary,
                )
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.approval_required), style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (count > 1) stringResource(R.string.approval_count, approval.toolName, count) else approval.toolName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (deciding) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            }
            approval.reason?.takeIf(String::isNotBlank)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            approval.detail?.takeIf(String::isNotBlank)?.let { detail ->
                SelectionContainer {
                    Text(
                        detail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 110.dp)
                            .verticalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedRectangle(10.dp))
                            .padding(8.dp),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                LiquidGlassButton(onClick = onReject, enabled = canWrite && !deciding, backdrop = backdrop, height = 36.dp) {
                    Text(stringResource(R.string.reject), style = MaterialTheme.typography.labelMedium)
                }
                LiquidGlassButton(
                    onClick = {
                        if (fullAccess) { acknowledged = false; showRiskDialog = true } else onAllowOnce()
                    },
                    enabled = canWrite && !deciding,
                    backdrop = backdrop,
                    tint = MaterialTheme.colorScheme.primary,
                    height = 36.dp,
                ) { Text(stringResource(R.string.allow_once), style = MaterialTheme.typography.labelMedium) }
            }
        }
    }

    if (showRiskDialog) {
        AlertDialog(
            onDismissRequest = { showRiskDialog = false; acknowledged = false },
            icon = { Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.confirm_full_access)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.full_access_warning), style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth().clickable { acknowledged = !acknowledged }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
                        Text(stringResource(R.string.full_access_ack), Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            dismissButton = { TextButton(onClick = { showRiskDialog = false; acknowledged = false }) { Text(stringResource(R.string.close)) } },
            confirmButton = {
                Button(onClick = { showRiskDialog = false; onAllowOnce() }, enabled = acknowledged, shape = CircleShape) {
                    Text(stringResource(R.string.confirm_allow_once))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionGlassPanel(
    pending: PendingQuestion,
    count: Int,
    submitting: Boolean,
    canWrite: Boolean,
    backdrop: Backdrop,
    onSubmit: (List<QuestionAnswerItem>) -> Unit,
) {
    val selectedById = remember(pending.id) { mutableStateMapOf<String, List<String>>() }
    val customById = remember(pending.id) { mutableStateMapOf<String, String>() }
    val canSubmit = pending.questions.all { q ->
        selectedById[q.id].orEmpty().isNotEmpty() || customById[q.id].orEmpty().trim().isNotEmpty()
    }

    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        backdrop = backdrop,
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.question_required), style = MaterialTheme.typography.titleSmall)
                    if (count > 1) Text(stringResource(R.string.question_count, count), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (submitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            }

            pending.questions.forEach { item ->
                val chosen = selectedById[item.id].orEmpty()
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    border = if (active) null else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    Text(opt.label, Modifier.padding(horizontal = 11.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    GlassOutlinedField(customById[item.id].orEmpty(), { customById[item.id] = it }, stringResource(R.string.question_custom_answer))
                }
            }

            LiquidGlassButton(
                onClick = {
                    val answers = pending.questions.map { q ->
                        QuestionAnswerItem(q.id, selectedById[q.id].orEmpty(), customById[q.id]?.trim()?.takeIf(String::isNotEmpty))
                    }
                    onSubmit(answers)
                },
                enabled = canWrite && !submitting && canSubmit,
                backdrop = backdrop,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                height = 40.dp,
            ) { Text(stringResource(R.string.submit_answer), style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingGlassComposer(
    value: String,
    onValueChange: (String) -> Unit,
    state: HarnessState,
    backdrop: Backdrop,
    onAttachImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onModelClick: () -> Unit,
    onPermissionSelect: (String) -> Unit,
    onSteer: () -> Unit,
    onSend: () -> Unit,
) {
    val canWrite = "chat.write" in state.device?.scopes.orEmpty()
    val hasPayload = value.isNotBlank() || state.pendingImages.isNotEmpty()
    val current = state.sessionModels?.current
    val session = state.sessions.find { it.id == state.selectedSessionId }
    val agentName = state.agentPresets.find { it.id == session?.agentPreset }?.name ?: session?.agentPreset

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CommandSuggestionsGlass(value, state.commands, backdrop, onValueChange)
        LiquidGlassFloatingBar(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            backdrop = backdrop,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state.pendingImages.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.pendingImages.forEach { image ->
                            PendingImageChip(image = image, onRemove = { onRemoveImage(image.id) })
                        }
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 32.dp, max = 96.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            stringResource(R.string.message_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    IconButton(
                        onClick = onAttachImage,
                        enabled = canWrite && !state.busy && state.pendingImages.size < 4,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Outlined.Image, stringResource(R.string.attach_image), Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(
                        onClick = onModelClick,
                        enabled = state.sessionModels != null && state.sessionModels.routable,
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Icon(Icons.Outlined.ModelTraining, null, Modifier.size(15.dp))
                        Text(
                            (current?.model ?: agentName).orEmpty().ifBlank { stringResource(R.string.model) },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(14.dp))
                    }
                    PermissionControl(
                        value = state.history?.permissionSelect(state.permissionCatalog),
                        enabled = canWrite && !state.busy,
                        onSelect = onPermissionSelect,
                    )
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.steer)) } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(
                            onClick = onSteer,
                            enabled = hasPayload && !state.busy && canWrite,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(Icons.Outlined.SubdirectoryArrowRight, stringResource(R.string.steer), Modifier.size(17.dp))
                        }
                    }
                    LiquidGlassIconButton(
                        onClick = onSend,
                        icon = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = stringResource(R.string.send),
                        enabled = hasPayload && !state.busy && canWrite,
                        backdrop = backdrop,
                        tint = if (hasPayload && canWrite) MaterialTheme.colorScheme.primary else Color.Unspecified,
                        size = 34.dp,
                        iconSize = 17.dp,
                    )
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
    Box(Modifier.size(48.dp)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = image.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedRectangle(11.dp)),
            )
        } else {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant, RoundedRectangle(11.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Image, null, Modifier.size(18.dp)) }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(17.dp)
                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Close, stringResource(R.string.remove_image), Modifier.size(11.dp), tint = Color.White)
        }
    }
}

@Composable
private fun TodoGlassPanel(
    todos: List<TodoItem>,
    expanded: Boolean,
    onToggle: () -> Unit,
    backdrop: Backdrop,
) {
    val palette = LocalGlassPalette.current
    val completeCount = todos.count { it.status == "completed" }
    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        backdrop = backdrop,
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(Modifier.animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(Icons.Outlined.CheckCircle, null, Modifier.size(15.dp), tint = palette.successText)
                Text(stringResource(R.string.current_tasks), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(R.string.task_progress, completeCount, todos.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Icon(
                    if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.ExpandMore,
                    null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                    HorizontalDivider(color = palette.divider)
                    todos.forEach { todo ->
                        val (icon, tint, statusLabel) = when (todo.status) {
                            "completed" -> Triple(Icons.Outlined.CheckCircle, palette.successText, stringResource(R.string.task_completed))
                            "in_progress" -> Triple(Icons.Outlined.Edit, MaterialTheme.colorScheme.primary, stringResource(R.string.task_in_progress))
                            else -> Triple(Icons.Outlined.RadioButtonUnchecked, MaterialTheme.colorScheme.onSurfaceVariant, stringResource(R.string.task_pending))
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Icon(icon, statusLabel, Modifier.size(14.dp).padding(top = 2.dp), tint = tint)
                            Text(
                                todo.content,
                                modifier = Modifier.weight(1f),
                                color = if (todo.status == "completed") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
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
private fun CommandSuggestionsGlass(
    value: String,
    commands: List<CommandDescriptor>,
    backdrop: Backdrop,
    onSelect: (String) -> Unit,
) {
    val query = value.removePrefix("/")
    val suggestions = if (value.startsWith("/") && query.none { it.isWhitespace() }) {
        commands.filter { it.name.startsWith(query, ignoreCase = true) }.take(5)
    } else emptyList()

    AnimatedVisibility(visible = suggestions.isNotEmpty()) {
        LiquidGlassFloatingBar(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            backdrop = backdrop,
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            Column {
                Text(
                    stringResource(R.string.available_commands),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                suggestions.forEach { command ->
                    val supportingText = localizedCommandDescription(command) ?: command.description.ifBlank {
                        command.input?.hint?.let { stringResource(R.string.command_input_hint, it) }.orEmpty()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect("/${command.name} ") }.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.Code, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text("/${command.name}", style = MaterialTheme.typography.labelMedium)
                            if (supportingText.isNotEmpty()) {
                                Text(supportingText, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
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
private fun PermissionControl(
    value: PermissionSelect?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    if (value == null || value.options.isEmpty()) return
    var menuOpen by remember { mutableStateOf(false) }
    var confirmFullAccess by remember { mutableStateOf(false) }
    var acknowledged by remember { mutableStateOf(false) }
    val current = value.options.find { it.value == value.currentValue }
    val currentLabel = permissionLabel(value.currentValue, current?.name)

    Box {
        TextButton(
            onClick = { menuOpen = true },
            enabled = enabled,
            modifier = Modifier.widthIn(max = 98.dp).height(32.dp),
            contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
        ) {
            Icon(permissionIcon(value.currentValue), null, Modifier.size(14.dp))
            Text(
                currentLabel,
                modifier = Modifier.padding(start = 3.dp).weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            Text(
                stringResource(R.string.access_mode),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
            value.options.filterNot { it.value == "custom" }.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(permissionLabel(option.value, option.name))
                            option.description?.takeIf(String::isNotBlank)?.let {
                                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    onClick = {
                        menuOpen = false
                        if (option.value == "danger-full-access") { acknowledged = false; confirmFullAccess = true }
                        else onSelect(option.value)
                    },
                    leadingIcon = { Icon(permissionIcon(option.value), null) },
                    trailingIcon = { if (option.value == value.currentValue) Icon(Icons.Outlined.Check, null) },
                )
            }
        }
    }

    if (confirmFullAccess) {
        AlertDialog(
            onDismissRequest = { confirmFullAccess = false; acknowledged = false },
            icon = { Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.permission_full_access_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.permission_full_access_warning), style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth().clickable { acknowledged = !acknowledged }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
                        Text(stringResource(R.string.permission_full_access_ack), Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            dismissButton = { TextButton(onClick = { confirmFullAccess = false; acknowledged = false }) { Text(stringResource(R.string.close)) } },
            confirmButton = {
                Button(
                    onClick = { confirmFullAccess = false; acknowledged = false; onSelect("danger-full-access") },
                    enabled = acknowledged,
                    shape = CircleShape,
                ) { Text(stringResource(R.string.enable_full_access)) }
            },
        )
    }
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
private fun ChatItem(item: ChatDisplayItem) {
    val palette = LocalGlassPalette.current
    when (item.kind) {
        ChatItemKind.USER -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .widthIn(max = 286.dp)
                    .clip(RoundedRectangle(16.dp))
                    .background(palette.userBubbleTint, RoundedRectangle(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(item.body, color = palette.userBubbleText, style = MaterialTheme.typography.bodyMedium)
            }
        }

        ChatItemKind.ASSISTANT -> CrispCard(
            modifier = Modifier.fillMaxWidth().widthIn(max = 700.dp),
            cornerRadius = 15.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Markdown(
                content = item.body,
                typography = compactMarkdownTypography(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        else -> ActivityCompactRow(item)
    }
}

@Composable
private fun ActivityCompactRow(item: ChatDisplayItem) {
    val expandable = item.kind != ChatItemKind.TOOL && item.body.isNotBlank()
    var expanded by remember(item.id) { mutableStateOf(false) }
    val label = when (item.kind) {
        ChatItemKind.CONTEXT -> stringResource(R.string.context_injection)
        ChatItemKind.REASONING -> stringResource(R.string.think)
        ChatItemKind.TOOL -> item.title ?: stringResource(R.string.tool_call)
        else -> ""
    }
    val icon = when (item.kind) {
        ChatItemKind.CONTEXT -> Icons.Outlined.Description
        ChatItemKind.REASONING -> Icons.Outlined.Code
        else -> Icons.Outlined.Build
    }
    CrispCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        cornerRadius = 12.dp,
        onClick = if (expandable) ({ expanded = !expanded }) else null,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(label, Modifier.padding(start = 6.dp), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                item.title?.takeIf { item.kind == ChatItemKind.CONTEXT }?.let {
                    Text(" · $it", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                }
                if (!expanded && item.body.isNotBlank()) {
                    Text(
                        " · ${item.body.lineSequence().first()}",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                    )
                } else Spacer(Modifier.weight(1f))
                if (expandable) {
                    Icon(
                        if (expanded) Icons.Outlined.ExpandMore else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AnimatedVisibility(expanded) {
                Markdown(
                    content = item.body,
                    typography = compactMarkdownTypography(),
                    modifier = Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun compactMarkdownTypography() = markdownTypography(
    h1 = MaterialTheme.typography.headlineMedium,
    h2 = MaterialTheme.typography.headlineSmall,
    h3 = MaterialTheme.typography.titleLarge,
    h4 = MaterialTheme.typography.titleMedium,
    h5 = MaterialTheme.typography.titleSmall,
    h6 = MaterialTheme.typography.titleSmall,
    text = MaterialTheme.typography.bodyMedium,
    code = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    inlineCode = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    quote = MaterialTheme.typography.bodyMedium,
    paragraph = MaterialTheme.typography.bodyMedium,
    ordered = MaterialTheme.typography.bodyMedium,
    bullet = MaterialTheme.typography.bodyMedium,
    list = MaterialTheme.typography.bodyMedium,
    table = MaterialTheme.typography.bodySmall,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ModelSheet(state: HarnessState, viewModel: HarnessViewModel, onClose: () -> Unit) {
    val sessionModels = state.sessionModels
    val session = state.sessions.find { it.id == state.selectedSessionId }
    val currentPreset = session?.agentPreset ?: state.agentPresets.firstOrNull { it.isDefault && it.available }?.id
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedRectangle(24.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            Text(stringResource(R.string.session_configuration), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.session_configuration_subtitle),
                modifier = Modifier.padding(top = 3.dp, bottom = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(0.70f)) {
                if (state.agentPresets.isNotEmpty()) {
                    item("agent-heading") {
                        Text(stringResource(R.string.agent_mode), Modifier.padding(top = 4.dp, bottom = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                    items(state.agentPresets, key = { "agent:${it.id}" }) { preset ->
                        AgentPresetRow(
                            preset = preset,
                            selected = preset.id == currentPreset,
                            enabled = session?.blank == true && preset.available && "chat.write" in state.device?.scopes.orEmpty() && !state.busy,
                            onClick = { viewModel.selectAgentPreset(preset.id) },
                        )
                    }
                    item("agent-lock-note") {
                        Text(
                            if (session?.blank == true) stringResource(R.string.agent_change_before_first_message) else stringResource(R.string.agent_locked_after_start),
                            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                if (sessionModels == null) {
                    item("model-loading") {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                } else {
                    sessionModels.groups.forEach { group ->
                        item(group.id) {
                            Text(group.name, Modifier.padding(top = 12.dp, bottom = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedRectangle(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (preset.isDefault) stringResource(R.string.agent_default_format, preset.name) else preset.name,
                color = if (enabled || selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
            )
            preset.description?.let {
                Text(it, Modifier.padding(top = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (selected) Icon(Icons.Outlined.Check, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelRow(providerId: String, model: ModelView, selected: Boolean, current: ModelSelection, viewModel: HarnessViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedRectangle(12.dp))
            .clickable {
                viewModel.selectModel(ModelSelection(providerId, model.id, model.reasoning?.defaultEffort ?: current.reasoningEffort))
            }
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(model.name, style = MaterialTheme.typography.titleSmall)
                model.description?.let {
                    Text(it, Modifier.padding(top = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (selected) Icon(Icons.Outlined.Check, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
        if (selected && !model.reasoning?.efforts.isNullOrEmpty()) {
            Text(stringResource(R.string.reasoning_effort), Modifier.padding(top = 8.dp, bottom = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                model.reasoning.efforts.forEach { effort ->
                    val active = current.reasoningEffort == effort.id
                    Surface(
                        onClick = { viewModel.selectModel(ModelSelection(providerId, model.id, effort.id)) },
                        shape = CircleShape,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                        border = if (active) null else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) { Text(effort.name, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
private fun RenameDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { GlassOutlinedField(value, onValueChange, stringResource(R.string.name)) },
        confirmButton = {
            Button(onClick = onConfirm, enabled = value.isNotBlank(), shape = CircleShape) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
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
        if (createWorkspace && state.workspacePickerDirectory == null) viewModel.prepareWorkspacePicker()
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedRectangle(24.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
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
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedRectangle(12.dp),
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                        Text(
                            state.chatWorkspaces.find { it.id == workspaceId }?.title ?: stringResource(R.string.no_chat_workspaces),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = workspaceMenu, onDismissRequest = { workspaceMenu = false }) {
                        state.chatWorkspaces.forEach { workspace ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(workspace.title)
                                        Text("/${workspace.path}", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
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
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedRectangle(12.dp),
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                        Text(
                            state.roots.find { it.id == state.workspacePickerRootId }?.label ?: stringResource(R.string.no_roots),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = rootMenu, onDismissRequest = { rootMenu = false }) {
                        state.roots.forEach { root ->
                            DropdownMenuItem(text = { Text(root.label) }, onClick = { viewModel.selectWorkspacePickerRoot(root.id); rootMenu = false })
                        }
                    }
                }
                PickerHeading(stringResource(R.string.workspace_directory))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedRectangle(11.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.loadWorkspacePickerDirectory(state.workspacePickerPath.substringBeforeLast('/', "")) },
                            enabled = state.workspacePickerPath.isNotEmpty() && !state.busy,
                            modifier = Modifier.size(36.dp),
                        ) { Icon(Icons.Outlined.ArrowUpward, stringResource(R.string.go_up), Modifier.size(17.dp)) }
                        Text(
                            "/${state.workspacePickerPath}",
                            modifier = Modifier.weight(1f).padding(end = 10.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        )
                    }
                }
                val directories = state.workspacePickerDirectory?.entries.orEmpty().filter { it.kind == "directory" }
                Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    if (directories.isEmpty()) {
                        Text(
                            stringResource(R.string.no_subdirectories),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        directories.take(8).forEach { directory ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedRectangle(10.dp))
                                    .clickable(enabled = !state.busy) { viewModel.loadWorkspacePickerDirectory(directory.path) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.Folder, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(directory.name, Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(18.dp))
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
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedRectangle(12.dp),
                    ) {
                        Icon(Icons.Outlined.Build, null, Modifier.size(17.dp))
                        Text(
                            state.agentPresets.find { it.id == presetId }?.name ?: stringResource(R.string.server_default),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = presetMenu, onDismissRequest = { presetMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.server_default)) }, onClick = { presetId = ""; presetMenu = false })
                        state.agentPresets.forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(preset.name)
                                        preset.description?.let {
                                            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
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
            Button(
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
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = CircleShape,
            ) {
                Text(stringResource(if (createWorkspace) R.string.create_workspace_and_session else R.string.create_session))
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
        Bitmap.createScaledBitmap(decoded, (decoded.width * ratio).toInt().coerceAtLeast(1), (decoded.height * ratio).toInt().coerceAtLeast(1), true)
    } else decoded
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 86, out)
    if (scaled !== decoded) scaled.recycle()
    decoded.recycle()
    return Triple(rawName.substringBeforeLast('.') + ".jpg", "image/jpeg", out.toByteArray())
}
