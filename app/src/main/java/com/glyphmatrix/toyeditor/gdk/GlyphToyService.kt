/*
 * GlyphToyService.kt
 *
 * Service for registering custom glyph animations as system toys.
 *
 * This service enables custom glyph matrix animations created in the
 * Glyph Matrix Toy Editor to be registered with Nothing OS and appear
 * in the Glyph settings as selectable toys.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.gdk

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

/**
 * Service that registers custom glyph animations as Nothing OS toys.
 *
 * When registered in AndroidManifest.xml with the appropriate intent filter
 * and metadata, this service allows custom animations to appear in the
 * Nothing OS Glyph settings alongside official toys.
 *
 * Registration in AndroidManifest.xml:
 * ```xml
 * <service
 *     android:name=".gdk.GlyphToyService"
 *     android:exported="true"
 *     android:permission="com.nothing.ketchum.permission.ENABLE">
 *     <intent-filter>
 *         <action android:name="com.nothing.glyph.TOY_SERVICE" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="toy_name"
 *         android:value="@string/app_name" />
 *     <meta-data
 *         android:name="toy_preview"
 *         android:resource="@drawable/toy_preview" />
 * </service>
 * ```
 */
class GlyphToyService : Service() {

    private val binder = LocalBinder()
    private var isPlaying = false
    private var currentPayload: GdkAnimationPayload? = null
    private var animationThread: Thread? = null

    companion object {
        private const val TAG = "GlyphToyService"

        /** Action to start playing an animation */
        const val ACTION_PLAY = "com.glyphmatrix.toyeditor.action.PLAY"

        /** Action to stop the current animation */
        const val ACTION_STOP = "com.glyphmatrix.toyeditor.action.STOP"

        /** Extra key for animation file path */
        const val EXTRA_ANIMATION_PATH = "animation_path"
    }

    inner class LocalBinder : Binder() {
        fun getService(): GlyphToyService = this@GlyphToyService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "GlyphToyService created")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "GlyphToyService bound")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val animationPath = intent.getStringExtra(EXTRA_ANIMATION_PATH)
                if (animationPath != null) {
                    loadAndPlayAnimation(animationPath)
                }
            }
            ACTION_STOP -> {
                stopAnimation()
            }
        }
        return START_STICKY
    }

    /**
     * Loads an animation from file and starts playback.
     *
     * @param filePath Path to the exported animation file
     */
    fun loadAndPlayAnimation(filePath: String) {
        val exporter = GdkExporter(this)
        val payload = exporter.loadFromJson(filePath)

        if (payload != null) {
            playAnimation(payload)
        } else {
            Log.e(TAG, "Failed to load animation from: $filePath")
        }
    }

    /**
     * Starts playing an animation.
     *
     * @param payload The animation payload to play
     */
    fun playAnimation(payload: GdkAnimationPayload) {
        stopAnimation() // Stop any existing animation

        currentPayload = payload
        isPlaying = true

        animationThread = Thread {
            Log.d(TAG, "Starting animation: ${payload.name}")

            val glyphManager = GlyphManager.getInstance(this@GlyphToyService)

            if (glyphManager.isSdkAvailable()) {
                glyphManager.initialize()
                glyphManager.register()
                glyphManager.openSession()

                try {
                    var loopCount = 0
                    val maxLoops = if (payload.loopCount < 0) Int.MAX_VALUE else payload.loopCount

                    while (isPlaying && loopCount < maxLoops) {
                        for (frame in payload.frames) {
                            if (!isPlaying) break

                            glyphManager.setFrame(frame.matrixData)
                            Thread.sleep(frame.durationMs)
                        }
                        loopCount++
                    }
                } catch (e: InterruptedException) {
                    Log.d(TAG, "Animation interrupted")
                } finally {
                    glyphManager.turnOffAll()
                    glyphManager.closeSession()
                }
            } else {
                Log.w(TAG, "SDK not available - animation preview only")
                // Preview mode: just log frames
                try {
                    for (frame in payload.frames) {
                        if (!isPlaying) break
                        Log.d(TAG, "Frame ${frame.frameIndex} - duration: ${frame.durationMs}ms")
                        Thread.sleep(frame.durationMs)
                    }
                } catch (e: InterruptedException) {
                    Log.d(TAG, "Animation interrupted")
                }
            }

            Log.d(TAG, "Animation completed: ${payload.name}")
        }

        animationThread?.start()
    }

    /**
     * Stops the current animation.
     */
    fun stopAnimation() {
        isPlaying = false
        animationThread?.interrupt()
        animationThread = null
        currentPayload = null
        Log.d(TAG, "Animation stopped")
    }

    /**
     * Checks if an animation is currently playing.
     */
    fun isAnimationPlaying(): Boolean = isPlaying

    /**
     * Gets the currently playing animation payload.
     */
    fun getCurrentPayload(): GdkAnimationPayload? = currentPayload

    override fun onDestroy() {
        stopAnimation()
        Log.d(TAG, "GlyphToyService destroyed")
        super.onDestroy()
    }
}
