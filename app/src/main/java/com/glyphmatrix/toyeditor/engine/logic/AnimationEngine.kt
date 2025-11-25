/*
 * AnimationEngine.kt
 *
 * Core animation rendering engine.
 *
 * This class provides the core logic for playing, rendering, and
 * managing animations. It handles frame timing, playback state, and
 * rendering coordination.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.engine.logic

import com.glyphmatrix.toyeditor.data.models.AnimationProject
import com.glyphmatrix.toyeditor.data.models.FrameData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Animation playback state.
 */
enum class PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
}

/**
 * Animation playback mode.
 */
enum class PlaybackMode {
    /** Play once and stop */
    ONCE,
    /** Loop continuously */
    LOOP,
    /** Play forward then backward */
    PING_PONG
}

/**
 * Engine for playing and managing animations.
 *
 * Handles frame timing, playback control, and animation state.
 */
class AnimationEngine {

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.LOOP)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    private var playbackJob: Job? = null
    private var project: AnimationProject? = null
    private var isPingPongReverse = false
    private var completedLoops = 0

    private val scope = CoroutineScope(Dispatchers.Default)

    /** Callback when frame changes during playback */
    var onFrameChanged: ((Int) -> Unit)? = null

    /** Callback when playback completes (for non-loop modes) */
    var onPlaybackComplete: (() -> Unit)? = null

    /**
     * Loads a project for playback.
     *
     * @param project The animation project to load
     */
    fun loadProject(project: AnimationProject) {
        stop()
        this.project = project
        _currentFrameIndex.value = project.currentFrameIndex
        completedLoops = 0
    }

    /**
     * Starts or resumes playback.
     */
    fun play() {
        if (project == null || project!!.frames.isEmpty()) return

        when (_playbackState.value) {
            PlaybackState.STOPPED -> startPlayback()
            PlaybackState.PAUSED -> resumePlayback()
            PlaybackState.PLAYING -> { /* Already playing */ }
        }
    }

    /**
     * Pauses playback.
     */
    fun pause() {
        if (_playbackState.value == PlaybackState.PLAYING) {
            playbackJob?.cancel()
            _playbackState.value = PlaybackState.PAUSED
        }
    }

    /**
     * Stops playback and resets to the first frame.
     */
    fun stop() {
        playbackJob?.cancel()
        _playbackState.value = PlaybackState.STOPPED
        _currentFrameIndex.value = 0
        isPingPongReverse = false
        completedLoops = 0
    }

    /**
     * Toggles between play and pause.
     */
    fun togglePlayPause() {
        when (_playbackState.value) {
            PlaybackState.PLAYING -> pause()
            PlaybackState.PAUSED, PlaybackState.STOPPED -> play()
        }
    }

    /**
     * Moves to the next frame manually.
     */
    fun stepForward() {
        val proj = project ?: return
        if (_playbackState.value == PlaybackState.PLAYING) return

        val nextIndex = (_currentFrameIndex.value + 1) % proj.frames.size
        _currentFrameIndex.value = nextIndex
        onFrameChanged?.invoke(nextIndex)
    }

    /**
     * Moves to the previous frame manually.
     */
    fun stepBackward() {
        val proj = project ?: return
        if (_playbackState.value == PlaybackState.PLAYING) return

        val prevIndex = if (_currentFrameIndex.value <= 0) {
            proj.frames.size - 1
        } else {
            _currentFrameIndex.value - 1
        }
        _currentFrameIndex.value = prevIndex
        onFrameChanged?.invoke(prevIndex)
    }

    /**
     * Jumps to a specific frame.
     *
     * @param index Frame index to jump to
     */
    fun goToFrame(index: Int) {
        val proj = project ?: return
        if (_playbackState.value == PlaybackState.PLAYING) return

        val safeIndex = index.coerceIn(0, proj.frames.size - 1)
        _currentFrameIndex.value = safeIndex
        onFrameChanged?.invoke(safeIndex)
    }

    /**
     * Sets the playback mode.
     *
     * @param mode The playback mode to use
     */
    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
    }

    /**
     * Gets the current frame data.
     *
     * @return Current frame or null if no project loaded
     */
    fun getCurrentFrame(): FrameData? {
        return project?.frames?.getOrNull(_currentFrameIndex.value)
    }

    private fun startPlayback() {
        val proj = project ?: return

        _playbackState.value = PlaybackState.PLAYING
        isPingPongReverse = false
        completedLoops = 0

        playbackJob = scope.launch {
            while (isActive && _playbackState.value == PlaybackState.PLAYING) {
                val frame = proj.frames.getOrNull(_currentFrameIndex.value)
                if (frame == null) {
                    stop()
                    break
                }

                onFrameChanged?.invoke(_currentFrameIndex.value)

                delay(frame.durationMs)

                if (!isActive || _playbackState.value != PlaybackState.PLAYING) break

                advanceFrame(proj)
            }
        }
    }

    private fun resumePlayback() {
        if (_playbackState.value != PlaybackState.PAUSED) return
        startPlayback()
    }

    private fun advanceFrame(proj: AnimationProject) {
        val currentIndex = _currentFrameIndex.value
        val frameCount = proj.frames.size

        when (_playbackMode.value) {
            PlaybackMode.ONCE -> {
                if (currentIndex >= frameCount - 1) {
                    _playbackState.value = PlaybackState.STOPPED
                    onPlaybackComplete?.invoke()
                } else {
                    _currentFrameIndex.value = currentIndex + 1
                }
            }

            PlaybackMode.LOOP -> {
                _currentFrameIndex.value = (currentIndex + 1) % frameCount
                if (_currentFrameIndex.value == 0) {
                    completedLoops++
                    if (proj.loopCount > 0 && completedLoops >= proj.loopCount) {
                        _playbackState.value = PlaybackState.STOPPED
                        onPlaybackComplete?.invoke()
                    }
                }
            }

            PlaybackMode.PING_PONG -> {
                if (isPingPongReverse) {
                    if (currentIndex <= 0) {
                        isPingPongReverse = false
                        _currentFrameIndex.value = minOf(1, frameCount - 1)
                        completedLoops++
                        if (proj.loopCount > 0 && completedLoops >= proj.loopCount) {
                            _playbackState.value = PlaybackState.STOPPED
                            onPlaybackComplete?.invoke()
                        }
                    } else {
                        _currentFrameIndex.value = currentIndex - 1
                    }
                } else {
                    if (currentIndex >= frameCount - 1) {
                        isPingPongReverse = true
                        _currentFrameIndex.value = maxOf(0, frameCount - 2)
                    } else {
                        _currentFrameIndex.value = currentIndex + 1
                    }
                }
            }
        }
    }

    /**
     * Cleans up resources.
     */
    fun cleanup() {
        stop()
        project = null
        onFrameChanged = null
        onPlaybackComplete = null
    }
}
