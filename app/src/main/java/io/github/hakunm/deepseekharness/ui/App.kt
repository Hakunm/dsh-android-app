package io.github.hakunm.deepseekharness.ui

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.data.ChatSession
import io.github.hakunm.deepseekharness.ui.glass.CompactSearchPill
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import io.github.hakunm.deepseekharness.ui.theme.LocalIsDarkGlass
import kotlinx.coroutines.launch

private enum class AppSection {
    CHAT,
    WORKSPACES,
    FILES,
    SETTINGS,
}

@Composable
fun DeepSeekHarnessApp(viewModel: HarnessViewModel) {
    val state by viewModel.state.collectAsState()
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var createSessionSheet by remember { mutableStateOf(false) }
    var createSessionInitialNewWs by remember { mutableStateOf(false) }
    var fileCreateRequest by remember { mutableStateOf<Boolean?>(null) }
    val section = AppSection.entries.getOrElse(sectionIndex) { AppSection.CHAT }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = !drawerState.isOpen && state.connected && sectionIndex != 0 && state.document == null) {
        sectionIndex = 0
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(palette.canvasBackground),
    ) {
        val wide = maxWidth >= 840.dp
        val openDrawer: () -> Unit = {
            if (!wide) scope.launch { drawerState.open() }
        }
        val closeDrawer: () -> Unit = {
            if (drawerState.isOpen) scope.launch { drawerState.close() }
        }

        when {
            state.restoring -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        WhaleMark(size = 42.dp)
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 1.8.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            !state.connected -> {
                ConnectScreen(state, viewModel)
            }

            wide -> {
                Row(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(284.dp)
                            .fillMaxHeight()
                            .background(palette.drawerBackground)
                            .border(width = 0.5.dp, color = palette.divider),
                    ) {
                        MinimalDrawerContent(
                            state = state,
                            viewModel = viewModel,
                            selectedSection = section,
                            onSelectSection = { target ->
                                sectionIndex = target.ordinal
                            },
                            onSelectSession = { sessionId ->
                                viewModel.selectSession(sessionId)
                                sectionIndex = AppSection.CHAT.ordinal
                            },
                            onNewChat = {
                                if (state.chatWorkspaces.isEmpty()) {
                                    createSessionInitialNewWs = true
                                    createSessionSheet = true
                                } else {
                                    viewModel.selectSession(null)
                                    sectionIndex = AppSection.CHAT.ordinal
                                }
                            },
                        )
                    }
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        ActiveSectionContent(
                            section = section,
                            state = state,
                            viewModel = viewModel,
                            wide = true,
                            onOpenDrawer = null,
                            onOpenNewSession = { newWs ->
                                createSessionInitialNewWs = newWs
                                createSessionSheet = true
                            },
                            onSelectSection = { sectionIndex = it.ordinal },
                            externalCreateFileRequest = fileCreateRequest,
                            onConsumeFileCreateRequest = { fileCreateRequest = null },
                        )
                    }
                }
            }

            else -> {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = state.document == null,
                    scrimColor = Color.Black.copy(alpha = if (dark) 0.56f else 0.28f),
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier
                                .fillMaxWidth(0.86f)
                                .fillMaxHeight(),
                            drawerShape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp),
                            drawerContainerColor = palette.drawerBackground,
                            drawerTonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0),
                        ) {
                            MinimalDrawerContent(
                                state = state,
                                viewModel = viewModel,
                                selectedSection = section,
                                onSelectSection = { target ->
                                    sectionIndex = target.ordinal
                                    closeDrawer()
                                },
                                onSelectSession = { sessionId ->
                                    viewModel.selectSession(sessionId)
                                    sectionIndex = AppSection.CHAT.ordinal
                                    closeDrawer()
                                },
                                onNewChat = {
                                    closeDrawer()
                                    if (state.chatWorkspaces.isEmpty()) {
                                        createSessionInitialNewWs = true
                                        createSessionSheet = true
                                    } else {
                                        viewModel.selectSession(null)
                                        sectionIndex = AppSection.CHAT.ordinal
                                    }
                                },
                            )
                        }
                    },
                ) {
                    ActiveSectionContent(
                        section = section,
                        state = state,
                        viewModel = viewModel,
                        wide = false,
                        onOpenDrawer = openDrawer,
                        onOpenNewSession = { newWs ->
                            createSessionInitialNewWs = newWs
                            createSessionSheet = true
                        },
                        onSelectSection = { sectionIndex = it.ordinal },
                        externalCreateFileRequest = fileCreateRequest,
                        onConsumeFileCreateRequest = { fileCreateRequest = null },
                    )
                }
            }
        }

        // Clean top error notification pill
        AnimatedVisibility(
            visible = state.error != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            state.error?.let { rawError ->
                FloatingErrorPill(
                    message = localizeError(rawError),
                    onDismiss = viewModel::clearError,
                )
            }
        }
    }

    if (createSessionSheet) {
        NewSessionSheet(
            state = state,
            viewModel = viewModel,
            initialCreateWorkspace = createSessionInitialNewWs,
            onCreated = {
                createSessionSheet = false
                sectionIndex = AppSection.CHAT.ordinal
            },
            onClose = { createSessionSheet = false },
        )
    }
}

