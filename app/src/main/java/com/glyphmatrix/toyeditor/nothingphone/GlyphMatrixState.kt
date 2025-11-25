/*
 * GlyphMatrixState.kt
 *
 * State management for the Nothing Phone 3 Glyph Matrix LED editor.
 *
 * This file provides an exportable Kotlin data structure for tracking
 * the on/off state of all LEDs in the Glyph Matrix, with support for
 * serialization and export to GDK-compatible formats.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.nothingphone

import androidx.compose.runtime.Immutable

/**
 * Represents the state of a single LED in the Glyph Matrix.
 *
 * @property index Unique index of this LED (0 to LED_COUNT-1)
 * @property isOn Whether the LED is currently lit
 * @property brightness Brightness level (0-255 for compatibility with existing system)
 */
@Immutable
data class LedState(
    val index: Int,
    val isOn: Boolean = false,
    val brightness: Int = 255
)

/**
 * Immutable state container for the entire Nothing Phone 3 Glyph Matrix.
 *
 * This class provides:
 * - Efficient state storage using a Map for sparse representation
 * - Immutable state updates with copy-on-write semantics
 * - Export functionality for GDK-compatible formats
 * - Conversion to/from the existing MatrixModel format
 *
 * @property ledStates Map of LED index to LED state (only "on" LEDs are stored)
 * @property brightness Global brightness multiplier (0-255)
 */
