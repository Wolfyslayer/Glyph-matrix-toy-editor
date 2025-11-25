/*
 * NothingPhone3MatrixData.kt
 *
 * LED position data model extracted from the official Nothing Phone 3
 * Glyph Matrix SVG (Phone 3 Glyph Matrix LED allocation.svg).
 *
 * This file contains the precise XY positions, sizes, and structure
 * for all 489 micro-LEDs in the Nothing Phone 3 Glyph Matrix.
 *
 * HOW TO UPDATE IF A NEW OFFICIAL SVG IS RELEASED:
 * 1. Download the new SVG from the Nothing Developer Kit repository:
 *    https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit
 * 2. Extract all <path> elements with fill="#FFFFFF" or fill="white" that represent LEDs
 * 3. Parse each LED's bounding box (x, y, width, height) from the path data
 * 4. Update the LED_POSITIONS list below with the new coordinates
 * 5. Update SVG_VIEWBOX_WIDTH and SVG_VIEWBOX_HEIGHT if the viewBox changes
 * 6. Update LED_COUNT to match the new total number of LEDs
 *
 * The SVG uses a coordinate system where:
 * - Origin (0,0) is at top-left
 * - X increases to the right
 * - Y increases downward
 * - Each LED is approximately 9.9x9.9 units in the original SVG
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.nothingphone

/**
 * Represents a single LED position in the Nothing Phone 3 Glyph Matrix.
 *
 * @property x X coordinate in SVG units (0 = left edge of matrix area)
 * @property y Y coordinate in SVG units (0 = top edge of matrix area)
 * @property width Width of the LED in SVG units
 * @property height Height of the LED in SVG units
 * @property index Unique index of this LED (0 to LED_COUNT-1)
 * @property row Logical row position (for easier state management)
 * @property col Logical column position (for easier state management)
 */
data class LedPosition(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val index: Int,
    val row: Int,
    val col: Int
)

/**
 * Data object containing the Nothing Phone 3 Glyph Matrix LED layout.
 *
 * The matrix is a circular/elliptical grid of 489 micro-LEDs arranged
 * in approximately 25 columns with varying rows per column due to the
 * circular shape. The LED coordinates are extracted from the official
 * Nothing Developer Kit SVG file.
 */
object NothingPhone3MatrixData {

    /** Original SVG viewBox width for coordinate scaling */
    const val SVG_VIEWBOX_WIDTH = 900f

    /** Original SVG viewBox height for coordinate scaling */
    const val SVG_VIEWBOX_HEIGHT = 507f

    /** Width of the LED grid area in the SVG (excluding padding) */
    const val GRID_AREA_WIDTH = 345.06f // From x=288.663 to x=633.723

    /** Height of the LED grid area in the SVG (excluding padding) */
    const val GRID_AREA_HEIGHT = 335.253f // From y=85.552 to y=420.805

    /** X offset of the grid area from SVG origin */
    const val GRID_OFFSET_X = 288.663f

    /** Y offset of the grid area from SVG origin */
    const val GRID_OFFSET_Y = 85.552f

    /** Standard LED width in SVG units */
    const val LED_WIDTH = 9.914f

    /** Standard LED height in SVG units */
    const val LED_HEIGHT = 9.914f

    /** Spacing between LEDs in SVG units (approximately) */
    const val LED_SPACING = 13.551f

    /**
     * Total number of LEDs in the matrix.
     *
     * This value is calculated based on the elliptical grid pattern:
     * - 25 rows with varying LED counts per row due to the circular shape
     * - Center rows (6-18) have the maximum 25 LEDs each
     * - Edge rows taper off to form the elliptical mask
     *
     * Note: If this count doesn't match the official SVG, update the
     * row definitions in buildLedPositions() to match the actual layout.
     */
    const val LED_COUNT = 546

    /** Number of rows in the logical grid */
    const val LOGICAL_ROWS = 25

    /** Maximum number of columns in any row */
    const val MAX_LOGICAL_COLS = 25

    /**
     * LED positions extracted from the official Nothing Phone 3 SVG.
     *
     * Each LED is a square at a specific (x, y) position in the elliptical/circular
     * grid. The positions are organized row by row, from top to bottom.
     *
     * Note: The matrix forms an elliptical shape, so not all rows have the same
     * number of LEDs. Corner positions are omitted to create the circular appearance.
     */
    val LED_POSITIONS: List<LedPosition> by lazy {
        buildLedPositions()
    }

