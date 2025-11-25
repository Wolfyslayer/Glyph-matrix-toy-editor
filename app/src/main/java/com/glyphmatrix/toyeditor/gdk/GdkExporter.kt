/*
 * GdkExporter.kt
 *
 * Exporter component for converting user-created matrix animations
 * to Nothing Glyph Developer Kit (GDK) compatible payloads.
 *
 * This class handles the conversion of animation projects into formats
 * that can be used with the Nothing Glyph Matrix SDK for displaying
 * custom animations on Nothing Phone 3 devices.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.gdk

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Represents a single frame in a GDK-compatible animation.
 *
 * @property frameIndex The index of this frame in the animation sequence
 * @property durationMs Duration this frame should be displayed in milliseconds
 * @property matrixData 2D array of brightness values (0-4095) for each LED
 */
data class GdkFrame(
    val frameIndex: Int,
    val durationMs: Long,
    val matrixData: Array<IntArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GdkFrame
        if (frameIndex != other.frameIndex) return false
        if (durationMs != other.durationMs) return false
        if (!matrixData.contentDeepEquals(other.matrixData)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = frameIndex
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + matrixData.contentDeepHashCode()
        return result
    }
}

/**
 * Represents a complete GDK-compatible animation payload.
 *
 * @property name Display name of the animation
 * @property version Payload format version
 * @property matrixWidth Width of the LED matrix
 * @property matrixHeight Height of the LED matrix
 * @property frameCount Total number of frames
 * @property loopCount Number of times to loop (-1 for infinite)
 * @property frames List of animation frames
 */
data class GdkAnimationPayload(
    val name: String,
    val version: Int = 1,
    val matrixWidth: Int,
    val matrixHeight: Int,
    val frameCount: Int,
    val loopCount: Int = -1,
    val frames: List<GdkFrame>
)

/**
 * Export format options for GDK animations.
 */
enum class GdkExportFormat {
    /** JSON format for debugging and manual import */
    JSON,
    /** Binary format optimized for device storage */
    BINARY
}

/**
 * Result of an export operation.
 */
sealed class GdkExportResult {
    data class Success(val filePath: String, val format: GdkExportFormat) : GdkExportResult()
    data class Error(val message: String, val exception: Exception? = null) : GdkExportResult()
}

/**
 * Exporter for converting animations to GDK-compatible formats.
 *
 * Usage:
 * ```kotlin
 * val exporter = GdkExporter(context)
 * val payload = exporter.createPayload("MyAnimation", frames, 33, 11)
 * val result = exporter.exportToFile(payload, GdkExportFormat.JSON)
 * ```
 */
