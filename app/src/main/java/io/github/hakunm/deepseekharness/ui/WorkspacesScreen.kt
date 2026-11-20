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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.data.ChatWorkspace
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.SquircleIconBadge
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette

@Composable
fun WorkspacesScreen(
    state: HarnessState,
    viewModel: HarnessViewModel,
    bottomDockInset: Dp = 0.dp,
    onCreateWorkspace: () -> Unit,
    onStartSessionInWorkspace: (String) -> Unit,
) {
    val palette = LocalGlassPalette.current
    val canWrite = "chat.write" in state.device?.scopes.orEmpty()
    var renameTarget by remember { mutableStateOf<ChatWorkspace?>(null) }
    var deleteTarget by remember { mutableStateOf<ChatWorkspace?>(null) }
    var titleDraft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Compact Top Bar
        Row(
            Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.workspaces),
                style = MaterialTheme.typography.headlineMedium,
            )
            SmallTagPill(text = "${state.chatWorkspaces.size}")
            Spacer(Modifier.weight(1f))
            if (canWrite) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                        .clickable(enabled = !state.busy, onClick = onCreateWorkspace)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.new_workspace),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            IconButton(
                onClick = viewModel::refreshDashboard,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(Icons.Outlined.Refresh, stringResource(R.string.refresh), Modifier.size(19.dp))
            }
        }

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
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(top = 2.dp, bottom = bottomDockInset + 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(state.chatWorkspaces, key = { _, ws -> ws.id }) { index, workspace ->
                    val sessionCount = state.sessions.count { it.workspaceId == workspace.id }
                    val (c1, c2) = when (index % 4) {
                        0 -> palette.accentBlue to palette.accentCyan
                        1 -> palette.accentIndigo to palette.accentBlue
                        2 -> palette.accentEmerald to palette.accentCyan
                        else -> palette.accentAmber to palette.accentCoral
                    }
                    CrispCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 15.dp,
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                SquircleIconBadge(
                                    icon = Icons.Outlined.Folder,
                                    startColor = c1,
                                    endColor = c2,
                                    size = 36.dp,
                                    iconSize = 19.dp,
                                )
                                Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
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
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.11f), CircleShape)
                                            .clickable(enabled = !state.busy) { onStartSessionInWorkspace(workspace.id) }
                                            .padding(horizontal = 11.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.ChatBubbleOutline,
                                            null,
                                            Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            stringResource(R.string.start_chat),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { titleDraft = workspace.title; renameTarget = workspace },
                                        enabled = !state.busy,
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            stringResource(R.string.rename_workspace),
                                            Modifier.size(17.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(
                                        onClick = { deleteTarget = workspace },
                                        enabled = !state.busy,
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            stringResource(R.string.remove_workspace),
                                            Modifier.size(17.dp),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
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
            title = { Text(stringResource(R.string.rename_workspace)) },
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
            title = { Text(stringResource(R.string.remove_workspace_title)) },
            text = { Text(stringResource(R.string.remove_workspace_warning, workspace.title)) },
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
