/*
 * NothingGlyphMatrixCanvas.kt
 *
 * Jetpack Compose canvas for rendering and interacting with the
 * Nothing Phone 3 Glyph Matrix LED grid.
 *
 * This composable provides:
 * - Pixel-perfect rendering of all 489 LEDs matching the official layout
 * - Tappable and toggleable LED entities
 * - Responsive resizing without distortion
 * - Nothing spec colors: #FFFFFF (on), #1C1C1C (off), #000 (background)
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.nothingphone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Nothing spec colors for the Glyph Matrix.
 */
object GlyphMatrixColors {
    /** LED on color - #FFFFFF (white) */
    val LED_ON = Color.White

    /** LED off color - #1C1C1C (dark gray) */
    val LED_OFF = Color(0xFF1C1C1C)

    /** Background color - #000000 (black) */
    val BACKGROUND = Color.Black

    /** Grid outline color for visual guidance (optional) */
    val GRID_OUTLINE = Color(0xFF333333)
}

/**
 * Configuration for the Glyph Matrix canvas.
 *
 * @property showGridOutline Whether to show grid cell outlines
 * @property ledCornerRadius Corner radius for LED squares (0 for sharp corners)
 * @property ledPadding Padding between LED and cell boundary
 */
data class GlyphMatrixCanvasConfig(
    val showGridOutline: Boolean = false,
    val ledCornerRadius: Float = 0f,
    val ledPadding: Float = 0.05f // Percentage of cell size
)

/**
 * Jetpack Compose canvas for rendering the Nothing Phone 3 Glyph Matrix.
 *
 * This composable renders all 489 LEDs in the elliptical Glyph Matrix layout,
 * matching the official Nothing Phone 3 LED allocation. Each LED is tappable
 * and can be toggled on/off.
 *
 * @param state Current state of the Glyph Matrix
 * @param onLedToggle Callback when an LED is tapped (receives LED index)
 * @param modifier Modifier for the canvas
 * @param config Optional configuration for rendering
 */
@Composable
fun NothingGlyphMatrixCanvas(
    state: GlyphMatrixState,
    onLedToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    config: GlyphMatrixCanvasConfig = GlyphMatrixCanvasConfig()
) {
    // Calculate aspect ratio from SVG viewBox
    val aspectRatio = NothingPhone3MatrixData.GRID_AREA_WIDTH /
            NothingPhone3MatrixData.GRID_AREA_HEIGHT

    // Remember LED positions for tap detection
    val ledPositions = remember { NothingPhone3MatrixData.LED_POSITIONS }

    Box(
        modifier = modifier
            .background(GlyphMatrixColors.BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .fillMaxSize()
                .pointerInput(ledPositions) {
                    detectTapGestures { offset ->
                        // Convert tap position to LED index
                        val ledIndex = findTappedLed(
                            tapOffset = offset,
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat(),
                            ledPositions = ledPositions
                        )
                        if (ledIndex >= 0) {
                            onLedToggle(ledIndex)
                        }
                    }
                }
        ) {
            drawGlyphMatrix(
                state = state,
                ledPositions = ledPositions,
                config = config
            )
        }
    }
}

/**
 * Draws the complete Glyph Matrix on the canvas.
 */
