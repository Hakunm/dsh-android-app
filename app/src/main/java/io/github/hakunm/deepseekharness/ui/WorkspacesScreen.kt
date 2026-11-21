package io.github.hakunm.deepseekharness.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.data.ChatWorkspace
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette

@Composable
fun WorkspacesScreen(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onOpenDrawer: (() -> Unit)? = null,
    onCreateWorkspace: () -> Unit,
    onStartSessionInWorkspace: (String) -> Unit,
) {
    val palette = LocalGlassPalette.current
    val canWrite = "chat.write" in state.device?.scopes.orEmpty()
    var renameTarget by remember { mutableStateOf<ChatWorkspace?>(null) }
    var deleteTarget by remember { mutableStateOf<ChatWorkspace?>(null) }
    var titleDraft by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.settingsCanvas),
    ) {
        MinimalTopBar(
            title = stringResource(R.string.workspaces),
            backgroundColor = palette.settingsCanvas,
            onNavigationClick = onOpenDrawer,
            trailing = {
                if (canWrite) {
                    TopBarIconAction(
                        onClick = onCreateWorkspace,
                        contentDescription = stringResource(R.string.new_workspace),
                        enabled = !state.busy,
                    ) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
                TopBarIconAction(
                    onClick = viewModel::refreshDashboard,
                    contentDescription = stringResource(R.string.refresh),
                ) {
                    Icon(Icons.Outlined.Refresh, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onBackground)
                }
            },
        )

        if (state.chatWorkspaces.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.FolderOpen,
                    title = stringResource(R.string.no_chat_workspaces),
                    subtitle = stringResource(R.string.manage_workspaces_subtitle),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item("workspaces-card") {
                    GroupedCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            state.chatWorkspaces.forEachIndexed { index, workspace ->
                                val sessionCount = state.sessions.count { it.workspaceId == workspace.id }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        WorkspaceStackIcon(
                                            size = 19.dp,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                        )
                                        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                                            Text(
                                                workspace.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                            Text(
                                                "/${workspace.path}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                            )
                                        }
                                        SmallTagPill(stringResource(R.string.session_count, sessionCount))
                                    }
                                    if (canWrite) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(start = 32.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(palette.pillBackground, CircleShape)
                                                    .clickable(enabled = !state.busy) { onStartSessionInWorkspace(workspace.id) }
                                                    .padding(horizontal = 11.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            ) {
                                                Icon(
                                                    Icons.Outlined.ChatBubbleOutline,
                                                    null,
                                                    Modifier.size(13.dp),
                                                    tint = MaterialTheme.colorScheme.onBackground,
                                                )
                                                Text(
                                                    stringResource(R.string.start_chat),
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    style = MaterialTheme.typography.labelMedium,
                                                )
                                            }
                                            Spacer(Modifier.weight(1f))
                                            IconButton(
                                                onClick = { titleDraft = workspace.title; renameTarget = workspace },
                                                enabled = !state.busy,
                                                modifier = Modifier.size(30.dp),
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Edit,
                                                    stringResource(R.string.rename_workspace),
                                                    Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            IconButton(
                                                onClick = { deleteTarget = workspace },
                                                enabled = !state.busy,
                                                modifier = Modifier.size(30.dp),
                                            ) {
                                                Icon(
                                                    Icons.Outlined.DeleteOutline,
                                                    stringResource(R.string.remove_workspace),
                                                    Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                        }
                                    }
                                }
                                if (index != state.chatWorkspaces.lastIndex) {
                                    HorizontalDivider(Modifier.padding(start = 48.dp), color = palette.divider)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    renameTarget?.let { workspace ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.rename_workspace), style = MaterialTheme.typography.titleLarge) },
            text = {
                GlassOutlinedField(titleDraft, { titleDraft = it }, stringResource(R.string.name))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.renameWorkspace(workspace.id, titleDraft); renameTarget = null },
                    enabled = titleDraft.isNotBlank(),
                    shape = CircleShape,
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text(stringResource(R.string.close)) }
            },
        )
    }

    deleteTarget?.let { workspace ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.remove_workspace_title), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.remove_workspace_warning, workspace.title), style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteWorkspace(workspace.id); deleteTarget = null }) {
                    Text(stringResource(R.string.remove_workspace), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}
