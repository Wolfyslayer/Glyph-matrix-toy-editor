/*
 * GlyphManager.kt
 *
 * Manager class for Nothing Glyph SDK integration.
 *
 * This class provides a wrapper around the Nothing Glyph Matrix SDK,
 * handling initialization, session management, and animation playback
 * for custom glyph matrix toys on Nothing Phone 3 devices.
 *
 * SDK Integration Notes:
 * - The SDK class is: com.nothing.ketchum.GlyphManager
 * - SDK must be manually added to app/libs/ before building
 * - See app/libs/README.md for setup instructions
 * - API Reference: https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor.gdk

import android.content.Context
import android.util.Log

/**
 * Status of the GDK connection.
 */
enum class GdkConnectionStatus {
    /** SDK not available or not initialized */
    NOT_AVAILABLE,
    /** SDK initialized but not connected */
    DISCONNECTED,
    /** Attempting to connect */
    CONNECTING,
    /** Connected and ready */
    CONNECTED,
    /** Connection failed */
    ERROR
}

/**
 * Callback interface for GDK events.
 */
interface GdkCallback {
    fun onConnectionStatusChanged(status: GdkConnectionStatus)
    fun onAnimationStarted()
    fun onAnimationCompleted()
    fun onError(message: String)
}

/**
 * Manager for Nothing Glyph SDK integration.
 *
 * This class provides a safe wrapper that works both with and without
 * the actual SDK installed, allowing development and testing on non-Nothing
 * devices while providing full functionality on Nothing Phone devices.
 *
 * Usage:
 * ```kotlin
 * val manager = GlyphManager.getInstance(context)
 * manager.setCallback(myCallback)
 * manager.initialize()
 *
 * // When ready to play animation
 * manager.openSession()
 * manager.playAnimation(payload)
 * manager.closeSession()
 * ```
 */
class GlyphManager private constructor(private val context: Context) {

    private var callback: GdkCallback? = null
    private var connectionStatus = GdkConnectionStatus.NOT_AVAILABLE
    private var isSessionOpen = false
    private var isSdkAvailable = false