private fun DrawScope.drawGlyphMatrix(
    state: GlyphMatrixState,
    ledPositions: List<LedPosition>,
    config: GlyphMatrixCanvasConfig
) {
    // Calculate scale factors to fit the grid in the canvas
    val scaleX = size.width / NothingPhone3MatrixData.GRID_AREA_WIDTH
    val scaleY = size.height / NothingPhone3MatrixData.GRID_AREA_HEIGHT

    // Use uniform scaling to maintain aspect ratio
    val scale = minOf(scaleX, scaleY)

    // Calculate offset to center the grid
    val scaledWidth = NothingPhone3MatrixData.GRID_AREA_WIDTH * scale
    val scaledHeight = NothingPhone3MatrixData.GRID_AREA_HEIGHT * scale
    val offsetX = (size.width - scaledWidth) / 2
    val offsetY = (size.height - scaledHeight) / 2

    // Draw each LED
    for (led in ledPositions) {
        // Calculate LED position relative to grid area
        val relativeX = led.x - NothingPhone3MatrixData.GRID_OFFSET_X
        val relativeY = led.y - NothingPhone3MatrixData.GRID_OFFSET_Y

        // Scale and offset
        val x = offsetX + relativeX * scale
        val y = offsetY + relativeY * scale
        val ledWidth = led.width * scale
        val ledHeight = led.height * scale

        // Apply padding
        val padding = ledWidth * config.ledPadding
        val paddedX = x + padding
        val paddedY = y + padding
        val paddedWidth = ledWidth - 2 * padding
        val paddedHeight = ledHeight - 2 * padding

        // Determine LED color based on state
        val ledState = state.getLedState(led.index)
        val color = if (ledState.isOn) {
            // Apply brightness to the on color
            val alpha = ledState.brightness / 255f
            GlyphMatrixColors.LED_ON.copy(alpha = alpha)
        } else {
            GlyphMatrixColors.LED_OFF
        }

        // Draw LED
        drawRect(
            color = color,
            topLeft = Offset(paddedX, paddedY),
            size = Size(paddedWidth, paddedHeight)
        )

        // Optional: Draw grid outline
        if (config.showGridOutline) {
            drawRect(
                color = GlyphMatrixColors.GRID_OUTLINE,
                topLeft = Offset(x, y),
                size = Size(ledWidth, ledHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}

/**
 * Finds the LED at the given tap position.
 *
 * @param tapOffset Tap position in canvas coordinates
 * @param canvasWidth Canvas width
 * @param canvasHeight Canvas height
 * @param ledPositions List of LED positions
 * @return LED index if found, -1 otherwise
 */
private fun findTappedLed(
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    ledPositions: List<LedPosition>
): Int {
    // Calculate scale factors
    val scaleX = canvasWidth / NothingPhone3MatrixData.GRID_AREA_WIDTH
    val scaleY = canvasHeight / NothingPhone3MatrixData.GRID_AREA_HEIGHT
    val scale = minOf(scaleX, scaleY)

    // Calculate offset
    val scaledWidth = NothingPhone3MatrixData.GRID_AREA_WIDTH * scale
    val scaledHeight = NothingPhone3MatrixData.GRID_AREA_HEIGHT * scale
    val offsetX = (canvasWidth - scaledWidth) / 2
    val offsetY = (canvasHeight - scaledHeight) / 2

    // Convert tap to grid coordinates
    val gridX = (tapOffset.x - offsetX) / scale + NothingPhone3MatrixData.GRID_OFFSET_X
    val gridY = (tapOffset.y - offsetY) / scale + NothingPhone3MatrixData.GRID_OFFSET_Y

    // Find the LED at this position
    for (led in ledPositions) {
        if (gridX >= led.x && gridX <= led.x + led.width &&
            gridY >= led.y && gridY <= led.y + led.height
        ) {
            return led.index
        }
    }

    return -1
}

/**
 * Simplified Glyph Matrix canvas for preview purposes.
 *
 * This version doesn't handle taps and is optimized for displaying
 * the matrix state in read-only mode (e.g., in timeline thumbnails).
 *
 * @param state Current state of the Glyph Matrix
 * @param modifier Modifier for the canvas
 */
@Composable
fun NothingGlyphMatrixPreview(
    state: GlyphMatrixState,
    modifier: Modifier = Modifier
) {
    val aspectRatio = NothingPhone3MatrixData.GRID_AREA_WIDTH /
            NothingPhone3MatrixData.GRID_AREA_HEIGHT
    val ledPositions = remember { NothingPhone3MatrixData.LED_POSITIONS }

    Box(
        modifier = modifier.background(GlyphMatrixColors.BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .fillMaxSize()
        ) {
            drawGlyphMatrix(
                state = state,
                ledPositions = ledPositions,
                config = GlyphMatrixCanvasConfig()
            )
        }
    }
}
