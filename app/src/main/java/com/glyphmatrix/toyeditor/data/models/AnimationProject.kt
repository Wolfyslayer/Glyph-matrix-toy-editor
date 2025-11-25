/*
 * AnimationProject.kt
 *
 * Data model representing an animation project.
 *
 * This data class encapsulates all information related to a single
 * animation project, including metadata, frames, settings, and references
 * to associated assets and widgets.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.data.models

import java.util.UUID

/**
 * Represents a complete animation project.
 *
 * @property id Unique identifier for this project
 * @property name Display name of the project
 * @property description Optional description
 * @property matrixWidth Width of the matrix
 * @property matrixHeight Height of the matrix
 * @property frames List of animation frames
 * @property currentFrameIndex Currently selected frame index
 * @property loopCount Number of times to loop (-1 for infinite)
 * @property createdAt Timestamp when project was created
 * @property modifiedAt Timestamp when project was last modified
 */
data class AnimationProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Project",
    val description: String = "",
    val matrixWidth: Int = MatrixModel.DEFAULT_WIDTH,
    val matrixHeight: Int = MatrixModel.DEFAULT_HEIGHT,
    val frames: List<FrameData> = listOf(FrameData.createEmpty()),
    val currentFrameIndex: Int = 0,
    val loopCount: Int = -1,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** Maximum number of frames allowed */
        const val MAX_FRAMES = 256

        /** Creates a new empty project with default settings */
        fun createNew(name: String = "Untitled Project"): AnimationProject {
            return AnimationProject(
                name = name,
                frames = listOf(FrameData.createEmpty())
            )
        }

        /** Creates a project with custom matrix dimensions */
        fun createWithSize(name: String, width: Int, height: Int): AnimationProject {
            return AnimationProject(
                name = name,
                matrixWidth = width.coerceIn(MatrixModel.MIN_DIMENSION, MatrixModel.MAX_DIMENSION),
                matrixHeight = height.coerceIn(MatrixModel.MIN_DIMENSION, MatrixModel.MAX_DIMENSION),
                frames = listOf(FrameData.createEmpty(width, height))
            )
        }
    }

    /** Gets the current frame */
    val currentFrame: FrameData
        get() = frames.getOrElse(currentFrameIndex) { frames.first() }

    /** Gets the total number of frames */
    val frameCount: Int
        get() = frames.size

    /** Gets the total animation duration in milliseconds */
    val totalDurationMs: Long
        get() = frames.sumOf { it.durationMs }

    /**
     * Creates a copy with updated current frame.
     *
     * @param frame Updated frame data
     * @return New project with updated frame
     */
    fun updateCurrentFrame(frame: FrameData): AnimationProject {
        val newFrames = frames.toMutableList()
        if (currentFrameIndex in frames.indices) {
            newFrames[currentFrameIndex] = frame
        }
        return copy(frames = newFrames, modifiedAt = System.currentTimeMillis())
    }

    /**
     * Sets a pixel on the current frame.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param value Brightness value
     * @return New project with updated pixel
     */
    fun setPixel(x: Int, y: Int, value: Int): AnimationProject {
        return updateCurrentFrame(currentFrame.setPixel(x, y, value))
    }

    /**
     * Adds a new empty frame.
     *
     * @return New project with added frame, or same project if max frames reached
     */
    fun addFrame(): AnimationProject {
        if (frames.size >= MAX_FRAMES) return this

        val newFrame = FrameData.createEmpty(matrixWidth, matrixHeight)
        val newFrames = frames + newFrame
        return copy(
            frames = newFrames,
            currentFrameIndex = newFrames.size - 1,
            modifiedAt = System.currentTimeMillis()
        )
    }

    /**
     * Duplicates the current frame.
     *
     * @return New project with duplicated frame
     */
    fun duplicateCurrentFrame(): AnimationProject {
        if (frames.size >= MAX_FRAMES) return this

        val duplicatedFrame = currentFrame.duplicate()
        val newFrames = frames.toMutableList()
        newFrames.add(currentFrameIndex + 1, duplicatedFrame)
        return copy(
            frames = newFrames,
            currentFrameIndex = currentFrameIndex + 1,
            modifiedAt = System.currentTimeMillis()
        )
    }

    /**
     * Deletes the current frame.
     *
     * @return New project with frame deleted, or same project if only one frame
     */
    fun deleteCurrentFrame(): AnimationProject {
        if (frames.size <= 1) return this

        val newFrames = frames.toMutableList()
        newFrames.removeAt(currentFrameIndex)
        val newIndex = minOf(currentFrameIndex, newFrames.size - 1)
        return copy(
            frames = newFrames,
            currentFrameIndex = newIndex,
            modifiedAt = System.currentTimeMillis()
        )
    }

    /**
     * Navigates to a specific frame.
     *
     * @param index Frame index to navigate to
     * @return New project with updated current frame index
     */
    fun goToFrame(index: Int): AnimationProject {
        val safeIndex = index.coerceIn(0, frames.size - 1)
        return if (safeIndex != currentFrameIndex) {
            copy(currentFrameIndex = safeIndex)
        } else {
            this
        }
    }

    /**
     * Navigates to the next frame.
     *
     * @param wrap Whether to wrap to first frame at the end
     * @return New project with updated current frame index
     */
    fun nextFrame(wrap: Boolean = true): AnimationProject {
        val nextIndex = if (currentFrameIndex >= frames.size - 1) {
            if (wrap) 0 else currentFrameIndex
        } else {
            currentFrameIndex + 1
        }
        return goToFrame(nextIndex)
    }

    /**
     * Navigates to the previous frame.
     *
     * @param wrap Whether to wrap to last frame at the beginning
     * @return New project with updated current frame index
     */
    fun previousFrame(wrap: Boolean = true): AnimationProject {
        val prevIndex = if (currentFrameIndex <= 0) {
            if (wrap) frames.size - 1 else currentFrameIndex
        } else {
            currentFrameIndex - 1
        }
        return goToFrame(prevIndex)
    }

    /**
     * Clears the current frame.
     *
     * @return New project with cleared current frame
     */
    fun clearCurrentFrame(): AnimationProject {
        return updateCurrentFrame(currentFrame.clear())
    }

    /**
     * Gets the frame data for onion skinning (previous/next frames for reference).
     *
     * @param lookBack Number of previous frames to include
     * @param lookAhead Number of next frames to include
     * @return List of frames for onion skinning with their offsets
     */
    fun getOnionSkinFrames(lookBack: Int = 1, lookAhead: Int = 1): List<Pair<Int, FrameData>> {
        val result = mutableListOf<Pair<Int, FrameData>>()

        // Previous frames
        for (i in lookBack downTo 1) {
            val index = currentFrameIndex - i
            if (index >= 0) {
                result.add(-i to frames[index])
            }
        }

        // Next frames
        for (i in 1..lookAhead) {
            val index = currentFrameIndex + i
            if (index < frames.size) {
                result.add(i to frames[index])
            }
        }

        return result
    }

    /**
     * Checks if the project has unsaved changes.
     *
     * @param savedTimestamp Timestamp of last save
     * @return true if there are unsaved changes
     */
    fun hasUnsavedChanges(savedTimestamp: Long): Boolean {
        return modifiedAt > savedTimestamp
    }

    /**
     * Renames the project.
     *
     * @param newName New name for the project
     * @return New project with updated name
     */
    fun rename(newName: String): AnimationProject {
        return copy(name = newName, modifiedAt = System.currentTimeMillis())
    }
}
