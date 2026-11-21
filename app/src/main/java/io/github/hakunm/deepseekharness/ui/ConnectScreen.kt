package io.github.hakunm.deepseekharness.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material3.IconButton
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
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import io.github.hakunm.deepseekharness.ui.theme.LocalIsDarkGlass

@Composable
fun ConnectScreen(state: HarnessState, viewModel: HarnessViewModel) {
    var code by rememberSaveable { mutableStateOf("") }
    var deviceName by rememberSaveable { mutableStateOf(Build.MODEL ?: "Android") }
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current

    Column(Modifier.fillMaxSize()) {
        // Seamless white/dark header matching main app screens
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.headerBackground)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            BrandMark(size = 28.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.connect_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(R.string.connect_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            IconButton(
                onClick = { viewModel.setThemeMode(if (dark) ThemeMode.LIGHT else ThemeMode.DARK) },
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    stringResource(R.string.appearance),
                    Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = ::toggleLanguage,
                modifier = Modifier.size(30.dp),
            ) {
                Icon(Icons.Outlined.Language, stringResource(R.string.language), Modifier.size(18.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
                        cornerRadius = 10.dp,
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                height = 36.dp,
                            ) {
                                Icon(Icons.Outlined.Refresh, null, Modifier.size(14.dp))
                                Text(stringResource(R.string.reconnect), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                CrispCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 10.dp,
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.server),
                            style = MaterialTheme.typography.labelSmall,
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
                            height = 35.dp,
                        ) {
                            if (state.busy) {
                                CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 1.8.dp)
                            } else {
                                Icon(Icons.Outlined.WifiTethering, null, Modifier.size(15.dp))
                            }
                            Text(stringResource(R.string.test_connection), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                CrispCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 10.dp,
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.device_pairing),
                            style = MaterialTheme.typography.labelSmall,
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
                            height = 38.dp,
                        ) {
                            if (state.busy) {
                                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.8.dp, color = Color.White)
                            } else {
                                Icon(Icons.Outlined.CheckCircle, null, Modifier.size(15.dp))
                            }
                            Text(stringResource(R.string.connect), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
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
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.labelMedium) } },
        supportingText = supportingText?.let { { Text(it, style = MaterialTheme.typography.labelSmall) } },
        leadingIcon = leadingIcon?.let { { Icon(it, null, Modifier.size(16.dp)) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        shape = RoundedRectangle(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = palette.cardBackground,
            unfocusedContainerColor = palette.searchBackground.copy(alpha = 0.65f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}
