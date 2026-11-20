package io.github.hakunm.deepseekharness.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.ModelTraining
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import io.github.hakunm.deepseekharness.BuildConfig
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.ThemeMode
import io.github.hakunm.deepseekharness.data.CustomProviderCreate
import io.github.hakunm.deepseekharness.data.ProviderModel
import io.github.hakunm.deepseekharness.data.ProviderPatch
import io.github.hakunm.deepseekharness.data.ProviderView
import io.github.hakunm.deepseekharness.ui.glass.CompactSegmentedControl
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.SquircleIconBadge
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import io.github.hakunm.deepseekharness.ui.theme.LocalIsDarkGlass

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: HarnessState,
    viewModel: HarnessViewModel,
    bottomDockInset: Dp = 0.dp,
) {
    var editedProvider by remember { mutableStateOf<ProviderView?>(null) }
    var creatingProvider by remember { mutableStateOf(false) }
    val scopes = state.device?.scopes.orEmpty()
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 700.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Compact Header Row
            Row(
                Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.settings),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                )
                IconButton(
                    onClick = { viewModel.setThemeMode(if (dark) ThemeMode.LIGHT else ThemeMode.DARK) },
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        stringResource(R.string.appearance),
                        Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = viewModel::refreshDashboard,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(Icons.Outlined.Refresh, stringResource(R.string.refresh), Modifier.size(20.dp))
                }
            }

            // Appearance & Language Card
            SectionPanel(R.string.appearance) {
                Column(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CompactSegmentedControl(
                        items = listOf(
                            stringResource(R.string.theme_system),
                            stringResource(R.string.theme_light),
                            stringResource(R.string.theme_dark),
                        ),
                        selectedIndex = when (state.themeMode) {
                            ThemeMode.SYSTEM -> 0
                            ThemeMode.LIGHT -> 1
                            ThemeMode.DARK -> 2
                        },
                        onSelect = { idx ->
                            viewModel.setThemeMode(
                                when (idx) {
                                    1 -> ThemeMode.LIGHT
                                    2 -> ThemeMode.DARK
                                    else -> ThemeMode.SYSTEM
                                },
                            )
                        },
                    )
                    val english = AppCompatDelegate.getApplicationLocales().toLanguageTags().startsWith("en")
                    CompactSegmentedControl(
                        items = listOf(
                            stringResource(R.string.chinese),
                            stringResource(R.string.english),
                        ),
                        selectedIndex = if (english) 1 else 0,
                        onSelect = { idx -> if ((idx == 1) != english) toggleLanguage() },
                    )
                }
            }

            // Model Providers Card
            SectionPanel(
                title = R.string.model_providers,
                trailing = {
                    if (state.providerSettings?.customProvider?.available == true) {
                        val canAdd = "settings.write" in scopes && state.providerSettings.writable
                        Row(
                            modifier = Modifier
                                .clickable(enabled = canAdd) { creatingProvider = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(Icons.Outlined.Add, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                stringResource(R.string.add_custom_provider),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                },
            ) {
                when {
                    "settings.read" !in scopes -> {
                        Box(Modifier.padding(12.dp)) {
                            WarningBanner(stringResource(R.string.settings_permission_required))
                        }
                    }

                    state.providerSettings == null -> {
                        Row(
                            Modifier.fillMaxWidth().height(76.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) }
                    }

                    else -> {
                        Column(Modifier.fillMaxWidth()) {
                            state.providerSettings.providers.forEachIndexed { index, provider ->
                                ProviderRow(
                                    provider = provider,
                                    index = index,
                                    canEdit = "settings.write" in scopes && state.providerSettings.writable && provider.configurable,
                                    onClick = {
                                        viewModel.clearDiscoveredModels()
                                        editedProvider = provider
                                    },
                                )
                                if (index != state.providerSettings.providers.lastIndex) {
                                    HorizontalDivider(Modifier.padding(start = 56.dp), color = palette.divider)
                                }
                            }
                            Text(
                                stringResource(R.string.provider_secret_note),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }

            // Connection & Capabilities Card
            SectionPanel(R.string.connection) {
                Column(Modifier.fillMaxWidth()) {
                    SettingRow(
                        icon = Icons.Outlined.CloudDone,
                        title = stringResource(R.string.endpoint),
                        value = state.endpoint,
                        iconStartColor = palette.accentBlue,
                        iconEndColor = palette.accentCyan,
                    ) {
                        StatusPill(
                            stringResource(if (state.eventsConnected) R.string.online else R.string.connected),
                            active = state.eventsConnected,
                        )
                    }
                    state.serverPluginVersion?.let { pluginVer ->
                        HorizontalDivider(Modifier.padding(start = 56.dp), color = palette.divider)
                        SettingRow(
                            icon = Icons.Outlined.Extension,
                            title = stringResource(R.string.server_version),
                            value = "dsh-workspace v$pluginVer",
                            iconStartColor = palette.accentIndigo,
                            iconEndColor = palette.accentBlue,
                        )
                    }
                    if (state.capabilities.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(start = 56.dp), color = palette.divider)
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                stringResource(R.string.server_capabilities),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                state.capabilities.forEach { cap -> SmallTagPill(cap) }
                            }
                        }
                    }
                    if (state.endpoint.startsWith("http://")) {
                        Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            WarningBanner(stringResource(R.string.http_warning))
                        }
                    }
                }
            }

            // Device & Permissions Card
            SectionPanel(R.string.device) {
                Column(Modifier.fillMaxWidth()) {
                    SettingRow(
                        icon = Icons.Outlined.PhoneAndroid,
                        title = state.device?.name.orEmpty(),
                        value = state.device?.id.orEmpty(),
                        iconStartColor = palette.accentEmerald,
                        iconEndColor = palette.accentCyan,
                    )
                    if (scopes.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(start = 56.dp), color = palette.divider)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            scopes.forEach { scope -> SmallTagPill(scope) }
                        }
                    }
                }
            }

            // About Card
            SectionPanel(R.string.about) {
                Column(Modifier.fillMaxWidth()) {
                    SettingRow(
                        icon = Icons.Outlined.Code,
                        title = stringResource(R.string.author),
                        value = stringResource(R.string.author_value),
                        iconStartColor = palette.accentAmber,
                        iconEndColor = palette.accentCoral,
                    )
                    HorizontalDivider(Modifier.padding(start = 56.dp), color = palette.divider)
                    SettingRow(
                        icon = Icons.Outlined.CheckCircle,
                        title = stringResource(R.string.version),
                        value = "v${BuildConfig.VERSION_NAME}",
                        iconStartColor = palette.accentBlue,
                        iconEndColor = palette.accentIndigo,
                    )
                }
            }

            // Clean Destructive Action Card
            CrispCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 15.dp,
                backgroundColor = palette.dangerTint,
                borderColor = palette.dangerText.copy(alpha = 0.22f),
                onClick = viewModel::disconnect,
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.DeleteForever, null, Modifier.size(18.dp), tint = palette.dangerText)
                    Text(
                        stringResource(R.string.disconnect),
                        color = palette.dangerText,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(Modifier.height(bottomDockInset + 12.dp))
        }
    }

    editedProvider?.let { provider ->
        ProviderEditor(state, viewModel, provider) { editedProvider = null }
    }
    if (creatingProvider) {
        CustomProviderEditor(state, viewModel) { creatingProvider = false }
    }
}