@Immutable
data class GlyphMatrixState(
    private val ledStates: Map<Int, LedState> = emptyMap(),
    val brightness: Int = 255
) {
    companion object {
        /** Maximum LED index (0-based) */
        const val MAX_LED_INDEX = NothingPhone3MatrixData.LED_COUNT - 1

        /** Creates an empty state with all LEDs off */
        fun empty(): GlyphMatrixState = GlyphMatrixState()

        /** Creates a state with all LEDs on at full brightness */
        fun allOn(): GlyphMatrixState {
            val states = (0 until NothingPhone3MatrixData.LED_COUNT).associateWith { index ->
                LedState(index = index, isOn = true, brightness = 255)
            }
            return GlyphMatrixState(ledStates = states)
        }

        /** Creates a state from a list of lit LED indices */
        fun fromIndices(indices: Collection<Int>, brightness: Int = 255): GlyphMatrixState {
            val states = indices.filter { it in 0..MAX_LED_INDEX }.associateWith { index ->
                LedState(index = index, isOn = true, brightness = brightness)
            }
            return GlyphMatrixState(ledStates = states, brightness = brightness)
        }

        /** Creates a state from row/column pairs */
        fun fromRowCol(positions: Collection<Pair<Int, Int>>, brightness: Int = 255): GlyphMatrixState {
            val indices = positions.mapNotNull { (row, col) ->
                NothingPhone3MatrixData.getLedIndex(row, col).takeIf { it >= 0 }
            }
            return fromIndices(indices, brightness)
        }
    }

    /** Returns the total number of LEDs in the matrix */
    val totalLeds: Int get() = NothingPhone3MatrixData.LED_COUNT

    /** Returns the number of LEDs that are currently lit */
    val litLedCount: Int get() = ledStates.count { it.value.isOn }

    /** Returns true if any LED is lit */
    val hasContent: Boolean get() = ledStates.any { it.value.isOn }

    /**
     * Gets the state of a specific LED by index.
     *
     * @param index LED index (0 to LED_COUNT-1)
     * @return LedState for the LED, defaults to off if not in map
     */
    fun getLedState(index: Int): LedState {
        return ledStates[index] ?: LedState(index = index, isOn = false)
    }

    /**
     * Gets the state of a specific LED by row and column.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @return LedState for the LED, or null if no LED exists at this position
     */
    fun getLedStateAt(row: Int, col: Int): LedState? {
        val index = NothingPhone3MatrixData.getLedIndex(row, col)
        return if (index >= 0) getLedState(index) else null
    }

    /**
     * Checks if a specific LED is on.
     *
     * @param index LED index
     * @return true if the LED is on
     */
    fun isLedOn(index: Int): Boolean {
        return ledStates[index]?.isOn == true
    }

    /**
     * Checks if a specific LED is on by row and column.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @return true if the LED is on, false if off or if no LED exists at this position
     */
    fun isLedOnAt(row: Int, col: Int): Boolean {
        val index = NothingPhone3MatrixData.getLedIndex(row, col)
        return if (index >= 0) isLedOn(index) else false
    }

    /**
     * Toggles the state of a specific LED.
     *
     * @param index LED index
     * @return New GlyphMatrixState with the LED toggled
     */
    fun toggleLed(index: Int): GlyphMatrixState {
        if (index !in 0..MAX_LED_INDEX) return this

        val currentState = getLedState(index)
        val newState = currentState.copy(isOn = !currentState.isOn)

        val newStates = if (newState.isOn) {
            ledStates + (index to newState)
        } else {
            ledStates - index
        }

        return copy(ledStates = newStates)
    }

    /**
     * Toggles the state of a specific LED by row and column.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @return New GlyphMatrixState with the LED toggled, or same state if no LED exists
     */
    fun toggleLedAt(row: Int, col: Int): GlyphMatrixState {
        val index = NothingPhone3MatrixData.getLedIndex(row, col)
        return if (index >= 0) toggleLed(index) else this
    }

    /**
     * Sets the state of a specific LED.
     *
     * @param index LED index
     * @param isOn Whether the LED should be on
     * @param ledBrightness Brightness for this LED (0-255)
     * @return New GlyphMatrixState with the LED state updated
     */
    fun setLed(index: Int, isOn: Boolean, ledBrightness: Int = brightness): GlyphMatrixState {
        if (index !in 0..MAX_LED_INDEX) return this

        val newState = LedState(index = index, isOn = isOn, brightness = ledBrightness)

        val newStates = if (isOn) {
            ledStates + (index to newState)
        } else {
            ledStates - index
        }

        return copy(ledStates = newStates)
    }

    /**
     * Sets the state of a specific LED by row and column.
     *
     * @param row Logical row index
     * @param col Logical column index
     * @param isOn Whether the LED should be on
     * @param ledBrightness Brightness for this LED (0-255)
     * @return New GlyphMatrixState with the LED state updated
     */
    fun setLedAt(row: Int, col: Int, isOn: Boolean, ledBrightness: Int = brightness): GlyphMatrixState {
        val index = NothingPhone3MatrixData.getLedIndex(row, col)
        return if (index >= 0) setLed(index, isOn, ledBrightness) else this
    }

    /**
     * Clears all LEDs (turns them all off).
     *
     * @return New GlyphMatrixState with all LEDs off
     */
    fun clear(): GlyphMatrixState {
        return copy(ledStates = emptyMap())
    }

    /**
     * Fills all LEDs (turns them all on).
     *
     * @param fillBrightness Brightness for all LEDs (0-255)
     * @return New GlyphMatrixState with all LEDs on
     */
    fun fill(fillBrightness: Int = brightness): GlyphMatrixState {
        val states = (0 until NothingPhone3MatrixData.LED_COUNT).associateWith { index ->
            LedState(index = index, isOn = true, brightness = fillBrightness)
        }
        return copy(ledStates = states)
    }

    /**
     * Inverts all LED states (on becomes off, off becomes on).
     *
     * @return New GlyphMatrixState with all LED states inverted
     */
    fun invert(): GlyphMatrixState {
        val newStates = (0 until NothingPhone3MatrixData.LED_COUNT).mapNotNull { index ->
            val currentlyOn = isLedOn(index)
            if (!currentlyOn) {
                index to LedState(index = index, isOn = true, brightness = brightness)
            } else {
                null
            }
        }.toMap()
        return copy(ledStates = newStates)
    }

    /**
     * Sets the global brightness.
     *
     * @param newBrightness New brightness value (0-255)
     * @return New GlyphMatrixState with updated brightness
     */
    fun withBrightness(newBrightness: Int): GlyphMatrixState {
        return copy(brightness = newBrightness.coerceIn(0, 255))
    }

    /**
     * Returns a list of all lit LED indices.
     */
    fun getLitLedIndices(): List<Int> {
        return ledStates.filter { it.value.isOn }.keys.sorted()
    }

    /**
     * Returns a list of all lit LED positions (row, col pairs).
     */
    fun getLitLedPositions(): List<Pair<Int, Int>> {
        return getLitLedIndices().mapNotNull { index ->
            NothingPhone3MatrixData.LED_POSITIONS.getOrNull(index)?.let { led ->
                led.row to led.col
            }
        }
    }

    /**
     * Exports the state to a 2D array compatible with GDK format.
     *
     * The array dimensions are [LOGICAL_ROWS][MAX_LOGICAL_COLS] where
     * each cell contains the brightness value (0 for off, brightness for on).
     *
     * @return 2D array of brightness values
     */
    fun toGdkArray(): Array<IntArray> {
        val array = Array(NothingPhone3MatrixData.LOGICAL_ROWS) {
            IntArray(NothingPhone3MatrixData.MAX_LOGICAL_COLS) { 0 }
        }

        for ((index, state) in ledStates) {
            if (state.isOn) {
                NothingPhone3MatrixData.LED_POSITIONS.getOrNull(index)?.let { led ->
                    if (led.row in array.indices && led.col in array[led.row].indices) {
                        // Scale to GDK brightness range (0-4095)
                        array[led.row][led.col] = (state.brightness * 4095 / 255)
                    }
                }
            }
        }

        return array
    }

    /**
     * Exports the state to a JSON-compatible map.
     *
     * @return Map containing the matrix state data
     */
    fun toExportMap(): Map<String, Any> {
        return mapOf(
            "version" to 1,
            "type" to "nothing_phone_3_glyph_matrix",
            "ledCount" to totalLeds,
            "litCount" to litLedCount,
            "brightness" to brightness,
            "litLeds" to getLitLedIndices(),
            "litPositions" to getLitLedPositions().map { (row, col) ->
                mapOf("row" to row, "col" to col)
            }
        )
    }
}
