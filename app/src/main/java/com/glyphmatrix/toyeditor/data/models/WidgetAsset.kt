/*
 * WidgetAsset.kt
 *
 * Data model representing a widget asset.
 *
 * This data class defines widget assets that can be placed on
 * the matrix canvas, including pre-built functional widgets and
 * user-created custom widgets.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.data.models

/**
 * Represents a widget asset that can be placed on the canvas.
 *
 * @property type The type of widget
 * @property name Display name of the widget
 * @property description Brief description of the widget
 * @property previewPixels Preview pixel data for the widget
 */
data class WidgetAsset(
    val type: WidgetType,
    val name: String,
    val description: String,
    val previewPixels: Array<IntArray>
) {
    companion object {
        /** Gets all available built-in widgets */
        fun getBuiltInWidgets(): List<WidgetAsset> {
            return listOf(
                createClockWidget(),
                createBatteryWidget(),
                createBatteryPercentWidget()
            )
        }

        /** Creates the clock widget asset */
        private fun createClockWidget(): WidgetAsset {
            // 5x5 pixel preview showing "12" in pixel font
            val preview = arrayOf(
                intArrayOf(255, 0, 255, 255, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                intArrayOf(0, 0, 0, 255, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                intArrayOf(0, 0, 255, 255, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                intArrayOf(0, 0, 255, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                intArrayOf(0, 0, 255, 255, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
            return WidgetAsset(
                type = WidgetType.CLOCK,
                name = "Pixel Clock",
                description = "Displays current time in pixel art style",
                previewPixels = preview
            )
        }

        /** Creates the battery widget asset */
        private fun createBatteryWidget(): WidgetAsset {
            // Battery icon preview
            val preview = arrayOf(
                intArrayOf(0, 255, 255, 255, 255, 255, 255),
                intArrayOf(255, 255, 0, 0, 0, 0, 255),
                intArrayOf(255, 255, 255, 255, 255, 0, 255),
                intArrayOf(255, 255, 0, 0, 0, 0, 255),
                intArrayOf(0, 255, 255, 255, 255, 255, 255)
            )
            return WidgetAsset(
                type = WidgetType.BATTERY,
                name = "Battery",
                description = "Battery indicator with charging animation",
                previewPixels = preview
            )
        }

        /** Creates the battery with percentage widget asset */
        private fun createBatteryPercentWidget(): WidgetAsset {
            // Battery with percentage preview
            val preview = arrayOf(
                intArrayOf(0, 255, 255, 255, 255, 255, 255, 0, 255, 255, 0, 255, 0),
                intArrayOf(255, 255, 0, 0, 0, 0, 255, 0, 255, 0, 0, 255, 0),
                intArrayOf(255, 255, 255, 255, 255, 0, 255, 0, 255, 255, 0, 255, 0),
                intArrayOf(255, 255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0),
                intArrayOf(0, 255, 255, 255, 255, 255, 255, 0, 255, 255, 0, 255, 0)
            )
            return WidgetAsset(
                type = WidgetType.BATTERY_PERCENT,
                name = "Battery %",
                description = "Battery with percentage display",
                previewPixels = preview
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WidgetAsset

        if (type != other.type) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (!previewPixels.contentDeepEquals(other.previewPixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + previewPixels.contentDeepHashCode()
        return result
    }
}
