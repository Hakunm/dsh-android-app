package io.github.hakunm.deepseekharness.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.OpenDocument
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.data.FileEntry
import io.github.hakunm.deepseekharness.ui.glass.CompactSearchPill
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassFloatingBar
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassIconButton
import io.github.hakunm.deepseekharness.ui.glass.SquircleIconBadge
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import kotlin.math.roundToInt

@Composable
fun FilesScreen(
    state: HarnessState,
    viewModel: HarnessViewModel,
    wide: Boolean,
    bottomDockInset: Dp = 0.dp,
    externalCreateDirectory: Boolean? = null,
    onConsumeExternalCreate: () -> Unit = {},
) {
    val context = LocalContext.current
    var createDirectory by remember { mutableStateOf<Boolean?>(null) }
    var moveDialog by remember { mutableStateOf(false) }
    var trashDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !wide && state.document != null) {
        viewModel.loadDirectory()
    }

    LaunchedEffect(externalCreateDirectory) {
        if (externalCreateDirectory != null) {
            createDirectory = externalCreateDirectory
            onConsumeExternalCreate()
        }
    }

    val uploadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val size = displaySize(context, it)
            viewModel.upload(displayName(context, it), size) {
                requireNotNull(context.contentResolver.openInputStream(it))
            }
        }
    }
    val replaceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val size = displaySize(context, it)
            viewModel.replaceSelected(size) { requireNotNull(context.contentResolver.openInputStream(it)) }
        }
    }
    val downloadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let { viewModel.downloadSelected { requireNotNull(context.contentResolver.openOutputStream(it)) } }
    }
    val download: () -> Unit = { state.document?.entry?.name?.let(downloadLauncher::launch) }

    if (state.roots.isEmpty()) {
        Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
            EmptyState(Icons.Outlined.FolderOpen, stringResource(R.string.no_roots))
        }
        return
    }
    if (wide) {
        Row(
            Modifier.fillMaxSize().padding(end = 8.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FileBrowser(
                state = state,
                viewModel = viewModel,
                onNewFile = { createDirectory = false },
                onNewFolder = { createDirectory = true },
                onUpload = { uploadLauncher.launch(arrayOf("*/*")) },
                onTrash = { viewModel.loadTrash(); trashDialog = true },
                bottomDockInset = bottomDockInset,
                modifier = Modifier.width(310.dp).fillMaxHeight(),
            )
            EditorPane(
                state = state,
                viewModel = viewModel,
                onBack = null,
                onDownload = download,
                onReplace = { replaceLauncher.launch(arrayOf("*/*")) },
                onMove = { moveDialog = true },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    } else if (state.document == null) {
        FileBrowser(
            state = state,
            viewModel = viewModel,
            onNewFile = { createDirectory = false },
            onNewFolder = { createDirectory = true },
            onUpload = { uploadLauncher.launch(arrayOf("*/*")) },
            onTrash = { viewModel.loadTrash(); trashDialog = true },
            bottomDockInset = bottomDockInset,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        EditorPane(
            state = state,
            viewModel = viewModel,
            onBack = { viewModel.loadDirectory() },
            onDownload = download,
            onReplace = { replaceLauncher.launch(arrayOf("*/*")) },
            onMove = { moveDialog = true },
            modifier = Modifier.fillMaxSize(),
        )
    }

    createDirectory?.let { directory ->
        NewEntryDialog(directory, onClose = { createDirectory = null }) { name ->
            viewModel.createEntry(name, directory)
            createDirectory = null
        }
    }
    if (moveDialog) MoveDialog(state.document, viewModel) { moveDialog = false }
    if (trashDialog) TrashDialog(state, viewModel) { trashDialog = false }
}

@Composable
private fun FileBrowser(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit,
    onUpload: () -> Unit,
    onTrash: () -> Unit,
    bottomDockInset: Dp,
    modifier: Modifier,
) {
    var rootMenu by remember { mutableStateOf(false) }
    var createMenu by remember { mutableStateOf(false) }
    val canWrite = "files.write" in state.device?.scopes.orEmpty()
    val palette = LocalGlassPalette.current
    val fileSearchResults = state.fileSearchResults
    val searchActive = fileSearchResults != null

    Column(modifier) {
        // Seamless Pure White/Dark Top Header Zone (matching Home screen!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.headerBackground)
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(palette.searchBackground, CircleShape)
                            .clickable { rootMenu = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, Modifier.size(13.5.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            state.roots.find { it.id == state.selectedRootId }?.label.orEmpty(),
                            modifier = Modifier.widthIn(max = 76.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = rootMenu, onDismissRequest = { rootMenu = false }) {
                        state.roots.forEach { root ->
                            DropdownMenuItem(
                                text = { Text(root.label) },
                                onClick = { viewModel.selectRoot(root.id); rootMenu = false },
                            )
                        }
                    }
                }

                CompactSearchPill(
                    query = state.fileSearchQuery,
                    onQueryChange = viewModel::setFileSearchQuery,
                    onSearch = { viewModel.searchFiles() },
                    onClear = viewModel::clearFileSearch,
                    placeholder = stringResource(R.string.search_files_hint),
                    searching = state.searchingFiles,
                    clearContentDescription = stringResource(R.string.clear_search),
                    modifier = Modifier.weight(1f),
                )

                Box {
                    IconButton(onClick = { createMenu = true }, enabled = canWrite, modifier = Modifier.size(27.dp)) {
                        Icon(Icons.Outlined.CreateNewFolder, stringResource(R.string.create), Modifier.size(17.dp))
                    }
                    DropdownMenu(expanded = createMenu, onDismissRequest = { createMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_file)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.InsertDriveFile, null) },
                            onClick = { createMenu = false; onNewFile() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_folder)) },
                            leadingIcon = { Icon(Icons.Outlined.Folder, null) },
                            onClick = { createMenu = false; onNewFolder() },
                        )
                    }
                }
                IconButton(onClick = onUpload, enabled = canWrite, modifier = Modifier.size(27.dp)) {
                    Icon(Icons.Outlined.UploadFile, stringResource(R.string.upload), Modifier.size(17.dp))
                }
                IconButton(onClick = onTrash, modifier = Modifier.size(27.dp)) {
                    Icon(Icons.Outlined.RestoreFromTrash, stringResource(R.string.trash), Modifier.size(17.dp))
                }
            }

            if (fileSearchResults == null) {
                Row(
                    Modifier.fillMaxWidth().height(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    IconButton(
                        onClick = { viewModel.loadDirectory(state.currentPath.substringBeforeLast('/', "")) },
                        enabled = state.currentPath.isNotEmpty(),
                        modifier = Modifier.size(22.dp),
                    ) {
                        Icon(Icons.Outlined.ArrowUpward, stringResource(R.string.go_up), Modifier.size(13.5.dp))
                    }
                    Text(
                        "/${state.currentPath}",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { viewModel.loadDirectory() }, modifier = Modifier.size(22.dp)) {
                        Icon(
                            Icons.Outlined.Refresh,
                            stringResource(R.string.refresh),
                            Modifier.size(13.5.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.search_results_count, fileSearchResults.entries.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (fileSearchResults.truncated) {
                            Text(
                                stringResource(R.string.search_truncated),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.clear_search),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable(onClick = viewModel::clearFileSearch).padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }

        val entries = fileSearchResults?.entries ?: state.directory?.entries.orEmpty()

        if (entries.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    Icons.Outlined.FolderOpen,
                    stringResource(if (searchActive) R.string.no_search_results else R.string.empty_directory),
                )
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 6.dp, bottom = bottomDockInset + 10.dp),
            ) {
                item("file-list-card") {
                    CrispCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 10.dp,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            entries.forEachIndexed { index, entry ->
                                FileCompactRow(
                                    entry = entry,
                                    showRelativePath = searchActive,
                                    onOpen = { viewModel.openEntry(entry) },
                                    onAction = { viewModel.selectEntryForAction(entry) },
                                )
                                if (index != entries.lastIndex) {
                                    HorizontalDivider(Modifier.padding(start = 46.dp), color = palette.divider)
                                }
                            }
                        }
                    }
                }
                if (!searchActive && state.directory?.nextCursor != null) {
                    item("load-more-files") {
                        Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            TextButton(onClick = viewModel::loadNextDirectoryPage) {
                                Text(stringResource(R.string.load_more), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileCompactRow(
    entry: FileEntry,
    showRelativePath: Boolean,
    onOpen: () -> Unit,
    onAction: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val isDir = entry.kind == "directory"
    val ext = entry.name.substringAfterLast('.', "").lowercase()
    val (icon, tint) = when {
        isDir -> Icons.Outlined.Folder to palette.accentBlue
        ext in setOf("md", "markdown", "txt") -> Icons.Outlined.Description to palette.accentIndigo
        ext in setOf("ts", "tsx", "js", "jsx", "kt", "java", "py", "rs", "go") -> Icons.Outlined.Code to palette.accentJade
        ext in setOf("json", "yaml", "yml", "toml", "xml") -> Icons.Outlined.DataObject to palette.accentAmber
        else -> Icons.AutoMirrored.Outlined.InsertDriveFile to MaterialTheme.colorScheme.outline
    }
    val kindLabel = stringResource(if (isDir) R.string.file_kind_directory else R.string.file_kind_file)
    val meta = when {
        showRelativePath && !isDir -> "${entry.path} · ${formatBytes(entry.size)}"
        showRelativePath -> entry.path
        isDir -> kindLabel
        else -> formatBytes(entry.size)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquircleIconBadge(
            icon = icon,
            startColor = tint,
            size = 24.dp,
            iconSize = 14.dp,
            cornerRadius = 6.5.dp,
            softTint = true,
        )
        Column(Modifier.weight(1f).padding(horizontal = 9.dp)) {
            Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            if (showRelativePath) {
                Text(
                    meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!showRelativePath) {
            Text(
                meta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        IconButton(onClick = onAction, modifier = Modifier.size(24.dp)) {
            Icon(
                Icons.Outlined.MoreVert,
                stringResource(R.string.file_actions),
                Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EditorPane(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onBack: (() -> Unit)?,
    onDownload: () -> Unit,
    onReplace: () -> Unit,
    onMove: () -> Unit,
    modifier: Modifier,
) {
    val document = state.document
    if (document == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            EmptyState(Icons.AutoMirrored.Outlined.InsertDriveFile, stringResource(R.string.select_file))
        }
        return
    }
    var actionsExpanded by remember(document.entry.path) { mutableStateOf(false) }
    val markdown = isMarkdown(document.entry.name)
    var markdownPreview by remember(document.entry.path) { mutableStateOf(markdown) }
    var textScale by remember(document.entry.path) { mutableStateOf(1f) }
    val applyZoom: (Float) -> Unit = { factor ->
        textScale = (textScale * factor).coerceIn(MIN_TEXT_SCALE, MAX_TEXT_SCALE)
    }
    val palette = LocalGlassPalette.current
    val editorBackdrop = rememberLayerBackdrop()

    Box(modifier.imePadding()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.cardBackground)
                .layerBackdrop(editorBackdrop),
        ) {
            when {
                document.text == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            Icons.AutoMirrored.Outlined.InsertDriveFile,
                            if (document.entry.kind == "directory") document.entry.kind else stringResource(R.string.binary_file),
                        )
                    }
                }
                markdown && markdownPreview -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .pinchToZoom(document.entry.path, applyZoom)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 14.dp, end = 14.dp, top = 52.dp, bottom = 58.dp),
                    ) {
                        Markdown(
                            content = document.text,
                            typography = fileMarkdownTypography(textScale),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                else -> {
                    NumberedTextEditor(
                        value = document.text,
                        onValueChange = viewModel::updateDocument,
                        scale = textScale,
                        topInset = 50.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .pinchToZoom(document.entry.path, applyZoom),
                    )
                }
            }
        }

        // Sleek 36dp Floating Frosted Liquid Glass Header Capsule
        LiquidGlassFloatingBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            capsule = true,
            backdrop = editorBackdrop,
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 4.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (onBack != null) {
                    LiquidGlassIconButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        backdrop = editorBackdrop,
                        size = 27.dp,
                        iconSize = 15.dp,
                    )
                }
                Column(Modifier.weight(1f).padding(horizontal = 2.dp)) {
                    Text(
                        document.entry.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        document.entry.path,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (markdown && document.text != null) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.chipSelectedBackground, CircleShape)
                            .clickable { markdownPreview = !markdownPreview }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(if (markdownPreview) R.string.markdown_source else R.string.markdown_preview),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                LiquidGlassIconButton(
                    onClick = viewModel::saveDocument,
                    icon = Icons.Outlined.Save,
                    contentDescription = stringResource(R.string.save),
                    enabled = document.text != null && document.entry.writable &&
                        "files.write" in state.device?.scopes.orEmpty(),
                    backdrop = editorBackdrop,
                    tint = if (document.text != null && document.entry.writable) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    size = 27.dp,
                    iconSize = 14.5.dp,
                )
                Box {
                    LiquidGlassIconButton(
                        onClick = { actionsExpanded = true },
                        icon = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.file_actions),
                        backdrop = editorBackdrop,
                        size = 27.dp,
                        iconSize = 15.dp,
                    )
                    DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.download)) },
                            leadingIcon = { Icon(Icons.Outlined.Download, null) },
                            enabled = document.entry.kind == "file",
                            onClick = { actionsExpanded = false; onDownload() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.replace_file)) },
                            leadingIcon = { Icon(Icons.Outlined.FileOpen, null) },
                            enabled = document.entry.kind == "file" && "files.write" in state.device?.scopes.orEmpty(),
                            onClick = { actionsExpanded = false; onReplace() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.rename_move)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.DriveFileMove, null) },
                            enabled = "files.write" in state.device?.scopes.orEmpty(),
                            onClick = { actionsExpanded = false; onMove() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_to_trash)) },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                            enabled = "files.delete" in state.device?.scopes.orEmpty(),
                            onClick = { actionsExpanded = false; viewModel.trashSelected() },
                        )
                    }
                }
            }
        }

        // Sleek 28dp Floating Frosted Liquid Glass Zoom Pill in Bottom-Right
        if (document.text != null) {
            LiquidGlassFloatingBar(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                capsule = true,
                backdrop = editorBackdrop,
                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 2.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    IconButton(
                        onClick = { textScale = (textScale - TEXT_SCALE_STEP).coerceAtLeast(MIN_TEXT_SCALE) },
                        enabled = textScale > MIN_TEXT_SCALE,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(Icons.Outlined.ZoomOut, stringResource(R.string.zoom_out), Modifier.size(13.5.dp))
                    }
                    Text(
                        "${(textScale * 100).roundToInt()}%",
                        modifier = Modifier.width(34.dp).clickable(enabled = textScale != 1f) { textScale = 1f },
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                    )
                    IconButton(
                        onClick = { textScale = (textScale + TEXT_SCALE_STEP).coerceAtMost(MAX_TEXT_SCALE) },
                        enabled = textScale < MAX_TEXT_SCALE,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(Icons.Outlined.ZoomIn, stringResource(R.string.zoom_in), Modifier.size(13.5.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberedTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    scale: Float,
    topInset: Dp,
    modifier: Modifier = Modifier,
) {
    val vertical = rememberScrollState()
    val horizontal = rememberScrollState()
    val lineCount = value.count { it == '\n' } + 1
    val digits = lineCount.toString().length
    val gutterWidth = (digits * 7 * scale + 18).dp
    val codeStyle = scaledTextStyle(
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            lineHeight = 16.5.sp,
        ),
        scale,
    )
    Row(modifier) {
        Box(
            Modifier
                .width(gutterWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                .clipToBounds(),
        ) {
            Text(
                text = (1..lineCount).joinToString("\n"),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, -vertical.value) }
                    .padding(top = topInset, end = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.End,
                style = codeStyle.copy(fontSize = 10.5.sp * scale),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizontal)
                .verticalScroll(vertical)
                .padding(start = 9.dp, end = 10.dp, top = topInset, bottom = 58.dp),
            textStyle = codeStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun fileMarkdownTypography(scale: Float) = markdownTypography(
    h1 = scaledTextStyle(MaterialTheme.typography.headlineMedium, scale),
    h2 = scaledTextStyle(MaterialTheme.typography.headlineSmall, scale),
    h3 = scaledTextStyle(MaterialTheme.typography.titleLarge, scale),
    h4 = scaledTextStyle(MaterialTheme.typography.titleMedium, scale),
    h5 = scaledTextStyle(MaterialTheme.typography.titleSmall, scale),
    h6 = scaledTextStyle(MaterialTheme.typography.titleSmall, scale),
    text = scaledTextStyle(MaterialTheme.typography.bodyMedium, scale),
    code = scaledTextStyle(MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), scale),
    inlineCode = scaledTextStyle(MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), scale),
    quote = scaledTextStyle(MaterialTheme.typography.bodyMedium, scale),
    paragraph = scaledTextStyle(MaterialTheme.typography.bodyMedium, scale),
    ordered = scaledTextStyle(MaterialTheme.typography.bodyMedium, scale),
    bullet = scaledTextStyle(MaterialTheme.typography.bodyMedium, scale),
    list = scaledTextStyle(MaterialTheme.typography.bodyMedium, scale),
    table = scaledTextStyle(MaterialTheme.typography.bodySmall, scale),
)

private fun scaledTextStyle(style: TextStyle, scale: Float): TextStyle = style.copy(
    fontSize = style.fontSize * scale,
    lineHeight = style.lineHeight * scale,
)

private fun Modifier.pinchToZoom(key: Any, onZoom: (Float) -> Unit): Modifier = pointerInput(key) {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            if (event.changes.count { it.pressed } >= 2) {
                val factor = event.calculateZoom()
                if (factor.isFinite() && factor != 1f) onZoom(factor)
                event.changes.forEach { it.consume() }
            }
        } while (event.changes.any { it.pressed })
    }
}

private const val MIN_TEXT_SCALE = 0.75f
private const val MAX_TEXT_SCALE = 2.5f
private const val TEXT_SCALE_STEP = 0.25f

private fun isMarkdown(name: String): Boolean = name.substringAfterLast('.', "").lowercase() in
    setOf("md", "markdown", "mdown", "mkd")

@Composable
private fun NewEntryDialog(directory: Boolean, onClose: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(if (directory) R.string.new_folder else R.string.new_file), style = MaterialTheme.typography.titleLarge) },
        text = { GlassOutlinedField(name, { name = it }, stringResource(R.string.file_name)) },
        confirmButton = {
            Button(onClick = { onCreate(name) }, enabled = name.isNotBlank(), shape = CircleShape) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
private fun MoveDialog(document: OpenDocument?, viewModel: HarnessViewModel, onClose: () -> Unit) {
    var destination by remember(document?.entry?.path) { mutableStateOf(document?.entry?.path.orEmpty()) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.rename_move), style = MaterialTheme.typography.titleLarge) },
        text = { GlassOutlinedField(destination, { destination = it }, stringResource(R.string.destination_path)) },
        confirmButton = {
            Button(onClick = { viewModel.moveSelected(destination); onClose() }, enabled = destination.isNotBlank(), shape = CircleShape) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
private fun TrashDialog(state: HarnessState, viewModel: HarnessViewModel, onClose: () -> Unit) {
    val palette = LocalGlassPalette.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.trash), style = MaterialTheme.typography.titleLarge) },
        text = {
            if (state.trash.isEmpty()) {
                Text(stringResource(R.string.no_trash), style = MaterialTheme.typography.bodySmall)
            } else {
                LazyColumn(Modifier.fillMaxWidth().height(260.dp)) {
                    items(state.trash, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                item.path,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            IconButton(
                                onClick = { viewModel.restoreTrash(item.id) },
                                enabled = "files.delete" in state.device?.scopes.orEmpty(),
                                modifier = Modifier.size(28.dp),
                            ) { Icon(Icons.Outlined.RestoreFromTrash, stringResource(R.string.restore), Modifier.size(16.dp)) }
                        }
                        HorizontalDivider(color = palette.divider)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
    )
}

private fun displayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getString(0)
    }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "upload.bin"
}

private fun displaySize(context: Context, uri: Uri): Long {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst() && !cursor.isNull(0)) return cursor.getLong(0)
    }
    return -1L
}

private fun formatBytes(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "%.1f KiB".format(size / 1024.0)
    else -> "%.1f MiB".format(size / 1024.0 / 1024.0)
}
