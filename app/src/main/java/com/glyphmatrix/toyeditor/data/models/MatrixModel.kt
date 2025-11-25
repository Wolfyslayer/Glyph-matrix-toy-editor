/*
 * MatrixModel.kt
 *
 * Data model representing matrix display configuration and state.
 *
 * This data class defines the properties and configuration of the
 * glyph matrix display, including dimensions, pixel properties, and
 * display characteristics. It also represents the pixel data state.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.data.models

/**
 * Represents the configuration and state of a pixel matrix display.
 *
 * @property width Width of the matrix in pixels
 * @property height Height of the matrix in pixels
 * @property pixels 2D array of pixel brightness values (0-255)
 */
data class MatrixModel(
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    val pixels: Array<IntArray> = createEmptyMatrix(width, height)
) {
    companion object {
        /** Default matrix width matching Nothing Phone 3 Glyph Matrix */
        const val DEFAULT_WIDTH = 33

        /** Default matrix height matching Nothing Phone 3 Glyph Matrix */
        const val DEFAULT_HEIGHT = 11

        /** Minimum supported matrix dimension */
        const val MIN_DIMENSION = 4

        /** Maximum supported matrix dimension */
        const val MAX_DIMENSION = 64

        /** Maximum brightness value */
        const val MAX_BRIGHTNESS = 255

        /** Creates an empty matrix filled with zeros */
        fun createEmptyMatrix(width: Int, height: Int): Array<IntArray> {
            return Array(height) { IntArray(width) { 0 } }
        }

        /** Creates a default matrix configuration for Nothing Phone 3 */
        fun createDefault(): MatrixModel {
            return MatrixModel(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        }

        /** Creates a custom-sized matrix */
        fun createCustom(width: Int, height: Int): MatrixModel {
            val safeWidth = width.coerceIn(MIN_DIMENSION, MAX_DIMENSION)
            val safeHeight = height.coerceIn(MIN_DIMENSION, MAX_DIMENSION)
            return MatrixModel(safeWidth, safeHeight)
        }
    }

    /**
     * Gets the pixel value at the specified coordinates.
     *
     * @param x X coordinate (column)
     * @param y Y coordinate (row)
     * @return Pixel brightness value (0-255), or 0 if out of bounds
     */
    fun getPixel(x: Int, y: Int): Int {
        return if (isValidCoordinate(x, y)) {
            pixels[y][x]
        } else {
            0
        }
    }

    /**
     * Creates a new MatrixModel with the pixel at specified coordinates set to the given value.
     *
     * @param x X coordinate (column)
     * @param y Y coordinate (row)
     * @param value Brightness value (0-255)
     * @return New MatrixModel with updated pixel
     */
    fun setPixel(x: Int, y: Int, value: Int): MatrixModel {
        if (!isValidCoordinate(x, y)) return this

        val newPixels = pixels.map { it.copyOf() }.toTypedArray()
        newPixels[y][x] = value.coerceIn(0, MAX_BRIGHTNESS)
        return copy(pixels = newPixels)
    }

    /**
     * Creates a new MatrixModel with all pixels cleared to zero.
     *
     * @return New MatrixModel with cleared pixels
     */
    fun clear(): MatrixModel {
        return copy(pixels = createEmptyMatrix(width, height))
    }

    /**
     * Creates a new MatrixModel filled with the specified value.
     *
     * @param value Brightness value to fill (0-255)
     * @return New MatrixModel with filled pixels
     */
    fun fill(value: Int): MatrixModel {
        val safeValue = value.coerceIn(0, MAX_BRIGHTNESS)
        val newPixels = Array(height) { IntArray(width) { safeValue } }
        return copy(pixels = newPixels)
    }

    /**
     * Creates a new MatrixModel with inverted pixel values.
     *
     * @return New MatrixModel with inverted pixels
     */
    fun invert(): MatrixModel {
        val newPixels = pixels.map { row ->
            row.map { MAX_BRIGHTNESS - it }.toIntArray()
        }.toTypedArray()
        return copy(pixels = newPixels)
    }

    /**
     * Checks if the specified coordinates are within the matrix bounds.
     *
     * @param x X coordinate (column)
     * @param y Y coordinate (row)
     * @return true if coordinates are valid
     */
    fun isValidCoordinate(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    /**
     * Creates a deep copy of this matrix model.
     *
     * @return New MatrixModel with copied pixel data
     */
    fun deepCopy(): MatrixModel {
        return copy(pixels = pixels.map { it.copyOf() }.toTypedArray())
    }

    /**
     * Counts the number of lit (non-zero) pixels.
     *
     * @return Count of pixels with brightness > 0
     */
    fun countLitPixels(): Int {
        return pixels.sumOf { row -> row.count { it > 0 } }
    }

    /**
     * Checks if any pixels are lit.
     *
     * @return true if at least one pixel has brightness > 0
     */
    fun hasContent(): Boolean {
        return pixels.any { row -> row.any { it > 0 } }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatrixModel

        if (width != other.width) return false
        if (height != other.height) return false
        if (!pixels.contentDeepEquals(other.pixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentDeepHashCode()
        return result
    }
}
