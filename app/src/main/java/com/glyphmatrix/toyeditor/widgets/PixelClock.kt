/*
 * PixelClock.kt
 *
 * Pixel clock widget implementation.
 *
 * This widget provides a customizable clock display that can be
 * placed on the matrix canvas, showing time in pixel art style
 * suitable for glyph matrix displays.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.widgets

import com.glyphmatrix.toyeditor.data.models.MatrixModel
import java.util.Calendar

/**
 * Time format for the clock display.
 */
enum class ClockFormat {
    /** 12-hour format (e.g., 12:30) */
    FORMAT_12H,
    /** 24-hour format (e.g., 23:30) */
    FORMAT_24H
}

/**
 * Pixel clock widget for displaying time in pixel art style.
 *
 * Each digit is 3x5 pixels, with 1 pixel spacing between digits.
 * The colon is 1 pixel wide.
 *
 * Total width for HH:MM = 3+1+3+1+3+1+3 = 15 pixels
 * With seconds HH:MM:SS = 15+1+3+1+3 = 23 pixels
 */
class PixelClock(
    private val format: ClockFormat = ClockFormat.FORMAT_24H,
    private val showSeconds: Boolean = false,
    private val brightness: Int = MatrixModel.MAX_BRIGHTNESS
) {
    companion object {
        /** Width of a single digit in pixels */
        const val DIGIT_WIDTH = 3

        /** Height of a digit in pixels */
        const val DIGIT_HEIGHT = 5

        /** Spacing between elements */
        const val SPACING = 1

        /** Width of the colon */
        const val COLON_WIDTH = 1

        /**
         * 3x5 pixel font for digits 0-9.
         * Each digit is represented as an array of 5 rows,
         * where each row is an array of 3 brightness values.
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

        /** Colon pattern (1x5) - dots at rows 1 and 3 */
        private val COLON_PATTERN = intArrayOf(0, 1, 0, 1, 0)
    }

    /** Width of the clock widget */
    val width: Int
        get() = if (showSeconds) {
            // HH:MM:SS = 6 digits + 2 colons + spacing
            6 * DIGIT_WIDTH + 5 * SPACING + 2 * COLON_WIDTH
        } else {
            // HH:MM = 4 digits + 1 colon + spacing
            4 * DIGIT_WIDTH + 3 * SPACING + COLON_WIDTH
        }

    /** Height of the clock widget */
    val height: Int = DIGIT_HEIGHT

    /**
     * Renders the current time to a pixel array.
     *
     * @param blinkColon Whether to blink the colon (for animation)
     * @return 2D array of brightness values
     */
    fun render(blinkColon: Boolean = true): Array<IntArray> {
        val calendar = Calendar.getInstance()
        var hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        // Convert to 12-hour format if needed
        if (format == ClockFormat.FORMAT_12H) {
            hours = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
        }

        val showColonDots = !blinkColon || (seconds % 2 == 0)

        return if (showSeconds) {
            renderTimeWithSeconds(hours, minutes, seconds, showColonDots)
        } else {
            renderTime(hours, minutes, showColonDots)
        }
    }

    /**
     * Renders time as HH:MM.
     */
    private fun renderTime(hours: Int, minutes: Int, showColon: Boolean): Array<IntArray> {
        val result = Array(height) { IntArray(width) { 0 } }

        var xOffset = 0

        // Hours tens digit
        xOffset = renderDigit(result, hours / 10, xOffset)
        xOffset += SPACING

        // Hours ones digit
        xOffset = renderDigit(result, hours % 10, xOffset)
        xOffset += SPACING

        // Colon
        xOffset = renderColon(result, xOffset, showColon)
        xOffset += SPACING

        // Minutes tens digit
        xOffset = renderDigit(result, minutes / 10, xOffset)
        xOffset += SPACING

        // Minutes ones digit
        renderDigit(result, minutes % 10, xOffset)

        return result
    }

    /**
     * Renders time as HH:MM:SS.
     */
    private fun renderTimeWithSeconds(
        hours: Int,
        minutes: Int,
        seconds: Int,
        showColon: Boolean
    ): Array<IntArray> {
        val result = Array(height) { IntArray(width) { 0 } }

        var xOffset = 0

        // Hours
        xOffset = renderDigit(result, hours / 10, xOffset)
        xOffset += SPACING
        xOffset = renderDigit(result, hours % 10, xOffset)
        xOffset += SPACING

        // First colon
        xOffset = renderColon(result, xOffset, showColon)
        xOffset += SPACING

        // Minutes
        xOffset = renderDigit(result, minutes / 10, xOffset)
        xOffset += SPACING
        xOffset = renderDigit(result, minutes % 10, xOffset)
        xOffset += SPACING

        // Second colon
        xOffset = renderColon(result, xOffset, showColon)
        xOffset += SPACING

        // Seconds
        xOffset = renderDigit(result, seconds / 10, xOffset)
        xOffset += SPACING
        renderDigit(result, seconds % 10, xOffset)

        return result
    }

    /**
     * Renders a single digit at the specified x offset.
     *
     * @return New x offset after the digit
     */
    private fun renderDigit(result: Array<IntArray>, digit: Int, xOffset: Int): Int {
        val pattern = DIGIT_FONT[digit.coerceIn(0, 9)]

        for (y in 0 until DIGIT_HEIGHT) {
            for (x in 0 until DIGIT_WIDTH) {
                if (pattern[y][x] == 1) {
                    val targetX = xOffset + x
                    if (targetX < result[0].size) {
                        result[y][targetX] = brightness
                    }
                }
            }
        }

        return xOffset + DIGIT_WIDTH
    }

    /**
     * Renders a colon at the specified x offset.
     *
     * @return New x offset after the colon
     */
    private fun renderColon(result: Array<IntArray>, xOffset: Int, visible: Boolean): Int {
        if (visible && xOffset < result[0].size) {
            for (y in 0 until DIGIT_HEIGHT) {
                if (COLON_PATTERN[y] == 1) {
                    result[y][xOffset] = brightness
                }
            }
        }
        return xOffset + COLON_WIDTH
    }

    /**
     * Applies the clock widget to a matrix model at the specified position.
     *
     * @param matrix The matrix to apply to
     * @param x X position
     * @param y Y position
     * @param blinkColon Whether to blink the colon
     * @return New MatrixModel with the clock applied
     */
    fun applyToMatrix(
        matrix: MatrixModel,
        x: Int,
        y: Int,
        blinkColon: Boolean = true
    ): MatrixModel {
        val clockPixels = render(blinkColon)
        var result = matrix

        for (cy in clockPixels.indices) {
            for (cx in clockPixels[cy].indices) {
                val targetX = x + cx
                val targetY = y + cy
                if (clockPixels[cy][cx] > 0) {
                    result = result.setPixel(targetX, targetY, clockPixels[cy][cx])
                }
            }
        }

        return result
    }
}