class GdkExporter(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    companion object {
        /** Maximum brightness value for GDK LEDs */
        const val MAX_BRIGHTNESS = 4095

        /** Default frame duration in milliseconds */
        const val DEFAULT_FRAME_DURATION_MS = 100L

        /** Export file extension for JSON format */
        const val JSON_EXTENSION = ".glyph.json"

        /** Export file extension for binary format */
        const val BINARY_EXTENSION = ".glyph.bin"

        /** Export directory name */
        const val EXPORT_DIR = "glyph_exports"
    }

    /**
     * Creates a GDK animation payload from frame data.
     *
     * @param name Name of the animation
     * @param frames List of frame data where each frame is a 2D array of pixel values
     * @param width Width of the matrix
     * @param height Height of the matrix
     * @param frameDurationMs Duration of each frame in milliseconds
     * @param loopCount Number of loops (-1 for infinite)
     * @return GdkAnimationPayload ready for export
     */
    fun createPayload(
        name: String,
        frames: List<Array<IntArray>>,
        width: Int,
        height: Int,
        frameDurationMs: Long = DEFAULT_FRAME_DURATION_MS,
        loopCount: Int = -1
    ): GdkAnimationPayload {
        val gdkFrames = frames.mapIndexed { index, frameData ->
            GdkFrame(
                frameIndex = index,
                durationMs = frameDurationMs,
                matrixData = normalizeToGdkBrightness(frameData)
            )
        }

        return GdkAnimationPayload(
            name = name,
            matrixWidth = width,
            matrixHeight = height,
            frameCount = gdkFrames.size,
            loopCount = loopCount,
            frames = gdkFrames
        )
    }

    /**
     * Normalizes pixel values to GDK brightness range (0-4095).
     *
     * @param frameData Original frame data with arbitrary pixel values
     * @return Frame data normalized to GDK brightness range
     */
    private fun normalizeToGdkBrightness(frameData: Array<IntArray>): Array<IntArray> {
        return frameData.map { row ->
            row.map { pixel ->
                // Normalize from 0-255 (typical color value) to 0-4095
                (pixel.coerceIn(0, 255) * MAX_BRIGHTNESS / 255)
            }.toIntArray()
        }.toTypedArray()
    }

    /**
     * Exports an animation payload to a file.
     *
     * @param payload The animation payload to export
     * @param format Export format (JSON or BINARY)
     * @param customFileName Optional custom file name (without extension)
     * @return GdkExportResult indicating success or failure
     */
    fun exportToFile(
        payload: GdkAnimationPayload,
        format: GdkExportFormat = GdkExportFormat.JSON,
        customFileName: String? = null
    ): GdkExportResult {
        return try {
            val exportDir = getExportDirectory()
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                return GdkExportResult.Error("Failed to create export directory")
            }

            val fileName = customFileName ?: sanitizeFileName(payload.name)
            val extension = when (format) {
                GdkExportFormat.JSON -> JSON_EXTENSION
                GdkExportFormat.BINARY -> BINARY_EXTENSION
            }

            val file = File(exportDir, "$fileName$extension")

            when (format) {
                GdkExportFormat.JSON -> exportAsJson(payload, file)
                GdkExportFormat.BINARY -> exportAsBinary(payload, file)
            }

            GdkExportResult.Success(file.absolutePath, format)
        } catch (e: Exception) {
            GdkExportResult.Error("Export failed: ${e.message}", e)
        }
    }

    /**
     * Exports payload as JSON file.
     */
    private fun exportAsJson(payload: GdkAnimationPayload, file: File) {
        BufferedWriter(OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8)).use { writer ->
            gson.toJson(payload, writer)
        }
    }

    /**
     * Exports payload as binary file for optimized storage.
     *
     * Binary format:
     * - Header: version (1 byte), width (2 bytes), height (2 bytes), frameCount (4 bytes)
     * - For each frame: duration (4 bytes), then width*height brightness values (2 bytes each)
     */
    private fun exportAsBinary(payload: GdkAnimationPayload, file: File) {
        FileOutputStream(file).use { stream ->
            // Write header
            stream.write(payload.version)
            stream.write(payload.matrixWidth shr 8)
            stream.write(payload.matrixWidth and 0xFF)
            stream.write(payload.matrixHeight shr 8)
            stream.write(payload.matrixHeight and 0xFF)
            stream.write((payload.frameCount shr 24) and 0xFF)
            stream.write((payload.frameCount shr 16) and 0xFF)
            stream.write((payload.frameCount shr 8) and 0xFF)
            stream.write(payload.frameCount and 0xFF)

            // Write frames
            for (frame in payload.frames) {
                // Write duration
                stream.write((frame.durationMs.toInt() shr 24) and 0xFF)
                stream.write((frame.durationMs.toInt() shr 16) and 0xFF)
                stream.write((frame.durationMs.toInt() shr 8) and 0xFF)
                stream.write(frame.durationMs.toInt() and 0xFF)

                // Write matrix data
                for (row in frame.matrixData) {
                    for (value in row) {
                        stream.write((value shr 8) and 0xFF)
                        stream.write(value and 0xFF)
                    }
                }
            }
        }
    }

    /**
     * Gets the export directory for glyph animations.
     */
    private fun getExportDirectory(): File {
        return File(context.getExternalFilesDir(null), EXPORT_DIR)
    }

    /**
     * Gets the path to the export directory.
     */
    fun getExportDirectoryPath(): String {
        return getExportDirectory().absolutePath
    }

    /**
     * Sanitizes a file name by removing invalid characters.
     */
    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(50) // Limit length
            .ifBlank { "glyph_animation" }
    }

    /**
     * Lists all exported animation files.
     */
    fun listExportedAnimations(): List<File> {
        val exportDir = getExportDirectory()
        return if (exportDir.exists()) {
            exportDir.listFiles { file ->
                file.name.endsWith(JSON_EXTENSION) || file.name.endsWith(BINARY_EXTENSION)
            }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Deletes an exported animation file.
     */
    fun deleteExport(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.delete()
    }

    /**
     * Loads a previously exported JSON payload.
     */
    fun loadFromJson(filePath: String): GdkAnimationPayload? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            file.bufferedReader().use { reader ->
                gson.fromJson(reader, GdkAnimationPayload::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
}
