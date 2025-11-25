/*
 * EditorScreen.kt
 *
 * Main editor screen composable for the Glyph Matrix Toy Editor.
 *
 * This composable serves as the primary editing interface, combining
 * the matrix canvas, widget panel, and timeline bar into a cohesive
 * pixel art animation editing experience.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glyphmatrix.toyeditor.data.models.AnimationProject
import com.glyphmatrix.toyeditor.data.models.WidgetType
import com.glyphmatrix.toyeditor.engine.logic.PlaybackState

/**
 * Editor state holder for managing undo/redo history.
 */
data class EditorState(
    val project: AnimationProject,
    val undoStack: List<AnimationProject> = emptyList(),
    val redoStack: List<AnimationProject> = emptyList()
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun pushState(newProject: AnimationProject): EditorState {
        return copy(
            project = newProject,
            undoStack = (undoStack + project).takeLast(50),
            redoStack = emptyList()
        )
    }

    fun undo(): EditorState {
        if (!canUndo) return this
        val previousState = undoStack.last()
        return copy(
            project = previousState,
            undoStack = undoStack.dropLast(1),
            redoStack = redoStack + project
        )
    }

    fun redo(): EditorState {
        if (!canRedo) return this
        val nextState = redoStack.last()
        return copy(
            project = nextState,
            undoStack = undoStack + project,
            redoStack = redoStack.dropLast(1)
        )
    }
}

/**
 * Main editor screen composable.
 *
 * @param editorState Current editor state
 * @param playbackState Current playback state
 * @param onEditorStateChange Called when editor state changes
 * @param onPlayPause Called when play/pause is triggered
 * @param onStop Called when stop is triggered
 * @param onNavigateToExport Called when export is requested
 * @param onNavigateToSettings Called when settings is requested
 * @param onNavigateToProjects Called when projects is requested
 * @param onSaveProject Called when save is requested
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    editorState: EditorState,
    playbackState: PlaybackState,
    onEditorStateChange: (EditorState) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onSaveProject: () -> Unit
) {
    var currentTool by remember { mutableStateOf(DrawingTool.DRAW) }
    var brushValue by remember { mutableFloatStateOf(255f) }
    var showWidgetPanel by remember { mutableStateOf(false) }
    var selectedWidget by remember { mutableStateOf<WidgetType?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var onionSkinEnabled by remember { mutableStateOf(false) }

    val project = editorState.project

    // Helper to update project with undo history
    fun updateProject(newProject: AnimationProject) {
        onEditorStateChange(editorState.pushState(newProject))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${project.matrixWidth}×${project.matrixHeight}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToProjects) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
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

                    IconButton(onClick = onSaveProject) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }

                    IconButton(onClick = onNavigateToExport) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Frame") },
                                onClick = {
                                    showMenu = false
                                    updateProject(project.clearCurrentFrame())
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Fill Frame") },
                                onClick = {
                                    showMenu = false
                                    val filledMatrix = project.currentFrame.matrix.fill(brushValue.toInt())
                                    updateProject(
                                        project.updateCurrentFrame(
                                            project.currentFrame.withMatrix(filledMatrix)
                                        )
                                    )
                                }
                            )
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
            ToolBar(
                currentTool = currentTool,
                onToolSelect = { currentTool = it },
                brushValue = brushValue,
                onBrushValueChange = { brushValue = it },
                onShowWidgets = { showWidgetPanel = !showWidgetPanel },
                widgetsActive = showWidgetPanel,
                onClearFrame = { updateProject(project.clearCurrentFrame()) }
            )

            // Canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val onionSkinFrames = if (onionSkinEnabled) {
                    project.getOnionSkinFrames()
                } else {
                    emptyList()
                }

                MatrixCanvas(
                    matrix = project.currentFrame.matrix,
                    modifier = Modifier.fillMaxSize(),
                    config = CanvasConfig(
                        onionSkinEnabled = onionSkinEnabled
                    ),
                    onionSkinFrames = onionSkinFrames,
                    currentTool = currentTool,
                    brushValue = brushValue.toInt(),
                    onPixelTap = { x, y, value ->
                        if (selectedWidget != null) {
                            // Place widget
                            // Widget placement logic would go here
                            selectedWidget = null
                        } else if (currentTool == DrawingTool.FILL) {
                            // Flood fill
                            val filled = floodFill(
                                project.currentFrame.matrix,
                                x, y,
                                value
                            )
                            updateProject(
                                project.updateCurrentFrame(
                                    project.currentFrame.withMatrix(filled)
                                )
                            )
                        } else {
                            updateProject(project.setPixel(x, y, value))
                        }
                    },
                    onPixelDrag = { x, y, value ->
                        if (selectedWidget == null && currentTool != DrawingTool.FILL) {
                            updateProject(project.setPixel(x, y, value))
                        }
                    },
                    onColorPicked = { color ->
                        brushValue = color.toFloat()
                        currentTool = DrawingTool.DRAW
                    }
                )
            }

            // Widget panel (animated visibility)
            AnimatedVisibility(
                visible = showWidgetPanel,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                WidgetPanel(
                    selectedWidget = selectedWidget,
                    onWidgetSelect = { selectedWidget = it },
                    onClose = { showWidgetPanel = false }
                )
            }

            // Timeline bar
            TimelineBar(
                frames = project.frames,
                currentFrameIndex = project.currentFrameIndex,
                playbackState = playbackState,
                onionSkinEnabled = onionSkinEnabled,
                onFrameSelect = { index ->
                    updateProject(project.goToFrame(index))
                },
                onAddFrame = {
                    updateProject(project.addFrame())
                },
                onDuplicateFrame = {
                    updateProject(project.duplicateCurrentFrame())
                },
                onDeleteFrame = {
                    updateProject(project.deleteCurrentFrame())
                },
                onPlayPause = onPlayPause,
                onStop = onStop,
                onPreviousFrame = {
                    updateProject(project.previousFrame())
                },
                onNextFrame = {
                    updateProject(project.nextFrame())
                },
                onToggleOnionSkin = {
                    onionSkinEnabled = !onionSkinEnabled
                }
            )
        }
    }
}

/**
 * Drawing tools toolbar.
 */
