/*
 * WidgetPanel.kt
 *
 * Widget selection and configuration panel composable.
 *
 * This composable provides a panel for selecting, configuring, and
 * placing pre-built widgets onto the matrix canvas. Widgets include
 * functional elements like clocks and battery indicators.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glyphmatrix.toyeditor.data.models.WidgetAsset
import com.glyphmatrix.toyeditor.data.models.WidgetType

/**
 * Panel for selecting and placing widgets.
 *
 * @param widgets List of available widget assets
 * @param selectedWidget Currently selected widget type (if any)
 * @param onWidgetSelect Called when a widget is selected
 * @param onWidgetDrag Called when a widget is being dragged for placement
 * @param onClose Called when the panel is closed
 * @param modifier Modifier for the panel
 */
@Composable
fun WidgetPanel(
    widgets: List<WidgetAsset> = WidgetAsset.getBuiltInWidgets(),
    selectedWidget: WidgetType? = null,
    onWidgetSelect: (WidgetType) -> Unit,
    onWidgetDrag: ((WidgetType, Float, Float) -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Widgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Widget Panel"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Widget grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                widgets.forEach { widget ->
                    WidgetCard(
                        widget = widget,
                        isSelected = selectedWidget == widget.type,
                        onClick = { onWidgetSelect(widget.type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Instructions
            Text(
                text = "Tap a widget to select, then tap on the canvas to place it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Individual widget card in the panel.
 */
@Composable
private fun WidgetCard(
    widget: WidgetAsset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Widget icon
            Icon(
                imageVector = getWidgetIcon(widget.type),
                contentDescription = widget.name,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Widget preview
            WidgetPreview(
                pixels = widget.previewPixels,
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Widget name
            Text(
                text = widget.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            // Widget size
            Text(
                text = "${widget.type.width}×${widget.type.height}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Preview canvas for widget pixels.
 */
@Composable
private fun WidgetPreview(
    pixels: Array<IntArray>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)
    ) {
        if (pixels.isEmpty()) return@Canvas

        val height = pixels.size
        val width = pixels.maxOfOrNull { it.size } ?: 0
        if (width == 0 || height == 0) return@Canvas

        val pixelWidth = size.width / width
        val pixelHeight = size.height / height
        val pixelSize = minOf(pixelWidth, pixelHeight)

        // Center the preview
        val offsetX = (size.width - pixelSize * width) / 2
        val offsetY = (size.height - pixelSize * height) / 2

        for (y in pixels.indices) {
            for (x in pixels[y].indices) {
                val brightness = pixels[y][x]
                if (brightness > 0) {
                    val px = offsetX + x * pixelSize
                    val py = offsetY + y * pixelSize
                    val alpha = brightness.toFloat() / 255f

                    drawRect(
                        color = Color.White.copy(alpha = alpha),
                        topLeft = Offset(px, py),
                        size = Size(pixelSize, pixelSize)
                    )
                }
            }
        }
    }
}

/**
 * Gets the appropriate icon for a widget type.
 */
private fun getWidgetIcon(type: WidgetType): ImageVector {
    return when (type) {
        WidgetType.CLOCK -> Icons.Default.AccessTime
        WidgetType.BATTERY -> Icons.Default.BatteryFull
        WidgetType.BATTERY_PERCENT -> Icons.Default.Percent
    }
}

/**
 * Compact widget selector for toolbar integration.
 */
@Composable
fun WidgetSelector(
    selectedWidget: WidgetType?,
    onWidgetSelect: (WidgetType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WidgetType.entries.forEach { type ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedWidget == type) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable {
                        onWidgetSelect(if (selectedWidget == type) null else type)
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getWidgetIcon(type),
                    contentDescription = type.displayName,
                    modifier = Modifier.size(24.dp),
                    tint = if (selectedWidget == type) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
