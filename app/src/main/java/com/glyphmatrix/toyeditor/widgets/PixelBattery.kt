/*
 * PixelBattery.kt
 *
 * Pixel battery indicator widget implementation.
 *
 * This widget provides a customizable battery level indicator
 * that can be placed on the matrix canvas, displaying battery status
 * in pixel art style suitable for glyph matrix displays.
 *
 * Features:
 * - Battery icon with level indicator
 * - Percentage display option
 * - Charging animation with pulse effect
 * - Low battery warning animation
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.widgets

import com.glyphmatrix.toyeditor.data.models.MatrixModel
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Battery display style options.
 */
enum class BatteryStyle {
    /** Icon only */
    ICON_ONLY,
    /** Icon with percentage text */
    ICON_WITH_PERCENT
}

/**
 * Pixel battery widget for displaying battery status in pixel art style.
 *
 * Battery icon dimensions: 7x5 pixels
 * With percentage: 13x5 pixels (icon + space + 2-3 digit number)
 */
class PixelBattery(
    private val style: BatteryStyle = BatteryStyle.ICON_ONLY,
    private val brightness: Int = MatrixModel.MAX_BRIGHTNESS
) {
    companion object {
        /** Width of the battery icon */
        const val ICON_WIDTH = 7

        /** Height of the battery icon */
        const val ICON_HEIGHT = 5

        /** Width for percentage display */
        const val PERCENT_WIDTH = 13

        /** Number of fill segments in the battery */
        const val FILL_SEGMENTS = 4

        /** Animation frame count for charging animation */
        const val CHARGING_FRAMES = 8

        /** Pulse animation frequency (higher = faster) */
        const val PULSE_FREQUENCY = 0.5f

        /**
         * 3x5 pixel font for digits (shared with PixelClock).
         */
        private val DIGIT_FONT: Array<Array<IntArray>> = arrayOf(
            // 0
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 1, 1)
            ),
            // 1
            arrayOf(
                intArrayOf(0, 1, 0),
                intArrayOf(1, 1, 0),
                intArrayOf(0, 1, 0),
                intArrayOf(0, 1, 0),
                intArrayOf(1, 1, 1)
            ),
            // 2
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 0),
                intArrayOf(1, 1, 1)
            ),
            // 3
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(0, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(1, 1, 1)
            ),
            // 4
            arrayOf(
                intArrayOf(1, 0, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(0, 0, 1)
            ),
            // 5
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 0),
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(1, 1, 1)
            ),
            // 6
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 0),
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 1, 1)
            ),
            // 7
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(0, 0, 1)
            ),
            // 8
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 1, 1)
            ),
            // 9
            arrayOf(
                intArrayOf(1, 1, 1),
                intArrayOf(1, 0, 1),
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 1),
                intArrayOf(1, 1, 1)
            )
        )

        /**
         * Battery outline pattern (7x5).
         * 1 = outline pixel, 0 = empty, 2 = fill area
         */
        private val BATTERY_OUTLINE = arrayOf(
            intArrayOf(0, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 1, 2, 2, 2, 2, 1),
            intArrayOf(1, 1, 2, 2, 2, 2, 1),
            intArrayOf(1, 1, 2, 2, 2, 2, 1),
            intArrayOf(0, 1, 1, 1, 1, 1, 1)
        )

        /**
         * Charging bolt pattern (3x5) - lightning bolt shape.
         */
        private val CHARGING_BOLT = arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 0),
            intArrayOf(1, 0, 0)
        )
    }

    /** Width of the widget */
    val width: Int
        get() = when (style) {
            BatteryStyle.ICON_ONLY -> ICON_WIDTH
            BatteryStyle.ICON_WITH_PERCENT -> PERCENT_WIDTH
        }

    /** Height of the widget */
    val height: Int = ICON_HEIGHT

    /**
     * Renders the battery widget.
     *
     * @param level Battery level (0-100)
     * @param isCharging Whether the battery is charging
     * @param animationFrame Current animation frame (for charging animation)
     * @return 2D array of brightness values
     */
    fun render(level: Int, isCharging: Boolean = false, animationFrame: Int = 0): Array<IntArray> {
        val result = Array(height) { IntArray(width) { 0 } }

        // Render battery icon
        renderBatteryIcon(result, level, isCharging, animationFrame)

        // Render percentage if needed
        if (style == BatteryStyle.ICON_WITH_PERCENT) {
            renderPercentage(result, level, ICON_WIDTH + 1)
        }

        return result
    }

    /**
     * Renders the battery icon portion.
     */
    private fun renderBatteryIcon(
        result: Array<IntArray>,
        level: Int,
        isCharging: Boolean,
        animationFrame: Int
    ) {
        val safeLevel = level.coerceIn(0, 100)
        val fillSegments = (safeLevel / 25).coerceIn(0, FILL_SEGMENTS)

        // Calculate pulse brightness for charging animation
        val pulseBrightness = if (isCharging) {
            val pulsePhase = (animationFrame * PULSE_FREQUENCY) % (2 * Math.PI)
            val pulseValue = (sin(pulsePhase) + 1) / 2
            (brightness * 0.5 + brightness * 0.5 * pulseValue).roundToInt()
        } else {
            brightness
        }

        // Render battery outline
        for (y in 0 until ICON_HEIGHT) {
            for (x in 0 until ICON_WIDTH) {
                when (BATTERY_OUTLINE[y][x]) {
                    1 -> result[y][x] = brightness // Outline
                    2 -> {
                        // Fill area - check if this segment should be lit
                        val segmentIndex = x - 2 // Fill starts at x=2
                        if (segmentIndex < fillSegments) {
                            result[y][x] = if (isCharging) pulseBrightness else brightness
                        }
                    }
                }
            }
        }

        // Overlay charging bolt if charging
        if (isCharging) {
            renderChargingBolt(result, animationFrame)
        }
    }

    /**
     * Renders the charging bolt animation.
     */
    private fun renderChargingBolt(result: Array<IntArray>, animationFrame: Int) {
        // Calculate blinking effect
        val showBolt = (animationFrame / 2) % 2 == 0

        if (showBolt) {
            // Position bolt in center of battery
            val boltX = 2
            val boltY = 0

            for (y in CHARGING_BOLT.indices) {
                for (x in CHARGING_BOLT[y].indices) {
                    if (CHARGING_BOLT[y][x] == 1) {
                        val targetX = boltX + x
                        val targetY = boltY + y
                        if (targetX < ICON_WIDTH && targetY < ICON_HEIGHT) {
                            // Invert the pixel for visibility
                            result[targetY][targetX] = if (result[targetY][targetX] > 0) 0 else brightness
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders the percentage digits.
     */
    private fun renderPercentage(result: Array<IntArray>, level: Int, xOffset: Int) {
        val safeLevel = level.coerceIn(0, 100)
        var x = xOffset

        if (safeLevel == 100) {
            // Render "1" "0" "0"
            x = renderDigit(result, 1, x)
            x += 1
            x = renderDigit(result, 0, x)
            x += 1
            renderDigit(result, 0, x)
        } else if (safeLevel >= 10) {
            // Render two digits
            x = renderDigit(result, safeLevel / 10, x)
            x += 1
            renderDigit(result, safeLevel % 10, x)
        } else {
            // Render single digit
            renderDigit(result, safeLevel, x)
        }
    }

    /**
     * Renders a single digit.
     *
     * @return New x offset after the digit
     */
    private fun renderDigit(result: Array<IntArray>, digit: Int, xOffset: Int): Int {
        val pattern = DIGIT_FONT[digit.coerceIn(0, 9)]

        for (y in 0 until ICON_HEIGHT) {
            for (x in 0 until 3) { // Digit width is 3
                if (pattern[y][x] == 1) {
                    val targetX = xOffset + x
                    if (targetX < result[0].size) {
                        result[y][targetX] = brightness
                    }
                }
            }
        }

        return xOffset + 3
    }

    /**
     * Renders a low battery warning animation.
     *
     * @param animationFrame Current animation frame
     * @return 2D array of brightness values
     */
    fun renderLowBatteryWarning(animationFrame: Int): Array<IntArray> {
        val result = Array(height) { IntArray(width) { 0 } }

        // Flash effect for low battery
        val flashOn = (animationFrame / 4) % 2 == 0
        val effectiveBrightness = if (flashOn) brightness else brightness / 3

        // Render battery outline only (empty)
        for (y in 0 until ICON_HEIGHT) {
            for (x in 0 until ICON_WIDTH) {
                if (BATTERY_OUTLINE[y][x] == 1) {
                    result[y][x] = effectiveBrightness
                }
            }
        }

        // Add exclamation mark in center when flashing
        if (flashOn) {
            // Exclamation mark at center
            result[1][3] = brightness
            result[2][3] = brightness
            result[4][3] = brightness
        }

        return result
    }

    /**
     * Applies the battery widget to a matrix model at the specified position.
     *
     * @param matrix The matrix to apply to
     * @param x X position
     * @param y Y position
     * @param level Battery level (0-100)
     * @param isCharging Whether charging
     * @param animationFrame Animation frame for charging effect
     * @return New MatrixModel with the battery applied
     */
    fun applyToMatrix(
        matrix: MatrixModel,
        x: Int,
        y: Int,
        level: Int,
        isCharging: Boolean = false,
        animationFrame: Int = 0
    ): MatrixModel {
        val pixels = if (level <= 10 && !isCharging) {
            renderLowBatteryWarning(animationFrame)
        } else {
            render(level, isCharging, animationFrame)
        }

        var result = matrix

        for (cy in pixels.indices) {
            for (cx in pixels[cy].indices) {
                val targetX = x + cx
                val targetY = y + cy
                if (pixels[cy][cx] > 0) {
                    result = result.setPixel(targetX, targetY, pixels[cy][cx])
                }
            }
        }

        return result
    }
}
