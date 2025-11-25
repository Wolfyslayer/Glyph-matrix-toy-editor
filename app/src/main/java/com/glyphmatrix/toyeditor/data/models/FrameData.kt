/*
 * FrameData.kt
 *
 * Data model representing a single animation frame.
 *
 * This data class encapsulates all pixel data and metadata for
 * a single frame within an animation sequence.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.data.models

import java.util.UUID

/**
 * Represents a single frame in an animation sequence.
 *
 * @property id Unique identifier for this frame
 * @property matrix The pixel data for this frame
 * @property durationMs Duration this frame should be displayed in milliseconds
 * @property name Optional name for the frame
 * @property placedWidgets List of widget instances placed on this frame
 */
data class FrameData(
    val id: String = UUID.randomUUID().toString(),
    val matrix: MatrixModel = MatrixModel.createDefault(),
    val durationMs: Long = DEFAULT_DURATION_MS,
    val name: String = "",
    val placedWidgets: List<PlacedWidget> = emptyList()
) {
    companion object {
        /** Default frame duration in milliseconds */
        const val DEFAULT_DURATION_MS = 100L

        /** Minimum frame duration */
        const val MIN_DURATION_MS = 16L // ~60fps

        /** Maximum frame duration */
        const val MAX_DURATION_MS = 5000L // 5 seconds

        /** Creates an empty frame with default settings */
        fun createEmpty(width: Int = MatrixModel.DEFAULT_WIDTH, height: Int = MatrixModel.DEFAULT_HEIGHT): FrameData {
            return FrameData(matrix = MatrixModel.createCustom(width, height))
        }
    }

    /**
     * Creates a copy of this frame with updated duration.
     *
     * @param durationMs New duration in milliseconds
     * @return New FrameData with updated duration
     */
    fun withDuration(durationMs: Long): FrameData {
        val safeDuration = durationMs.coerceIn(MIN_DURATION_MS, MAX_DURATION_MS)
        return copy(durationMs = safeDuration)
    }

    /**
     * Creates a copy of this frame with updated matrix.
     *
     * @param matrix New matrix data
     * @return New FrameData with updated matrix
     */
    fun withMatrix(matrix: MatrixModel): FrameData {
        return copy(matrix = matrix)
    }

    /**
     * Creates a copy of this frame with a pixel set.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param value Brightness value
     * @return New FrameData with updated pixel
     */
    fun setPixel(x: Int, y: Int, value: Int): FrameData {
        return copy(matrix = matrix.setPixel(x, y, value))
    }

    /**
     * Creates a copy of this frame with cleared matrix.
     *
     * @return New FrameData with cleared matrix
     */
    fun clear(): FrameData {
        return copy(matrix = matrix.clear())
    }

    /**
     * Creates a deep copy of this frame with a new ID.
     *
     * @return New FrameData that is a duplicate of this frame
     */
    fun duplicate(): FrameData {
        return copy(
            id = UUID.randomUUID().toString(),
            matrix = matrix.deepCopy(),
            placedWidgets = placedWidgets.toList()
        )
    }

    /**
     * Adds a widget to this frame.
     *
     * @param widget The widget to add
     * @return New FrameData with the widget added
     */
    fun addWidget(widget: PlacedWidget): FrameData {
        return copy(placedWidgets = placedWidgets + widget)
    }

    /**
     * Removes a widget from this frame.
     *
     * @param widgetId ID of the widget to remove
     * @return New FrameData with the widget removed
     */
    fun removeWidget(widgetId: String): FrameData {
        return copy(placedWidgets = placedWidgets.filter { it.id != widgetId })
    }

    /**
     * Updates a widget on this frame.
     *
     * @param widget The updated widget
     * @return New FrameData with the widget updated
     */
    fun updateWidget(widget: PlacedWidget): FrameData {
        return copy(placedWidgets = placedWidgets.map { 
            if (it.id == widget.id) widget else it 
        })
    }

    /**
     * Checks if this frame has any content (lit pixels or widgets).
     *
     * @return true if frame has content
     */
    fun hasContent(): Boolean {
        return matrix.hasContent() || placedWidgets.isNotEmpty()
    }
}

/**
 * Represents a widget instance placed on a frame.
 *
 * @property id Unique identifier for this placement
 * @property widgetType Type of widget (e.g., "clock", "battery")
 * @property x X position on the matrix
 * @property y Y position on the matrix
 * @property config Widget-specific configuration
 */
data class PlacedWidget(
    val id: String = UUID.randomUUID().toString(),
    val widgetType: WidgetType,
    val x: Int,
    val y: Int,
    val config: Map<String, Any> = emptyMap()
) {
    /**
     * Creates a copy of this widget with updated position.
     *
     * @param x New X position
     * @param y New Y position
     * @return New PlacedWidget with updated position
     */
    fun moveTo(x: Int, y: Int): PlacedWidget {
        return copy(x = x, y = y)
    }
}

/**
 * Enumeration of available widget types.
 */
enum class WidgetType(val displayName: String, val width: Int, val height: Int) {
    CLOCK("Clock", 17, 5),
    BATTERY("Battery", 7, 5),
    BATTERY_PERCENT("Battery %", 13, 5)
}