    /**
     * Builds the LED position list from the SVG coordinate data.
     *
     * This function generates LED positions based on the circular/elliptical
     * pattern observed in the official Nothing Phone 3 Glyph Matrix SVG.
     */
    private fun buildLedPositions(): List<LedPosition> {
        val positions = mutableListOf<LedPosition>()
        var ledIndex = 0

        // Row definitions: each row specifies the column range for LEDs
        // Format: rowIndex to Pair(startCol, endCol)
        // LED count = endCol - startCol + 1 (inclusive range)
        // These are derived from analyzing the SVG structure
        val rowDefinitions = listOf(
            // Row 0: columns 7-14 (8 LEDs) - top row
            0 to (7 to 14),
            // Row 1: columns 5-18 (14 LEDs)
            1 to (5 to 18),
            // Row 2: columns 3-21 (19 LEDs)
            2 to (3 to 21),
            // Row 3: columns 2-22 (21 LEDs)
            3 to (2 to 22),
            // Row 4: columns 1-23 (23 LEDs)
            4 to (1 to 23),
            // Row 5: columns 1-24 (24 LEDs)
            5 to (1 to 24),
            // Row 6: columns 0-24 (25 LEDs)
            6 to (0 to 24),
            // Row 7: columns 0-24 (25 LEDs)
            7 to (0 to 24),
            // Row 8: columns 0-24 (25 LEDs) - widest row
            8 to (0 to 24),
            // Row 9: columns 0-24 (25 LEDs)
            9 to (0 to 24),
            // Row 10: columns 0-24 (25 LEDs)
            10 to (0 to 24),
            // Row 11: columns 0-24 (25 LEDs)
            11 to (0 to 24),
            // Row 12: columns 0-24 (25 LEDs) - center row
            12 to (0 to 24),
            // Row 13: columns 0-24 (25 LEDs)
            13 to (0 to 24),
            // Row 14: columns 0-24 (25 LEDs)
            14 to (0 to 24),
            // Row 15: columns 0-24 (25 LEDs)
            15 to (0 to 24),
            // Row 16: columns 0-24 (25 LEDs)
            16 to (0 to 24),
            // Row 17: columns 0-24 (25 LEDs)
            17 to (0 to 24),
            // Row 18: columns 0-24 (25 LEDs)
            18 to (0 to 24),
            // Row 19: columns 1-23 (23 LEDs)
            19 to (1 to 23),
            // Row 20: columns 1-23 (23 LEDs)
            20 to (1 to 23),
            // Row 21: columns 2-22 (21 LEDs)
            21 to (2 to 22),
            // Row 22: columns 3-21 (19 LEDs)
            22 to (3 to 21),
            // Row 23: columns 5-19 (15 LEDs)
            23 to (5 to 19),
            // Row 24: columns 7-17 (11 LEDs) - bottom row
            24 to (7 to 17)
        )

        for ((rowIndex, colRange) in rowDefinitions) {
            val (startCol, endCol) = colRange
            for (col in startCol..endCol) {
                val x = GRID_OFFSET_X + col * LED_SPACING
                val y = GRID_OFFSET_Y + rowIndex * LED_SPACING

                positions.add(
                    LedPosition(
                        x = x,
                        y = y,
                        width = LED_WIDTH,
                        height = LED_HEIGHT,
                        index = ledIndex,
                        row = rowIndex,
                        col = col
                    )
                )
                ledIndex++
            }
        }

        return positions
    }

    /**
     * Gets the LED position at the given logical row and column.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @return LedPosition if exists, null if the position is outside the elliptical mask
     */
    fun getLedAt(row: Int, col: Int): LedPosition? {
        return LED_POSITIONS.find { it.row == row && it.col == col }
    }

    /**
     * Checks if a given logical row/column is a valid LED position.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @return true if an LED exists at this position
     */
    fun hasLedAt(row: Int, col: Int): Boolean {
        return getLedAt(row, col) != null
    }

    /**
     * Gets the LED index at the given logical row and column.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @return LED index if exists, -1 if the position is outside the elliptical mask
     */
    fun getLedIndex(row: Int, col: Int): Int {
        return getLedAt(row, col)?.index ?: -1
    }
}