@Composable
private fun ActiveSectionContent(
    section: AppSection,
    state: HarnessState,
    viewModel: HarnessViewModel,
    wide: Boolean,
    onOpenDrawer: (() -> Unit)?,
    onOpenNewSession: (Boolean) -> Unit,
    onSelectSection: (AppSection) -> Unit,
    externalCreateFileRequest: Boolean?,
    onConsumeFileCreateRequest: () -> Unit,
) {
    when (section) {
        AppSection.CHAT -> ChatScreen(
            state = state,
            viewModel = viewModel,
            wide = wide,
            onOpenDrawer = onOpenDrawer,
            onCreateSession = { onOpenNewSession(false) },
            onCreateWorkspace = { onOpenNewSession(true) },
            onJumpToFiles = { onSelectSection(AppSection.FILES) },
            onJumpToSettings = { onSelectSection(AppSection.SETTINGS) },
        )
        AppSection.WORKSPACES -> WorkspacesScreen(
            state = state,
            viewModel = viewModel,
            onOpenDrawer = onOpenDrawer,
            onCreateWorkspace = { onOpenNewSession(true) },
            onStartSessionInWorkspace = { wsId ->
                viewModel.createSession(wsId, null)
                onSelectSection(AppSection.CHAT)
            },
        )
        AppSection.FILES -> FilesScreen(
            state = state,
            viewModel = viewModel,
            wide = wide,
            onOpenDrawer = onOpenDrawer,
            externalCreateDirectory = externalCreateFileRequest,
            onConsumeExternalCreate = onConsumeFileCreateRequest,
        )
        AppSection.SETTINGS -> SettingsScreen(
            state = state,
            viewModel = viewModel,
            onOpenDrawer = onOpenDrawer,
        )
    }
}

/**
 * Approved Prototype Screen 3:
 * Borderless slide-over left navigation drawer with DeepSeek wordmark,
 * 4 clean outline nav rows, session search, single-line recent sessions, and pinned bottom `+ 新建对话` pill CTA.
 */
