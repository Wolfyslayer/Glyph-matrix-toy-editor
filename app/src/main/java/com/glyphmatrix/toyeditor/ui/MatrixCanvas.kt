/*
 * MatrixCanvas.kt
 *
 * Pixel art drawing canvas composable for matrix display editing.
 *
 * This composable provides the main drawing surface where users
 * create and edit pixel art for glyph matrix displays. It handles
 * touch input, rendering, and visual feedback.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.glyphmatrix.toyeditor.data.models.FrameData
import com.glyphmatrix.toyeditor.data.models.MatrixModel
import kotlin.math.floor

/**
 * Drawing tool modes.
 */
enum class DrawingTool {
    /** Draw pixels with the current brush */
    DRAW,
    /** Erase pixels */
    ERASE,
    /** Fill connected area */
    FILL,
    /** Pick color from pixel */
    PICKER
}

/**
 * Configuration for the matrix canvas.
 */
data class CanvasConfig(
    val showGrid: Boolean = true,
    val gridColor: Color = Color(0xFF333333),
    val backgroundColor: Color = Color.Black,
    val pixelOnColor: Color = Color.White,
    val onionSkinColor: Color = Color(0x40FF0000),
    val onionSkinEnabled: Boolean = false,
    val pixelSpacing: Float = 1f
)

/**
 * Interactive pixel art canvas for matrix editing.
 *
 * @param matrix Current matrix data to display
 * @param modifier Modifier for the canvas
 * @param config Canvas configuration
 * @param onionSkinFrames Frames to display as onion skin reference
 * @param currentTool Current drawing tool
 * @param brushValue Current brush brightness value (0-255)
 * @param onPixelTap Called when a pixel is tapped
 * @param onPixelDrag Called when dragging across pixels
 * @param onColorPicked Called when color picker selects a pixel
 */
@Composable
fun MatrixCanvas(
    matrix: MatrixModel,
    modifier: Modifier = Modifier,
    config: CanvasConfig = CanvasConfig(),
    onionSkinFrames: List<Pair<Int, FrameData>> = emptyList(),
    currentTool: DrawingTool = DrawingTool.DRAW,
    brushValue: Int = MatrixModel.MAX_BRIGHTNESS,
    onPixelTap: (x: Int, y: Int, value: Int) -> Unit = { _, _, _ -> },
    onPixelDrag: (x: Int, y: Int, value: Int) -> Unit = { _, _, _ -> },
    onColorPicked: (Int) -> Unit = {}
) {
    val aspectRatio = matrix.width.toFloat() / matrix.height.toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(config.backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(matrix, currentTool, brushValue) {
                    detectTapGestures { offset ->
                        val (x, y) = calculatePixelCoordinates(
                            offset,
                            size.width.toFloat(),
                            size.height.toFloat(),
                            matrix.width,
                            matrix.height,
                            config.pixelSpacing
                        )

                        if (matrix.isValidCoordinate(x, y)) {
                            when (currentTool) {
                                DrawingTool.DRAW -> onPixelTap(x, y, brushValue)
                                DrawingTool.ERASE -> onPixelTap(x, y, 0)
                                DrawingTool.FILL -> onPixelTap(x, y, brushValue)
                                DrawingTool.PICKER -> {
                                    val color = matrix.getPixel(x, y)
                                    onColorPicked(color)
                                }
                            }
                        }
                    }
                }
                .pointerInput(matrix, currentTool, brushValue) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val (x, y) = calculatePixelCoordinates(
                            change.position,
                            size.width.toFloat(),
                            size.height.toFloat(),
                            matrix.width,
                            matrix.height,
                            config.pixelSpacing
                        )

                        if (matrix.isValidCoordinate(x, y)) {
                            when (currentTool) {
                                DrawingTool.DRAW -> onPixelDrag(x, y, brushValue)
                                DrawingTool.ERASE -> onPixelDrag(x, y, 0)
                                DrawingTool.FILL -> { /* Fill doesn't support drag */ }
                                DrawingTool.PICKER -> {
                                    val color = matrix.getPixel(x, y)
                                    onColorPicked(color)
                                }
                            }
                        }
                    }
                }
        ) {
            val pixelWidth = (size.width - config.pixelSpacing * (matrix.width - 1)) / matrix.width
            val pixelHeight = (size.height - config.pixelSpacing * (matrix.height - 1)) / matrix.height
            val pixelSize = minOf(pixelWidth, pixelHeight)

            // Center the grid
            val totalWidth = pixelSize * matrix.width + config.pixelSpacing * (matrix.width - 1)
            val totalHeight = pixelSize * matrix.height + config.pixelSpacing * (matrix.height - 1)
            val offsetX = (size.width - totalWidth) / 2
            val offsetY = (size.height - totalHeight) / 2

            // Draw onion skin frames first (if enabled)
            if (config.onionSkinEnabled) {
                for ((offset, frame) in onionSkinFrames) {
                    drawOnionSkinFrame(
                        frame.matrix,
                        pixelSize,
                        config.pixelSpacing,
                        offsetX,
                        offsetY,
                        config.onionSkinColor.copy(alpha = 0.3f / kotlin.math.abs(offset))
                    )
                }
            }

            // Draw grid background if enabled
            if (config.showGrid) {
                drawGrid(
                    matrix.width,
                    matrix.height,
                    pixelSize,
                    config.pixelSpacing,
                    offsetX,
                    offsetY,
                    config.gridColor
                )
            }

            // Draw current frame pixels
            drawPixels(
                matrix,
                pixelSize,
                config.pixelSpacing,
                offsetX,
                offsetY,
                config.pixelOnColor
            )
        }
    }
}

