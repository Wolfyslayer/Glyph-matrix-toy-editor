/*
 * ExportGuideScreen.kt
 *
 * UI screen providing export options and manual integration guide for
 * activating custom glyph matrix animations on Nothing Phone devices.
 *
 * This screen provides users with:
 * - One-tap export to GDK-compatible format
 * - Step-by-step instructions for manual integration
 * - Links to Nothing developer resources
 * - Troubleshooting tips
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Data class representing an export step.
 */
data class ExportStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * Composable screen for glyph animation export and integration guide.
 *
 * @param onExportClick Callback when the export button is clicked
 * @param onNavigateBack Callback to navigate back
 * @param isNothingDevice Whether the device is a Nothing Phone
 * @param isSdkAvailable Whether the GDK SDK is available
 * @param exportedFilePath Path to the exported file (if any)
 * @param isExporting Whether an export is in progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportGuideScreen(
    onExportClick: () -> Unit,
    onNavigateBack: () -> Unit,
    isNothingDevice: Boolean,
    isSdkAvailable: Boolean,
    exportedFilePath: String? = null,
    isExporting: Boolean = false
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export to Nothing Phone") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Device status card
            DeviceStatusCard(
                isNothingDevice = isNothingDevice,
                isSdkAvailable = isSdkAvailable
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Export button
            ExportButton(
                onClick = onExportClick,
                isExporting = isExporting,
                exportedFilePath = exportedFilePath
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Manual integration guide
            if (!isSdkAvailable || !isNothingDevice) {
                ManualIntegrationGuide()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Developer resources
            DeveloperResourcesSection()
        }
    }
}

/**
 * Card showing device and SDK status.
 */
@Composable
private fun DeviceStatusCard(
    isNothingDevice: Boolean,
    isSdkAvailable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isNothingDevice && isSdkAvailable)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isNothingDevice)
                        Icons.Default.PhoneAndroid
                    else
                        Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isNothingDevice)
                            "Nothing Phone Detected"
                        else
                            "Non-Nothing Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isNothingDevice && isSdkAvailable)
                            "Ready for direct activation"
                        else if (isNothingDevice)
                            "SDK not available - manual export required"
                        else
                            "Export will be saved for transfer to Nothing Phone",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isNothingDevice && isSdkAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GDK Status: Connected",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Export action button with status.
 */
@Composable
private fun ExportButton(
    onClick: () -> Unit,
    isExporting: Boolean,
    exportedFilePath: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isExporting
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Exporting...")
            } else {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Animation")
            }
        }

        if (exportedFilePath != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Export Complete!",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = exportedFilePath,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Manual integration guide section.
 */
@Composable
private fun ManualIntegrationGuide() {
    val steps = listOf(
        ExportStep(
            stepNumber = 1,
            title = "Export Your Animation",
            description = "Tap the Export button above to save your animation in GDK-compatible format (.glyph.json)",
            icon = Icons.Default.Download
        ),
        ExportStep(
            stepNumber = 2,
            title = "Transfer to Nothing Phone",
            description = "Copy the exported file to your Nothing Phone using USB transfer, cloud storage, or nearby sharing",
            icon = Icons.Default.Share
        ),
        ExportStep(
            stepNumber = 3,
            title = "Enable Developer Options",
            description = "On your Nothing Phone, go to Settings > About > tap Build Number 7 times to enable Developer Options",
            icon = Icons.Default.Settings
        ),
        ExportStep(
            stepNumber = 4,
            title = "Enable Glyph Debug Mode",
            description = "Connect your phone via ADB and run:\nadb shell settings put global nt_glyph_interface_debug_enable 1",
            icon = Icons.Default.Code
        ),
        ExportStep(
            stepNumber = 5,
            title = "Install with GDK App",
            description = "Use a GDK-compatible app or build your own with the Nothing GDK to load and play your animation",
            icon = Icons.Default.PlayArrow
        ),
        ExportStep(
            stepNumber = 6,
            title = "Activate in Glyph Settings",
            description = "Your custom toy will appear in Settings > Glyph Interface > Glyph Toys on your Nothing Phone",
            icon = Icons.Default.Stars
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manual Integration Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            steps.forEach { step ->
                StepItem(step = step)
                if (step != steps.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * Individual step item in the guide.
 */
@Composable
private fun StepItem(step: ExportStep) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.stepNumber.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Developer resources section with links.
 */
@Composable
private fun DeveloperResourcesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Developer Resources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ResourceLink(
                title = "Glyph Matrix Developer Kit",
                url = "github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ResourceLink(
                title = "Glyph Developer Kit Documentation",
                url = "nothing.tech/pages/glyph-developer-kit"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ResourceLink(
                title = "Nothing Developer Community",
                url = "nothing.community"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Note: This app uses the official Nothing GDK. All integrations follow Nothing's public API guidelines.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Resource link item.
 */
@Composable
private fun ResourceLink(
    title: String,
    url: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Link,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
