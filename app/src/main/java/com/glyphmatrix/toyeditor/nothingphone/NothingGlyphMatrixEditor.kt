/*
 * NothingGlyphMatrixEditor.kt
 *
 * Full-featured editor screen for the Nothing Phone 3 Glyph Matrix.
 *
 * This composable provides a complete editing interface for the Glyph Matrix,
 * including:
 * - Interactive LED canvas with tap-to-toggle
 * - Toolbar with drawing tools (draw, erase, fill, clear)
 * - Brightness control
 * - Export functionality
 *
 * The editor is designed to be modular and can be integrated into
 * the main app navigation or used standalone.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.nothingphone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightbulbOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Drawing tool modes for the Glyph Matrix editor.
 */
enum class GlyphDrawingTool {
    /** Draw mode - tap to turn LED on */
    DRAW,
    /** Erase mode - tap to turn LED off */
    ERASE,
    /** Toggle mode - tap to toggle LED state */
    TOGGLE
}

/**
 * Editor state holder for managing undo/redo history.
 */
data class GlyphEditorState(
    val matrixState: GlyphMatrixState = GlyphMatrixState.empty(),
    val undoStack: List<GlyphMatrixState> = emptyList(),
    val redoStack: List<GlyphMatrixState> = emptyList()
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun pushState(newState: GlyphMatrixState): GlyphEditorState {
        return copy(
            matrixState = newState,
            undoStack = (undoStack + matrixState).takeLast(50),
            redoStack = emptyList()
        )
    }

    fun undo(): GlyphEditorState {
        if (!canUndo) return this
        val previousState = undoStack.last()
        return copy(
            matrixState = previousState,
            undoStack = undoStack.dropLast(1),
            redoStack = redoStack + matrixState
        )
    }

    fun redo(): GlyphEditorState {
        if (!canRedo) return this
        val nextState = redoStack.last()
        return copy(
            matrixState = nextState,
            undoStack = undoStack + matrixState,
            redoStack = redoStack.dropLast(1)
        )
    }
}

/**
 * Main editor screen for the Nothing Phone 3 Glyph Matrix.
 *
 * This composable provides a complete editing interface with:
 * - Interactive LED canvas
 * - Drawing tools (draw, erase, toggle)
 * - Brightness control
 * - Undo/redo support
 * - Clear and fill actions
 *
 * @param editorState Current editor state
 * @param onEditorStateChange Called when editor state changes
 * @param onExport Called when export is requested (provides the matrix state)
 * @param onNavigateBack Called when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NothingGlyphMatrixEditor(
    editorState: GlyphEditorState,
    onEditorStateChange: (GlyphEditorState) -> Unit,
    onExport: ((GlyphMatrixState) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    var currentTool by remember { mutableStateOf(GlyphDrawingTool.TOGGLE) }
    var brightness by remember { mutableFloatStateOf(255f) }

    val matrixState = editorState.matrixState

    // Helper to update state with undo history
    fun updateState(newState: GlyphMatrixState) {
        onEditorStateChange(editorState.pushState(newState))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Glyph Matrix Editor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nothing Phone (3) • ${matrixState.litLedCount}/${matrixState.totalLeds} LEDs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Undo button
                    IconButton(
                        onClick = { onEditorStateChange(editorState.undo()) },
                        enabled = editorState.canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (editorState.canUndo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }

                    // Redo button
                    IconButton(
                        onClick = { onEditorStateChange(editorState.redo()) },
                        enabled = editorState.canRedo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (editorState.canRedo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }

                    // Export button
                    if (onExport != null) {
                        IconButton(onClick = { onExport(matrixState) }) {
                            Icon(Icons.Default.Save, contentDescription = "Export")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tool bar
            GlyphToolBar(
                currentTool = currentTool,
                onToolSelect = { currentTool = it },
                brightness = brightness,
                onBrightnessChange = { brightness = it },
                onClear = { updateState(matrixState.clear()) },
                onFill = { updateState(matrixState.fill(brightness.toInt())) },
                onInvert = { updateState(matrixState.invert()) }
            )

            // Canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                NothingGlyphMatrixCanvas(
                    state = matrixState.withBrightness(brightness.toInt()),
                    onLedToggle = { index ->
                        val newState = when (currentTool) {
                            GlyphDrawingTool.DRAW -> matrixState.setLed(index, true, brightness.toInt())
                            GlyphDrawingTool.ERASE -> matrixState.setLed(index, false)
                            GlyphDrawingTool.TOGGLE -> matrixState.toggleLed(index)
                        }
                        updateState(newState)
                    },
                    modifier = Modifier.fillMaxSize(),
                    config = GlyphMatrixCanvasConfig(
                        showGridOutline = false,
                        ledPadding = 0.08f
                    )
                )
            }

            // Status bar
            GlyphStatusBar(
                litCount = matrixState.litLedCount,
                totalCount = matrixState.totalLeds,
                brightness = brightness.toInt()
            )
        }
    }
}

/**
 * Tool bar for the Glyph Matrix editor.
 */
@Composable
private fun GlyphToolBar(
    currentTool: GlyphDrawingTool,
    onToolSelect: (GlyphDrawingTool) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onClear: () -> Unit,
    onFill: () -> Unit,
    onInvert: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drawing tools
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GlyphToolButton(
                        icon = Icons.Default.Brush,
                        contentDescription = "Draw",
                        isSelected = currentTool == GlyphDrawingTool.DRAW,
                        onClick = { onToolSelect(GlyphDrawingTool.DRAW) }
                    )

                    GlyphToolButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Erase",
                        isSelected = currentTool == GlyphDrawingTool.ERASE,
                        onClick = { onToolSelect(GlyphDrawingTool.ERASE) }
                    )

                    GlyphToolButton(
                        icon = Icons.Default.Lightbulb,
                        contentDescription = "Toggle",
                        isSelected = currentTool == GlyphDrawingTool.TOGGLE,
                        onClick = { onToolSelect(GlyphDrawingTool.TOGGLE) }
                    )
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GlyphToolButton(
                        icon = Icons.Default.FormatColorFill,
                        contentDescription = "Fill All",
                        isSelected = false,
                        onClick = onFill
                    )

                    GlyphToolButton(
                        icon = Icons.Default.LightbulbOutline,
                        contentDescription = "Invert",
                        isSelected = false,
                        onClick = onInvert
                    )

                    GlyphToolButton(
                        icon = Icons.Default.Clear,
                        contentDescription = "Clear All",
                        isSelected = false,
                        onClick = onClear
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brightness slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(80.dp)
                )

                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0f..255f,
                    modifier = Modifier.weight(1f)
                )

                // Brightness preview
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = brightness / 255f))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${brightness.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}

/**
 * Individual tool button.
 */
@Composable
private fun GlyphToolButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * Status bar showing LED statistics.
 */
@Composable
private fun GlyphStatusBar(
    litCount: Int,
    totalCount: Int,
    brightness: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEDs: $litCount / $totalCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Brightness: $brightness",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Nothing Phone (3)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
