package io.github.hakunm.deepseekharness.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import io.github.hakunm.deepseekharness.HarnessState
import io.github.hakunm.deepseekharness.HarnessViewModel
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.ThemeMode
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassButton
import io.github.hakunm.deepseekharness.ui.glass.LiquidGlassIconButton
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import io.github.hakunm.deepseekharness.ui.theme.LocalIsDarkGlass

@Composable
fun ConnectScreen(state: HarnessState, viewModel: HarnessViewModel) {
    var code by rememberSaveable { mutableStateOf("") }
    var deviceName by rememberSaveable { mutableStateOf(Build.MODEL ?: "Android") }
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Compact Top Row: Brand + Title + Appearance & Language icons
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BrandMark(size = 42.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.connect_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        stringResource(R.string.connect_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                LiquidGlassIconButton(
                    onClick = { viewModel.setThemeMode(if (dark) ThemeMode.LIGHT else ThemeMode.DARK) },
                    icon = if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = stringResource(R.string.appearance),
                    size = 34.dp,
                )
                LiquidGlassIconButton(
                    onClick = ::toggleLanguage,
                    icon = Icons.Outlined.Language,
                    contentDescription = stringResource(R.string.language),
                    size = 34.dp,
                )
            }

            AnimatedVisibility(visible = state.healthOk) {
                val versionSuffix = state.serverPluginVersion?.let { " · v$it" }.orEmpty()
                StatusPill(
                    label = stringResource(R.string.connection_ok) + versionSuffix,
                    active = true,
                )
            }

            AnimatedVisibility(visible = state.hasSavedConnection) {
                CrispCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(
                            stringResource(R.string.saved_connection_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LiquidGlassButton(
                            onClick = viewModel::retryRestore,
                            enabled = !state.busy,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            height = 40.dp,
                        ) {
                            Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                            Text(stringResource(R.string.reconnect), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Compact Server Card
            CrispCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.server),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GlassOutlinedField(
                        value = state.endpointDraft,
                        onValueChange = viewModel::setEndpointDraft,
                        label = stringResource(R.string.server_address),
                        leadingIcon = Icons.Outlined.Link,
                        keyboardType = KeyboardType.Uri,
                    )
                    if (state.endpointDraft.trim().startsWith("http://")) {
                        WarningBanner(stringResource(R.string.http_warning))
                    }
                    LiquidGlassButton(
                        onClick = { viewModel.testConnection(state.endpointDraft) },
                        enabled = !state.busy && state.endpointDraft.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        height = 40.dp,
                    ) {
                        if (state.busy) {
                            CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.WifiTethering, null, Modifier.size(16.dp))
                        }
                        Text(stringResource(R.string.test_connection), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Compact Pairing Card
            CrispCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.device_pairing),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GlassOutlinedField(
                        value = code,
                        onValueChange = { code = it },
                        label = stringResource(R.string.pairing_code),
                        leadingIcon = Icons.Outlined.Key,
                    )
                    GlassOutlinedField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = stringResource(R.string.device_name),
                        leadingIcon = Icons.Outlined.PhoneAndroid,
                    )
                    LiquidGlassButton(
                        onClick = { viewModel.pair(state.endpointDraft, code, deviceName) },
                        enabled = !state.busy && state.endpointDraft.isNotBlank() && code.isNotBlank() && deviceName.isNotBlank(),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                    ) {
                        if (state.busy) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Outlined.CheckCircle, null, Modifier.size(17.dp))
                        }
                        Text(stringResource(R.string.connect), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun GlassOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    val palette = LocalGlassPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        supportingText = supportingText?.let { { Text(it, style = MaterialTheme.typography.labelSmall) } },
        leadingIcon = leadingIcon?.let { { Icon(it, null, Modifier.size(18.dp)) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        shape = RoundedRectangle(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = palette.cardBackground,
            unfocusedContainerColor = palette.searchBackground.copy(alpha = 0.55f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = palette.cardBorder,
        ),
    )
}