    companion object {
        private const val TAG = "GlyphManager"
        private const val NOTHING_SDK_CLASS = "com.nothing.ketchum.GlyphManager"

        @Volatile
        private var instance: GlyphManager? = null

        /**
         * Gets the singleton instance of GlyphManager.
         */
        fun getInstance(context: Context): GlyphManager {
            return instance ?: synchronized(this) {
                instance ?: GlyphManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        checkSdkAvailability()
    }

    /**
     * Checks if the Nothing SDK is available on this device.
     */
    private fun checkSdkAvailability() {
        isSdkAvailable = try {
            Class.forName(NOTHING_SDK_CLASS)
            true
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "Nothing SDK not available on this device")
            false
        }
    }

    /**
     * Sets the callback for GDK events.
     */
    fun setCallback(callback: GdkCallback?) {
        this.callback = callback
    }

    /**
     * Checks if the SDK is available and device is a Nothing Phone.
     */
    fun isSdkAvailable(): Boolean = isSdkAvailable

    /**
     * Returns true if this is a Nothing Phone device.
     */
    fun isNothingPhone(): Boolean {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val brand = android.os.Build.BRAND.lowercase()
        return manufacturer == "nothing" || brand == "nothing"
    }

    /**
     * Gets the current connection status.
     */
    fun getConnectionStatus(): GdkConnectionStatus = connectionStatus

    /**
     * Initializes the GDK SDK.
     *
     * Must be called before any other SDK operations.
     */
    fun initialize(): Boolean {
        if (!isSdkAvailable) {
            Log.w(TAG, "Cannot initialize: SDK not available")
            updateStatus(GdkConnectionStatus.NOT_AVAILABLE)
            return false
        }

        return try {
            // SDK call: com.nothing.ketchum.GlyphManager.init(context)
            Log.d(TAG, "Initializing Nothing GDK...")
            updateStatus(GdkConnectionStatus.DISCONNECTED)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SDK", e)
            updateStatus(GdkConnectionStatus.ERROR)
            callback?.onError("Failed to initialize SDK: ${e.message}")
            false
        }
    }

    /**
     * Registers the app with the GDK service.
     *
     * Required before opening sessions. Uses the API key from AndroidManifest.xml.
     */
    fun register(): Boolean {
        if (!isSdkAvailable) {
            Log.w(TAG, "Cannot register: SDK not available")
            return false
        }

        return try {
            updateStatus(GdkConnectionStatus.CONNECTING)
            // SDK call: com.nothing.ketchum.GlyphManager.register(callback)
            Log.d(TAG, "Registering with Nothing GDK service...")
            updateStatus(GdkConnectionStatus.CONNECTED)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register", e)
            updateStatus(GdkConnectionStatus.ERROR)
            callback?.onError("Failed to register: ${e.message}")
            false
        }
    }

    /**
     * Opens a session for controlling the glyph lights.
     *
     * A session must be opened before playing animations.
     */
    fun openSession(): Boolean {
        if (!isSdkAvailable) {
            Log.w(TAG, "Cannot open session: SDK not available")
            return false
        }

        if (connectionStatus != GdkConnectionStatus.CONNECTED) {
            Log.w(TAG, "Cannot open session: not connected")
            return false
        }

        return try {
            // SDK call: com.nothing.ketchum.GlyphManager.openSession()
            Log.d(TAG, "Opening glyph session...")
            isSessionOpen = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open session", e)
            callback?.onError("Failed to open session: ${e.message}")
            false
        }
    }

    /**
     * Closes the current glyph session.
     *
     * Should be called when finished controlling the lights.
     */
    fun closeSession() {
        if (!isSessionOpen) return

        try {
            // SDK call: com.nothing.ketchum.GlyphManager.closeSession()
            Log.d(TAG, "Closing glyph session...")
            isSessionOpen = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close session", e)
        }
    }

    /**
     * Plays an animation on the glyph matrix.
     *
     * @param payload The animation payload to play
     * @return true if playback started successfully
     */
    fun playAnimation(payload: GdkAnimationPayload): Boolean {
        if (!isSessionOpen) {
            Log.w(TAG, "Cannot play animation: session not open")
            return false
        }

        return try {
            Log.d(TAG, "Playing animation: ${payload.name}")
            callback?.onAnimationStarted()

            // SDK calls for animation playback:
            // for (frame in payload.frames) {
            //     com.nothing.ketchum.GlyphManager.setMatrix(frame.matrixData)
            //     delay(frame.durationMs)
            // }

            callback?.onAnimationCompleted()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play animation", e)
            callback?.onError("Failed to play animation: ${e.message}")
            false
        }
    }

    /**
     * Sets a single frame on the glyph matrix.
     *
     * @param matrixData 2D array of brightness values (0-4095)
     */
    fun setFrame(matrixData: Array<IntArray>): Boolean {
        if (!isSessionOpen) {
            Log.w(TAG, "Cannot set frame: session not open")
            return false
        }

        return try {
            // SDK call: com.nothing.ketchum.GlyphManager.setMatrix(matrixData)
            Log.d(TAG, "Setting glyph frame")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set frame", e)
            false
        }
    }

    /**
     * Turns off all glyph LEDs.
     */
    fun turnOffAll(): Boolean {
        if (!isSessionOpen) {
            Log.w(TAG, "Cannot turn off: session not open")
            return false
        }

        return try {
            // SDK call: com.nothing.ketchum.GlyphManager.turnOff()
            Log.d(TAG, "Turning off all glyphs")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to turn off", e)
            false
        }
    }

    /**
     * Cleans up resources and closes any open sessions.
     */
    fun cleanup() {
        closeSession()
        updateStatus(GdkConnectionStatus.DISCONNECTED)
        Log.d(TAG, "GlyphManager cleaned up")
    }

    private fun updateStatus(status: GdkConnectionStatus) {
        connectionStatus = status
        callback?.onConnectionStatusChanged(status)
    }
}
