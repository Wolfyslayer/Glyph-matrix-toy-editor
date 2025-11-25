/*
 * SettingsScreen.kt
 *
 * Settings screen for the Glyph Matrix Toy Editor.
 *
 * Provides configuration options for the app, including
 * matrix dimensions, export settings, and app preferences.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glyphmatrix.toyeditor.data.models.MatrixModel

/**
 * App settings data class.
 */
data class AppSettings(
    val defaultMatrixWidth: Int = MatrixModel.DEFAULT_WIDTH,
    val defaultMatrixHeight: Int = MatrixModel.DEFAULT_HEIGHT,
    val defaultFrameDuration: Long = 100L,
    val showGrid: Boolean = true,
    val autoSave: Boolean = true,
    val darkMode: Boolean = true,
    val hapticFeedback: Boolean = true
)

/**
 * Settings screen composable.
 *
 * @param settings Current app settings
 * @param onSettingsChange Called when settings are updated
 * @param onNavigateBack Called to navigate back
 * @param onNavigateToHelp Called to navigate to help/onboarding
 * @param isNothingPhone Whether running on a Nothing Phone
 * @param isSdkAvailable Whether GDK SDK is available
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    isNothingPhone: Boolean,
    isSdkAvailable: Boolean
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Status Section
            SettingsSection(
                title = "Device",
                icon = Icons.Default.Phone
            ) {
                DeviceStatusCard(
                    isNothingPhone = isNothingPhone,
                    isSdkAvailable = isSdkAvailable
                )
            }

            // Editor Settings Section
            SettingsSection(
                title = "Editor",
                icon = Icons.Default.Settings
            ) {
                // Default Matrix Width
                SettingsSlider(
                    title = "Default Matrix Width",
                    value = settings.defaultMatrixWidth.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(defaultMatrixWidth = it.toInt()))
                    },
                    valueRange = MatrixModel.MIN_DIMENSION.toFloat()..MatrixModel.MAX_DIMENSION.toFloat(),
                    valueDisplay = "${settings.defaultMatrixWidth}px"
                )

                // Default Matrix Height
                SettingsSlider(
                    title = "Default Matrix Height",
                    value = settings.defaultMatrixHeight.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(defaultMatrixHeight = it.toInt()))
                    },
                    valueRange = MatrixModel.MIN_DIMENSION.toFloat()..MatrixModel.MAX_DIMENSION.toFloat(),
                    valueDisplay = "${settings.defaultMatrixHeight}px"
                )

                // Default Frame Duration
                SettingsSlider(
                    title = "Default Frame Duration",
                    value = settings.defaultFrameDuration.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(defaultFrameDuration = it.toLong()))
                    },
                    valueRange = 16f..1000f,
                    valueDisplay = "${settings.defaultFrameDuration}ms"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show Grid
                SettingsSwitch(
                    title = "Show Grid",
                    description = "Display grid lines on the canvas",
                    checked = settings.showGrid,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(showGrid = it))
                    }
                )

                // Auto Save
                SettingsSwitch(
                    title = "Auto Save",
                    description = "Automatically save projects",
                    checked = settings.autoSave,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(autoSave = it))
                    }
                )

                // Haptic Feedback
                SettingsSwitch(
                    title = "Haptic Feedback",
                    description = "Vibrate on interactions",
                    checked = settings.hapticFeedback,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(hapticFeedback = it))
                    }
                )
            }

            // About Section
            SettingsSection(
                title = "About",
                icon = Icons.Default.Info
            ) {
                SettingsInfoItem(
                    title = "Version",
                    value = "1.0.0"
                )

                SettingsInfoItem(
                    title = "License",
                    value = "MIT License"
                )

                SettingsClickableItem(
                    title = "Help & Tutorial",
                    description = "Learn how to use the app",
                    onClick = onNavigateToHelp
                )
            }
        }
    }
}

/**
 * Settings section wrapper.
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

/**
 * Settings slider item.
 */
@Composable
private fun SettingsSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

/**
 * Settings switch item.
 */
@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Settings info item (read-only).
 */
@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Settings clickable item.
 */
@Composable
private fun SettingsClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Device status card.
 */
@Composable
private fun DeviceStatusCard(
    isNothingPhone: Boolean,
    isSdkAvailable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isNothingPhone && isSdkAvailable) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isNothingPhone) "Nothing Phone Detected" else "Non-Nothing Device",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSdkAvailable) {
                    "GDK SDK: Available"
                } else {
                    "GDK SDK: Not Available"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isNothingPhone) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You can still create and export animations. Transfer to a Nothing Phone to use with the Glyph interface.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