/**
 * Calculates which pixel coordinate corresponds to a touch position.
 */
private fun calculatePixelCoordinates(
    offset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    matrixWidth: Int,
    matrixHeight: Int,
    pixelSpacing: Float
): Pair<Int, Int> {
    val pixelWidth = (canvasWidth - pixelSpacing * (matrixWidth - 1)) / matrixWidth
    val pixelHeight = (canvasHeight - pixelSpacing * (matrixHeight - 1)) / matrixHeight
    val pixelSize = minOf(pixelWidth, pixelHeight)

    val totalWidth = pixelSize * matrixWidth + pixelSpacing * (matrixWidth - 1)
    val totalHeight = pixelSize * matrixHeight + pixelSpacing * (matrixHeight - 1)
    val offsetX = (canvasWidth - totalWidth) / 2
    val offsetY = (canvasHeight - totalHeight) / 2

    val adjustedX = offset.x - offsetX
    val adjustedY = offset.y - offsetY

    val cellSize = pixelSize + pixelSpacing
    val x = floor(adjustedX / cellSize).toInt()
    val y = floor(adjustedY / cellSize).toInt()

    return Pair(x, y)
}

/**
 * Draws the pixel grid background.
 */
private fun DrawScope.drawGrid(
    width: Int,
    height: Int,
    pixelSize: Float,
    spacing: Float,
    offsetX: Float,
    offsetY: Float,
    gridColor: Color
) {
    val cellSize = pixelSize + spacing

    for (y in 0 until height) {
        for (x in 0 until width) {
            val px = offsetX + x * cellSize
            val py = offsetY + y * cellSize

            drawRect(
                color = gridColor,
                topLeft = Offset(px, py),
                size = Size(pixelSize, pixelSize)
            )
        }
    }
}

/**
 * Draws the pixels from a matrix.
 */
private fun DrawScope.drawPixels(
    matrix: MatrixModel,
    pixelSize: Float,
    spacing: Float,
    offsetX: Float,
    offsetY: Float,
    pixelColor: Color
) {
    val cellSize = pixelSize + spacing

    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            val brightness = matrix.getPixel(x, y)
            if (brightness > 0) {
                val px = offsetX + x * cellSize
                val py = offsetY + y * cellSize
                val alpha = brightness.toFloat() / MatrixModel.MAX_BRIGHTNESS

                drawRect(
                    color = pixelColor.copy(alpha = alpha),
                    topLeft = Offset(px, py),
                    size = Size(pixelSize, pixelSize)
                )
            }
        }
    }
}

/**
 * Draws an onion skin frame.
 */
private fun DrawScope.drawOnionSkinFrame(
    matrix: MatrixModel,
    pixelSize: Float,
    spacing: Float,
    offsetX: Float,
    offsetY: Float,
    color: Color
) {
    val cellSize = pixelSize + spacing

    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            val brightness = matrix.getPixel(x, y)
            if (brightness > 0) {
                val px = offsetX + x * cellSize
                val py = offsetY + y * cellSize

                drawRect(
                    color = color,
                    topLeft = Offset(px, py),
                    size = Size(pixelSize, pixelSize)
                )
            }
        }
    }
}

/**
 * Preview canvas that shows a non-interactive view of a frame.
 *
 * @param frame Frame data to display
 * @param modifier Modifier for the canvas
 */
@Composable
fun MatrixPreview(
    frame: FrameData,
    modifier: Modifier = Modifier
) {
    val matrix = frame.matrix
    val aspectRatio = matrix.width.toFloat() / matrix.height.toFloat()

    Canvas(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(Color.Black)
    ) {
        val pixelWidth = size.width / matrix.width
        val pixelHeight = size.height / matrix.height
        val pixelSize = minOf(pixelWidth, pixelHeight)

        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                val brightness = matrix.getPixel(x, y)
                if (brightness > 0) {
                    val px = x * pixelSize
                    val py = y * pixelSize
                    val alpha = brightness.toFloat() / MatrixModel.MAX_BRIGHTNESS

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