@Composable
private fun MinimalDrawerContent(
    state: HarnessState,
    viewModel: HarnessViewModel,
    selectedSection: AppSection,
    onSelectSection: (AppSection) -> Unit,
    onSelectSession: (String) -> Unit,
    onNewChat: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val canWriteChat = "chat.write" in state.device?.scopes.orEmpty()
    val searchResults = state.sessionSearchResults
    val displayedSessions = remember(state.sessions, searchResults, state.showArchived) {
        val raw = searchResults?.items?.map { it.session } ?: state.sessions
        val visible = raw.filter { !it.archived || state.showArchived }
        visible.sortedWith(
            compareByDescending<ChatSession> { it.pinned && !it.archived }
                .thenBy { it.archived },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Brand Wordmark Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 18.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            WhaleMark(size = 24.dp)
            Text(
                text = "DeepSeek",
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        if (state.eventsConnected) palette.accentEmerald else MaterialTheme.colorScheme.outline,
                        CircleShape,
                    ),
            )
        }

        // 4 Clean Borderless Primary Navigation Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            DrawerNavItem(
                label = stringResource(R.string.chat),
                selected = selectedSection == AppSection.CHAT && state.selectedSessionId == null,
                onClick = {
                    viewModel.selectSession(null)
                    onSelectSection(AppSection.CHAT)
                },
                icon = {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(18.5.dp))
                },
            )
            DrawerNavItem(
                label = stringResource(R.string.workspaces),
                selected = selectedSection == AppSection.WORKSPACES,
                trailingText = state.chatWorkspaces.size.takeIf { it > 0 }?.toString(),
                onClick = { onSelectSection(AppSection.WORKSPACES) },
                icon = {
                    WorkspaceStackIcon(size = 18.5.dp, tint = MaterialTheme.colorScheme.onBackground)
                },
            )
            DrawerNavItem(
                label = stringResource(R.string.nav_code),
                selected = selectedSection == AppSection.FILES,
                onClick = { onSelectSection(AppSection.FILES) },
                icon = {
                    Icon(Icons.Outlined.Code, null, Modifier.size(18.5.dp))
                },
            )
            DrawerNavItem(
                label = stringResource(R.string.nav_connect_settings),
                selected = selectedSection == AppSection.SETTINGS,
                onClick = { onSelectSection(AppSection.SETTINGS) },
                icon = {
                    Icon(Icons.Outlined.Tune, null, Modifier.size(18.5.dp))
                },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            color = palette.divider,
        )

        // Compact Session Search Pill
        CompactSearchPill(
            query = state.sessionSearchQuery,
            onQueryChange = viewModel::setSessionSearchQuery,
            onSearch = { viewModel.searchSessions() },
            onClear = viewModel::clearSessionSearch,
            placeholder = stringResource(R.string.search_sessions_hint),
            searching = state.searchingSessions,
            clearContentDescription = stringResource(R.string.clear_search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )

        // Recent Sessions Heading + Archived Filter Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (searchResults != null) {
                    stringResource(R.string.search_results_count, displayedSessions.size)
                } else {
                    stringResource(R.string.recent_sessions)
                },
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (state.showArchived) palette.accentSoft else Color.Transparent,
                        CircleShape,
                    )
                    .clickable { viewModel.toggleShowArchived() }
                    .padding(horizontal = 8.dp, vertical = 2.5.dp),
            ) {
                Text(
                    text = stringResource(R.string.all_sessions),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = if (state.showArchived) palette.accentInk else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (state.showArchived) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }

        // Scrollable Single-Line Recent Sessions List
        if (displayedSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (searchResults != null) R.string.no_search_results else R.string.no_sessions,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            ) {
                items(displayedSessions, key = { it.id }) { session ->
                    DrawerSessionRow(
                        session = session,
                        selected = selectedSection == AppSection.CHAT && state.selectedSessionId == session.id,
                        canWrite = canWriteChat && !state.busy,
                        onClick = { onSelectSession(session.id) },
                        onTogglePin = { viewModel.togglePinSession(session) },
                        onRename = { viewModel.renameSession(session.id, it) },
                        onFork = { viewModel.forkSession(session.id) },
                        onArchive = { viewModel.archiveSession(session.id) },
                        onUnarchive = { viewModel.unarchiveSession(session.id) },
                    )
                }
            }
        }

        // Pinned Bottom Footer: Device Initial Avatar + Solid `+ 新建对话` Pill CTA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val deviceName = state.device?.name.orEmpty().ifBlank { "DSH" }
            val firstChar = deviceName.trim().firstOrNull()
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSelectSection(AppSection.SETTINGS) }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.accentSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (firstChar != null && firstChar.isLetter()) {
                        Text(
                            text = firstChar.uppercaseChar().toString(),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentInk,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = palette.accentInk,
                        )
                    }
                }
                Text(
                    text = deviceName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = 96.dp),
                )
            }

            PrimaryPillButton(
                onClick = onNewChat,
                enabled = canWriteChat && !state.busy,
                height = 40.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                Text(
                    stringResource(R.string.new_chat_cta),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailingText: String? = null,
    icon: @Composable () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .background(if (selected) palette.pillBackground else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(
            text = label,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (!trailingText.isNullOrBlank()) {
            Text(
                text = trailingText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DrawerSessionRow(
    session: ChatSession,
    selected: Boolean,
    canWrite: Boolean,
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
    val shape = RoundedCornerShape(11.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(39.dp)
            .clip(shape)
            .background(if (selected) palette.pillBackground else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(start = 11.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            session.pendingInteraction != null -> {
                Box(Modifier.size(7.dp).background(palette.accentAmber, CircleShape))
            }
            session.running -> {
                Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
            session.pinned -> {
                Icon(
                    Icons.Outlined.PushPin,
                    contentDescription = null,
                    modifier = Modifier.size(14.5.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            else -> {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(14.5.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = displayTitle,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (session.archived) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )

        Box {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canWrite) { menuExpanded = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.MoreHoriz,
                    contentDescription = stringResource(R.string.session_actions),
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
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

    if (renameOpen) {
        AlertDialog(
            onDismissRequest = { renameOpen = false },
            title = { Text(stringResource(R.string.rename_session), style = MaterialTheme.typography.titleLarge) },
            text = { GlassOutlinedField(titleDraft, { titleDraft = it }, stringResource(R.string.name)) },
            confirmButton = {
                Button(
                    onClick = { onRename(titleDraft); renameOpen = false },
                    enabled = titleDraft.isNotBlank(),
                    shape = CircleShape,
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameOpen = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }

    if (archiveOpen) {
        AlertDialog(
            onDismissRequest = { archiveOpen = false },
            title = { Text(stringResource(R.string.archive_session_title), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.archive_session_warning), style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                Button(onClick = { onArchive(); archiveOpen = false }, shape = CircleShape) {
                    Text(stringResource(R.string.archive_session))
                }
            },
            dismissButton = {
                TextButton(onClick = { archiveOpen = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

@Composable
private fun FloatingErrorPill(message: String, onDismiss: () -> Unit) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .clip(shape)
            .background(palette.dangerTint, shape)
            .border(1.dp, palette.dangerText.copy(alpha = 0.22f), shape)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Outlined.ErrorOutline, null, Modifier.size(16.dp), tint = palette.dangerText)
        Text(
            message,
            modifier = Modifier.weight(1f),
            color = palette.dangerText,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Close, stringResource(R.string.dismiss), Modifier.size(14.5.dp), tint = palette.dangerText)
        }
    }
}

@Composable
internal fun localizeError(error: String): String = when {
    error.startsWith("PROTECTED_DRIVE_ROOT|") ->
        stringResource(R.string.protected_drive_root_error, error.substringAfter('|'))
    error.startsWith("PROTECTED_DIRECTORY|") ->
        stringResource(R.string.protected_directory_error, error.substringAfter('|'))
    error.startsWith("LINK_TRAVERSAL_FORBIDDEN|") ->
        stringResource(R.string.link_traversal_error, error.substringAfter('|'))
    error.startsWith("DIRECTORY_NOT_FOUND|") ->
        stringResource(R.string.directory_not_found_error, error.substringAfter('|'))
    error.startsWith("PAIRING_INVALID") || error.startsWith("PAIRING_EXPIRED") ->
        stringResource(R.string.pairing_invalid_error)
    error == "FILE_TOO_LARGE" -> stringResource(R.string.file_too_large)
    error == "IMAGE_TOO_LARGE" -> stringResource(R.string.image_too_large)
    error.startsWith("ETAG_MISMATCH") -> stringResource(R.string.etag_conflict)
    error == "BINARY_FILE" -> stringResource(R.string.binary_file)
    error.startsWith("RATE_LIMITED") -> stringResource(R.string.rate_limited)
    error.startsWith("AGENT_PRESET_LOCKED") -> stringResource(R.string.agent_preset_locked)
    else -> error
}

internal fun toggleLanguage() {
    val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val next = if (current.startsWith("en")) "zh-CN" else "en"
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
}
