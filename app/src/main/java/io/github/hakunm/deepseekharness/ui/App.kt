package io.github.hakunm.deepseekharness.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.LiquidDockTab
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassDock
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassFloatingBar
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassIconButton
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassStage
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette

private enum class AppSection(val labelRes: Int) {
    CHAT(R.string.chat),
    WORKSPACES(R.string.workspaces),
    FILES(R.string.files),
    SETTINGS(R.string.settings),
}

@Composable
fun DeepSeekHarnessApp(viewModel: HarnessViewModel) {
    val state by viewModel.state.collectAsState()
    var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var createSessionSheet by remember { mutableStateOf(false) }
    var createSessionInitialNewWs by remember { mutableStateOf(false) }
    var fileCreateRequest by remember { mutableStateOf<Boolean?>(null) }
    val section = AppSection.entries.getOrElse(sectionIndex) { AppSection.CHAT }
    val canWriteChat = "chat.write" in state.device?.scopes.orEmpty()
    val canWriteFiles = "files.write" in state.device?.scopes.orEmpty()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        val showFloatingBottomBar = !wide && state.connected && when (section) {
            AppSection.CHAT -> state.selectedSessionId == null
            AppSection.WORKSPACES -> true
            AppSection.FILES -> state.document == null
            AppSection.SETTINGS -> true
        }
        val dockTabs = listOf(
            LiquidDockTab(stringResource(R.string.chat), Icons.Outlined.ChatBubbleOutline),
            LiquidDockTab(stringResource(R.string.workspaces), Icons.Outlined.Workspaces),
            LiquidDockTab(stringResource(R.string.files), Icons.Outlined.FolderOpen),
            LiquidDockTab(stringResource(R.string.settings), Icons.Outlined.Settings),
        )

        LiquidGlassStage(
            content = {
                when {
                    state.restoring -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CrispCard(
                                cornerRadius = 22.dp,
                                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 22.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    BrandMark(size = 48.dp)
                                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }

                    !state.connected -> {
                        ConnectScreen(state, viewModel)
                    }

                    wide -> {
                        Row(
                            Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding(),
                        ) {
                            WideLiquidRail(
                                state = state,
                                tabs = dockTabs,
                                selectedIndex = sectionIndex,
                                onSelect = { sectionIndex = it },
                                onRefresh = viewModel::refreshDashboard,
                            )
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                ActiveSectionContent(
                                    section = section,
                                    state = state,
                                    viewModel = viewModel,
                                    wide = true,
                                    bottomDockInset = 12.dp,
                                    onOpenNewSession = { newWs ->
                                        createSessionInitialNewWs = newWs
                                        createSessionSheet = true
                                    },
                                    onOpenChatTab = { sectionIndex = 0 },
                                    externalCreateFileRequest = fileCreateRequest,
                                    onConsumeFileCreateRequest = { fileCreateRequest = null },
                                )
                            }
                        }
                    }

                    else -> {
                        Box(Modifier.fillMaxSize()) {
                            ActiveSectionContent(
                                section = section,
                                state = state,
                                viewModel = viewModel,
                                wide = false,
                                bottomDockInset = if (showFloatingBottomBar) 82.dp else 0.dp,
                                onOpenNewSession = { newWs ->
                                    createSessionInitialNewWs = newWs
                                    createSessionSheet = true
                                },
                                onOpenChatTab = { sectionIndex = 0 },
                                externalCreateFileRequest = fileCreateRequest,
                                onConsumeFileCreateRequest = { fileCreateRequest = null },
                            )
                        }
                    }
                }
            },
            overlay = {
                // Floating Crystal Liquid Glass Error Toast
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    state.error?.let { rawError ->
                        FloatingErrorPill(
                            message = localizeError(rawError),
                            onDismiss = viewModel::clearError,
                        )
                    }
                }

                // Signature 5-slot True Crystal Liquid Glass Bottom Dock
                AnimatedVisibility(
                    visible = showFloatingBottomBar,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    LiquidGlassDock(
                        tabs = dockTabs,
                        selectedIndex = sectionIndex,
                        onSelect = { sectionIndex = it },
                        onCenterAction = {
                            if (section == AppSection.FILES) {
                                fileCreateRequest = false
                            } else {
                                createSessionInitialNewWs = section == AppSection.WORKSPACES
                                createSessionSheet = true
                            }
                        },
                        centerActionDescription = stringResource(
                            if (section == AppSection.FILES) R.string.new_file else R.string.new_session,
                        ),
                        centerActionEnabled = if (section == AppSection.FILES) canWriteFiles && !state.busy
                        else canWriteChat && !state.busy,
                        modifier = Modifier.widthIn(max = 420.dp),
                    )
                }
            },
        )
    }

    if (createSessionSheet) {
        NewSessionSheet(
            state = state,
            viewModel = viewModel,
            initialCreateWorkspace = createSessionInitialNewWs,
            onCreated = {
                createSessionSheet = false
                sectionIndex = 0
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
    bottomDockInset: Dp,
    onOpenNewSession: (Boolean) -> Unit,
    onOpenChatTab: () -> Unit,
    externalCreateFileRequest: Boolean?,
    onConsumeFileCreateRequest: () -> Unit,
) {
    when (section) {
        AppSection.CHAT -> ChatScreen(
            state = state,
            viewModel = viewModel,
            wide = wide,
            bottomDockInset = bottomDockInset,
            onCreateSession = { onOpenNewSession(false) },
        )
        AppSection.WORKSPACES -> WorkspacesScreen(
            state = state,
            viewModel = viewModel,
            bottomDockInset = bottomDockInset,
            onCreateWorkspace = { onOpenNewSession(true) },
            onStartSessionInWorkspace = { wsId ->
                viewModel.createSession(wsId, null)
                onOpenChatTab()
            },
        )
        AppSection.FILES -> FilesScreen(
            state = state,
            viewModel = viewModel,
            wide = wide,
            bottomDockInset = bottomDockInset,
            externalCreateDirectory = externalCreateFileRequest,
            onConsumeExternalCreate = onConsumeFileCreateRequest,
        )
        AppSection.SETTINGS -> SettingsScreen(state, viewModel, bottomDockInset)
    }
}

@Composable
private fun WideLiquidRail(
    state: HarnessState,
    tabs: List<LiquidDockTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = Modifier.width(204.dp).fillMaxHeight().padding(10.dp),
    ) {
        CrispCard(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(12.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    BrandMark(size = 34.dp)
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                tabs.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent,
                                CircleShape,
                            )
                            .clickable { onSelect(index) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(18.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        state.device?.name.orEmpty(),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LiquidGlassIconButton(
                        onClick = onRefresh,
                        icon = Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        size = 32.dp,
                        iconSize = 16.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingErrorPill(message: String, onDismiss: () -> Unit) {
    val palette = LocalGlassPalette.current
    LiquidGlassFloatingBar(
        modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
        cornerRadius = 18.dp,
        surfaceTint = palette.dangerTint.copy(alpha = 0.72f),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.ErrorOutline, null, Modifier.size(18.dp), tint = palette.dangerText)
            Text(
                message,
                modifier = Modifier.weight(1f),
                color = palette.dangerText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Box(Modifier.size(24.dp).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Close, stringResource(R.string.dismiss), Modifier.size(16.dp), tint = palette.dangerText)
            }
        }
    }
}

@Composable
private fun localizeError(error: String): String = when {
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