@Composable
private fun ToolBar(
    currentTool: DrawingTool,
    onToolSelect: (DrawingTool) -> Unit,
    brushValue: Float,
    onBrushValueChange: (Float) -> Unit,
    onShowWidgets: () -> Unit,
    widgetsActive: Boolean,
    onClearFrame: () -> Unit
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
                    ToolButton(
                        icon = Icons.Default.Brush,
                        contentDescription = "Draw",
                        isSelected = currentTool == DrawingTool.DRAW,
                        onClick = { onToolSelect(DrawingTool.DRAW) }
                    )

                    ToolButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Erase",
                        isSelected = currentTool == DrawingTool.ERASE,
                        onClick = { onToolSelect(DrawingTool.ERASE) }
                    )

                    ToolButton(
                        icon = Icons.Default.FormatColorFill,
                        contentDescription = "Fill",
                        isSelected = currentTool == DrawingTool.FILL,
                        onClick = { onToolSelect(DrawingTool.FILL) }
                    )

                    ToolButton(
                        icon = Icons.Default.Colorize,
                        contentDescription = "Color Picker",
                        isSelected = currentTool == DrawingTool.PICKER,
                        onClick = { onToolSelect(DrawingTool.PICKER) }
                    )
                }

                // Widget and clear buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ToolButton(
                        icon = Icons.Default.Widgets,
                        contentDescription = "Widgets",
                        isSelected = widgetsActive,
                        onClick = onShowWidgets
                    )

                    ToolButton(
                        icon = Icons.Default.Clear,
                        contentDescription = "Clear",
                        isSelected = false,
                        onClick = onClearFrame
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
                    value = brushValue,
                    onValueChange = onBrushValueChange,
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
                            .background(Color.White.copy(alpha = brushValue / 255f))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${brushValue.toInt()}",
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
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
 * Flood fill algorithm for the fill tool.
 */
private fun floodFill(
    matrix: com.glyphmatrix.toyeditor.data.models.MatrixModel,
    startX: Int,
    startY: Int,
    newValue: Int
): com.glyphmatrix.toyeditor.data.models.MatrixModel {
    val targetValue = matrix.getPixel(startX, startY)
    if (targetValue == newValue) return matrix

    var result = matrix
    val visited = mutableSetOf<Pair<Int, Int>>()
    val queue = ArrayDeque<Pair<Int, Int>>()
    queue.add(Pair(startX, startY))

    while (queue.isNotEmpty()) {
        val (x, y) = queue.removeFirst()

        if (!result.isValidCoordinate(x, y)) continue
        if (Pair(x, y) in visited) continue
        if (result.getPixel(x, y) != targetValue) continue

        visited.add(Pair(x, y))
        result = result.setPixel(x, y, newValue)

        queue.add(Pair(x + 1, y))
        queue.add(Pair(x - 1, y))
        queue.add(Pair(x, y + 1))
        queue.add(Pair(x, y - 1))
    }

    return result
}