@Composable
private fun ProviderRow(provider: ProviderView, index: Int, canEdit: Boolean, onClick: () -> Unit) {
    val palette = LocalGlassPalette.current
    val (c1, c2) = when (index % 4) {
        0 -> palette.accentBlue to palette.accentCyan
        1 -> palette.accentIndigo to palette.accentBlue
        2 -> palette.accentEmerald to palette.accentCyan
        else -> palette.accentAmber to palette.accentCoral
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canEdit, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquircleIconBadge(
            icon = Icons.Outlined.ModelTraining,
            startColor = if (provider.active || provider.credential.configured) c1 else MaterialTheme.colorScheme.outline,
            endColor = if (provider.active || provider.credential.configured) c2 else MaterialTheme.colorScheme.outline,
            size = 34.dp,
            iconSize = 18.dp,
            cornerRadius = 10.dp,
        )
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    provider.displayName,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (provider.active) {
                    SmallTagPill(
                        text = stringResource(R.string.active),
                        backgroundColor = palette.successTint,
                        contentColor = palette.successText,
                    )
                }
            }
            Text(
                if (provider.credential.configured) stringResource(R.string.credential_configured)
                else stringResource(R.string.credential_missing),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (!provider.error.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Icon(Icons.Outlined.WarningAmber, null, Modifier.size(12.dp), tint = palette.warningText)
                    Text(
                        provider.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = palette.warningText,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        if (provider.credential.configured) {
            Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp), tint = palette.successText)
        }
        if (canEdit) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                null,
                Modifier.padding(start = 4.dp).size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomProviderEditor(
    state: HarnessState,
    viewModel: HarnessViewModel,
    onClose: () -> Unit,
) {
    val capability = state.providerSettings?.customProvider ?: return
    var id by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var protocol by remember(capability.protocols) { mutableStateOf(capability.protocols.firstOrNull().orEmpty()) }
    var apiKey by remember { mutableStateOf("") }
    var modelIds by remember { mutableStateOf("") }
    var protocolMenu by remember { mutableStateOf(false) }
    val models = parseModelIds(modelIds)
    val routeValid = id.matches(Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$"))
    val ready = routeValid && baseUrl.isNotBlank() && protocol.isNotBlank() && models.isNotEmpty()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedRectangle(24.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState())
                .imePadding().padding(horizontal = 18.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(stringResource(R.string.add_custom_provider), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.custom_provider_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            GlassOutlinedField(
                value = id,
                onValueChange = { id = it.trim().lowercase() },
                label = stringResource(R.string.provider_id),
                supportingText = stringResource(R.string.provider_id_hint),
                isError = id.isNotEmpty() && !routeValid,
            )
            GlassOutlinedField(
                value = displayName,
                onValueChange = { displayName = it },
                label = stringResource(R.string.provider_display_name),
            )
            GlassOutlinedField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = stringResource(R.string.base_url),
                leadingIcon = Icons.Outlined.Link,
                keyboardType = KeyboardType.Uri,
            )
            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { protocolMenu = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedRectangle(12.dp),
                ) {
                    Text(protocol.ifBlank { stringResource(R.string.provider_protocol) }, Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
                }
                DropdownMenu(expanded = protocolMenu, onDismissRequest = { protocolMenu = false }) {
                    capability.protocols.forEach { choice ->
                        DropdownMenuItem(text = { Text(choice) }, onClick = { protocol = choice; protocolMenu = false })
                    }
                }
            }
            GlassOutlinedField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = stringResource(R.string.api_key_optional),
                leadingIcon = Icons.Outlined.Key,
                visualTransformation = PasswordVisualTransformation(),
            )
            GlassOutlinedField(
                value = modelIds,
                onValueChange = { modelIds = it },
                modifier = Modifier.height(126.dp),
                label = stringResource(R.string.model_ids),
                supportingText = stringResource(R.string.model_ids_required_hint),
                singleLine = false,
            )
            Button(
                onClick = {
                    viewModel.createCustomProvider(
                        CustomProviderCreate(
                            id = id,
                            displayName = displayName.trim().takeIf(String::isNotEmpty),
                            baseURL = baseUrl.trim(),
                            api = protocol,
                            apiKey = apiKey.trim().takeIf(String::isNotEmpty),
                            models = models,
                            expectedRevision = capability.revision,
                        ),
                    )
                    onClose()
                },
                enabled = ready && !state.busy,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = CircleShape,
            ) { Text(stringResource(R.string.create_provider)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderEditor(
    state: HarnessState,
    viewModel: HarnessViewModel,
    provider: ProviderView,
    onClose: () -> Unit,
) {
    var displayName by remember(provider.id) { mutableStateOf(provider.config.displayName ?: provider.displayName) }
    var baseUrl by remember(provider.id) { mutableStateOf(provider.config.baseURL.orEmpty()) }
    var protocol by remember(provider.id) { mutableStateOf(provider.config.api.orEmpty()) }
    var apiKey by remember(provider.id) { mutableStateOf("") }
    var modelIds by remember(provider.id) { mutableStateOf(provider.config.models.joinToString("\n") { it.id }) }
    var confirmRemove by remember(provider.id) { mutableStateOf(false) }
    val discovered = state.discoveredModels
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedRectangle(24.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState())
                .imePadding().padding(horizontal = 18.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(provider.displayName, style = MaterialTheme.typography.headlineSmall)
            Text(provider.id, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (!provider.error.isNullOrBlank()) WarningBanner(provider.error)
            Text(
                if (provider.credential.configured) stringResource(R.string.credential_reference, provider.credential.ref)
                else stringResource(R.string.credential_missing),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            GlassOutlinedField(displayName, { displayName = it }, stringResource(R.string.provider_display_name))
            GlassOutlinedField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = stringResource(R.string.base_url),
                placeholder = provider.defaultBaseURL ?: stringResource(R.string.server_default),
                leadingIcon = Icons.Outlined.Link,
                keyboardType = KeyboardType.Uri,
            )
            GlassOutlinedField(
                value = protocol,
                onValueChange = { protocol = it },
                label = stringResource(R.string.provider_protocol),
                placeholder = stringResource(R.string.server_default),
            )
            GlassOutlinedField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = stringResource(R.string.api_key),
                placeholder = if (provider.credential.configured) stringResource(R.string.keep_existing_secret)
                else stringResource(R.string.enter_api_key),
                leadingIcon = Icons.Outlined.Key,
                visualTransformation = PasswordVisualTransformation(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = { viewModel.discoverModels(provider.id, baseUrl, protocol, apiKey) },
                    enabled = !state.busy,
                    shape = CircleShape,
                ) {
                    if (state.busy) CircularProgressIndicator(Modifier.padding(end = 6.dp).size(14.dp), strokeWidth = 2.dp)
                    Icon(Icons.Outlined.ModelTraining, null, Modifier.size(16.dp))
                    Text(stringResource(R.string.discover_models), Modifier.padding(start = 6.dp))
                }
            }
            if (discovered.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.discovered_models, discovered.size),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    TextButton(onClick = { modelIds = discovered.joinToString("\n") { it.id } }) {
                        Text(stringResource(R.string.use_discovered_models))
                    }
                }
            }
            GlassOutlinedField(
                value = modelIds,
                onValueChange = { modelIds = it },
                modifier = Modifier.height(126.dp),
                label = stringResource(R.string.model_ids),
                supportingText = stringResource(
                    if (provider.config.modelsInherited) R.string.inherited_model_ids_hint
                    else R.string.model_ids_hint,
                ),
                singleLine = false,
            )
            Button(
                onClick = {
                    viewModel.updateProvider(
                        provider.id,
                        ProviderPatch(
                            displayName = displayName.trim(),
                            baseURL = baseUrl.trim(),
                            api = protocol.trim(),
                            apiKey = apiKey.takeIf { it.isNotBlank() },
                            models = parseModelIds(modelIds).takeUnless { models ->
                                provider.config.modelsInherited &&
                                    models.map { it.id } == provider.config.models.map { it.id }
                            },
                        ),
                    )
                    onClose()
                },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = CircleShape,
            ) { Text(stringResource(R.string.save_provider)) }

            if (provider.removable) {
                TextButton(
                    onClick = { confirmRemove = true },
                    enabled = !state.busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(17.dp))
                    Text(stringResource(R.string.remove_provider), Modifier.padding(start = 5.dp))
                }
            }
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text(stringResource(R.string.remove_provider_title)) },
            text = { Text(stringResource(R.string.remove_provider_warning, provider.displayName)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeProvider(provider.id, state.providerSettings?.customProvider?.revision)
                        confirmRemove = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = CircleShape,
                ) { Text(stringResource(R.string.confirm), color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

private fun parseModelIds(value: String): List<ProviderModel> = value
    .split('\n', ',')
    .map(String::trim)
    .filter(String::isNotEmpty)
    .distinct()
    .map(::ProviderModel)
