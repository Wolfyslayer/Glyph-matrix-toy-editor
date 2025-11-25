/*
 * Exporter.kt
 *
 * Export functionality for animations.
 *
 * This class provides export capabilities to convert animation
 * projects into various output formats suitable for use with glyph
 * matrix display hardware and other applications.
 *
 * For Nothing Phone 3 GDK integration, see:
 * - com.glyphmatrix.toyeditor.gdk.GdkExporter - GDK payload export
 * - com.glyphmatrix.toyeditor.gdk.GlyphManager - GDK SDK integration
 * - com.glyphmatrix.toyeditor.gdk.GlyphToyService - Toy service registration
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.engine.logic

import android.content.Context
import android.graphics.Bitmap
import com.glyphmatrix.toyeditor.data.models.AnimationProject
import com.glyphmatrix.toyeditor.data.models.FrameData
import com.glyphmatrix.toyeditor.gdk.GdkExporter
import com.glyphmatrix.toyeditor.gdk.GdkExportFormat
import com.glyphmatrix.toyeditor.gdk.GdkExportResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream

/**
 * Export format options.
 */
enum class ExportFormat {
    /** JSON format for project interchange */
    PROJECT_JSON,
    /** GDK-compatible JSON for Nothing Phone */
    GDK_JSON,
    /** GDK-compatible binary for Nothing Phone */
    GDK_BINARY,
    /** PNG sprite sheet */
    PNG_SPRITE_SHEET
}

/**
 * Result of an export operation.
 */
sealed class ExportResult {
    data class Success(val filePath: String, val format: ExportFormat) : ExportResult()
    data class Error(val message: String, val exception: Exception? = null) : ExportResult()
}

/**
 * Exporter for converting animation projects to various formats.
 *
 * @param context Android context for file access
 */
