/*
 * TimelineBar.kt
 *
 * Animation timeline control bar composable.
 *
 * This composable provides timeline controls for frame-by-frame
 * animation editing, including frame navigation, playback controls,
 * and frame management operations.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphmatrix.toyeditor.data.models.FrameData
import com.glyphmatrix.toyeditor.engine.logic.PlaybackState

/**
 * Timeline bar for animation frame management.
 *
 * @param frames List of animation frames
 * @param currentFrameIndex Currently selected frame index
 * @param playbackState Current playback state
 * @param onionSkinEnabled Whether onion skinning is enabled
 * @param onFrameSelect Called when a frame is selected
 * @param onAddFrame Called when add frame button is pressed
 * @param onDuplicateFrame Called when duplicate frame button is pressed
 * @param onDeleteFrame Called when delete frame button is pressed
 * @param onPlayPause Called when play/pause button is pressed
 * @param onStop Called when stop button is pressed
 * @param onPreviousFrame Called when previous frame button is pressed
 * @param onNextFrame Called when next frame button is pressed
 * @param onToggleOnionSkin Called when onion skin toggle is pressed
 * @param modifier Modifier for the timeline bar
 */
@Composable
fun TimelineBar(
    frames: List<FrameData>,
    currentFrameIndex: Int,
    playbackState: PlaybackState,
    onionSkinEnabled: Boolean,
    onFrameSelect: (Int) -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onPreviousFrame: () -> Unit,
    onNextFrame: () -> Unit,
    onToggleOnionSkin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to current frame
    LaunchedEffect(currentFrameIndex) {
        val targetPosition = currentFrameIndex * 72 // Approximate frame thumbnail width
        scrollState.animateScrollTo(targetPosition.coerceAtLeast(0))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Playback controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousFrame) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous Frame"
                        )
                    }

                    IconButton(onClick = onPlayPause) {
                        Icon(
                            if (playbackState == PlaybackState.PLAYING) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (playbackState == PlaybackState.PLAYING) {
                                "Pause"
                            } else {
                                "Play"
                            }
                        )
                    }

                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }

                    IconButton(onClick = onNextFrame) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Frame")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Frame counter
                    Text(
                        text = "${currentFrameIndex + 1} / ${frames.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Right side: Frame operations
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleOnionSkin
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Toggle Onion Skin",
                            tint = if (onionSkinEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    IconButton(onClick = onAddFrame) {
                        Icon(Icons.Default.Add, contentDescription = "Add Frame")
                    }

                    IconButton(onClick = onDuplicateFrame) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Frame")
                    }

                    IconButton(
                        onClick = onDeleteFrame,
                        enabled = frames.size > 1
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Frame",
                            tint = if (frames.size > 1) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Frame thumbnails row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                frames.forEachIndexed { index, frame ->
                    FrameThumbnail(
                        frame = frame,
                        index = index,
                        isSelected = index == currentFrameIndex,
                        onClick = { onFrameSelect(index) }
                    )
                }
            }
        }
    }
}

/**
 * Individual frame thumbnail in the timeline.
 */
@Composable
private fun FrameThumbnail(
    frame: FrameData,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Frame preview
        Box(
            modifier = Modifier
                .size(56.dp, 32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black)
        ) {
            MatrixPreview(
                frame = frame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Frame number
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        // Duration
        Text(
            text = "${frame.durationMs}ms",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