class Exporter(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val gdkExporter = GdkExporter(context)

    companion object {
        const val PROJECT_EXTENSION = ".glyphproject.json"
        const val SPRITE_EXTENSION = ".png"
        const val EXPORT_DIR = "exports"
    }

    /**
     * Exports a project to the specified format.
     *
     * @param project The animation project to export
     * @param format Export format
     * @param fileName Optional custom file name
     * @return ExportResult indicating success or failure
     */
    fun export(
        project: AnimationProject,
        format: ExportFormat,
        fileName: String? = null
    ): ExportResult {
        return try {
            when (format) {
                ExportFormat.PROJECT_JSON -> exportProjectJson(project, fileName)
                ExportFormat.GDK_JSON -> exportGdk(project, GdkExportFormat.JSON, fileName)
                ExportFormat.GDK_BINARY -> exportGdk(project, GdkExportFormat.BINARY, fileName)
                ExportFormat.PNG_SPRITE_SHEET -> exportSpriteSheet(project, fileName)
            }
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}", e)
        }
    }

    /**
     * Exports project as JSON for later import.
     */
    private fun exportProjectJson(project: AnimationProject, fileName: String?): ExportResult {
        val exportDir = getExportDirectory()
        val name = fileName ?: sanitizeFileName(project.name)
        val file = File(exportDir, "$name$PROJECT_EXTENSION")

        val projectData = ProjectExportData(
            id = project.id,
            name = project.name,
            description = project.description,
            matrixWidth = project.matrixWidth,
            matrixHeight = project.matrixHeight,
            frames = project.frames.map { frame ->
                FrameExportData(
                    id = frame.id,
                    durationMs = frame.durationMs,
                    name = frame.name,
                    pixels = frame.matrix.pixels.map { it.toList() }
                )
            },
            loopCount = project.loopCount,
            createdAt = project.createdAt,
            modifiedAt = project.modifiedAt
        )

        file.writeText(gson.toJson(projectData))
        return ExportResult.Success(file.absolutePath, ExportFormat.PROJECT_JSON)
    }

    /**
     * Exports to GDK format.
     */
    private fun exportGdk(
        project: AnimationProject,
        gdkFormat: GdkExportFormat,
        fileName: String?
    ): ExportResult {
        val frames = project.frames.map { it.matrix.pixels }
        val payload = gdkExporter.createPayload(
            name = project.name,
            frames = frames,
            width = project.matrixWidth,
            height = project.matrixHeight,
            frameDurationMs = project.frames.firstOrNull()?.durationMs 
                ?: FrameData.DEFAULT_DURATION_MS,
            loopCount = project.loopCount
        )

        return when (val result = gdkExporter.exportToFile(payload, gdkFormat, fileName)) {
            is GdkExportResult.Success -> {
                val format = if (gdkFormat == GdkExportFormat.JSON) {
                    ExportFormat.GDK_JSON
                } else {
                    ExportFormat.GDK_BINARY
                }
                ExportResult.Success(result.filePath, format)
            }
            is GdkExportResult.Error -> {
                ExportResult.Error(result.message, result.exception)
            }
        }
    }

    /**
     * Exports as PNG sprite sheet.
     */
    private fun exportSpriteSheet(project: AnimationProject, fileName: String?): ExportResult {
        val exportDir = getExportDirectory()
        val name = fileName ?: sanitizeFileName(project.name)
        val file = File(exportDir, "$name$SPRITE_EXTENSION")

        val frameCount = project.frames.size
        val pixelSize = 10 // Each matrix pixel = 10x10 in output

        val spriteWidth = project.matrixWidth * pixelSize
        val spriteHeight = project.matrixHeight * pixelSize * frameCount

        val bitmap = Bitmap.createBitmap(spriteWidth, spriteHeight, Bitmap.Config.ARGB_8888)

        for ((frameIndex, frame) in project.frames.withIndex()) {
            val yOffset = frameIndex * project.matrixHeight * pixelSize
            
            for (y in 0 until project.matrixHeight) {
                for (x in 0 until project.matrixWidth) {
                    val brightness = frame.matrix.getPixel(x, y)
                    val color = if (brightness > 0) {
                        // White with varying alpha based on brightness
                        (brightness shl 24) or 0xFFFFFF
                    } else {
                        // Black background
                        0xFF000000.toInt()
                    }

                    // Fill the pixel block
                    for (py in 0 until pixelSize) {
                        for (px in 0 until pixelSize) {
                            bitmap.setPixel(
                                x * pixelSize + px,
                                yOffset + y * pixelSize + py,
                                color
                            )
                        }
                    }
                }
            }
        }

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        return ExportResult.Success(file.absolutePath, ExportFormat.PNG_SPRITE_SHEET)
    }

    /**
     * Imports a project from JSON file.
     *
     * @param filePath Path to the JSON file
     * @return Imported AnimationProject or null on failure
     */
    fun importProject(filePath: String): AnimationProject? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            val data = gson.fromJson(file.readText(), ProjectExportData::class.java)

            AnimationProject(
                id = data.id,
                name = data.name,
                description = data.description,
                matrixWidth = data.matrixWidth,
                matrixHeight = data.matrixHeight,
                frames = data.frames.map { frameData ->
                    FrameData(
                        id = frameData.id,
                        durationMs = frameData.durationMs,
                        name = frameData.name,
                        matrix = com.glyphmatrix.toyeditor.data.models.MatrixModel(
                            width = data.matrixWidth,
                            height = data.matrixHeight,
                            pixels = frameData.pixels.map { it.toIntArray() }.toTypedArray()
                        )
                    )
                },
                loopCount = data.loopCount,
                createdAt = data.createdAt,
                modifiedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lists all exported files.
     *
     * @return List of exported file paths
     */
    fun listExports(): List<File> {
        val exportDir = getExportDirectory()
        return if (exportDir.exists()) {
            exportDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Deletes an exported file.
     *
     * @param filePath Path to the file to delete
     * @return true if deletion was successful
     */
    fun deleteExport(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.delete()
    }

    private fun getExportDirectory(): File {
        val dir = File(context.getExternalFilesDir(null), EXPORT_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(50)
            .ifBlank { "animation" }
    }
}

/**
 * Data class for project JSON export.
 */
private data class ProjectExportData(
    val id: String,
    val name: String,
    val description: String,
    val matrixWidth: Int,
    val matrixHeight: Int,
    val frames: List<FrameExportData>,
    val loopCount: Int,
    val createdAt: Long,
    val modifiedAt: Long
)

/**
 * Data class for frame JSON export.
 */
private data class FrameExportData(
    val id: String,
    val durationMs: Long,
    val name: String,
    val pixels: List<List<Int>>
)
