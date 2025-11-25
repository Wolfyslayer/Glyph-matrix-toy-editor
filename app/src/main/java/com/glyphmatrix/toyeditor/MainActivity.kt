/*
 * MainActivity.kt
 *
 * Main entry point of the Glyph Matrix Toy Editor application.
 *
 * This activity serves as the primary entry point for the Android application,
 * hosting the Jetpack Compose UI and initializing the application's core components.
 * It handles the app lifecycle, navigation setup, and coordination between
 * the editor screen and other UI components.
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

package com.glyphmatrix.toyeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.glyphmatrix.toyeditor.data.models.AnimationProject
import com.glyphmatrix.toyeditor.engine.logic.AnimationEngine
import com.glyphmatrix.toyeditor.engine.logic.PlaybackState
import com.glyphmatrix.toyeditor.gdk.GlyphManager
import com.glyphmatrix.toyeditor.nothingphone.GlyphEditorState
import com.glyphmatrix.toyeditor.nothingphone.GlyphMatrixState
import com.glyphmatrix.toyeditor.nothingphone.NothingGlyphMatrixEditor
import com.glyphmatrix.toyeditor.ui.AppSettings
import com.glyphmatrix.toyeditor.ui.EditorScreen
import com.glyphmatrix.toyeditor.ui.EditorState
import com.glyphmatrix.toyeditor.ui.ExportGuideScreen
import com.glyphmatrix.toyeditor.ui.HelpScreen
import com.glyphmatrix.toyeditor.ui.OnboardingScreen
import com.glyphmatrix.toyeditor.ui.ProjectInfo
import com.glyphmatrix.toyeditor.ui.ProjectsScreen
import com.glyphmatrix.toyeditor.ui.SettingsScreen

/**
 * Navigation route constants.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val PROJECTS = "projects"
    const val EDITOR = "editor"
    const val GLYPH_MATRIX_EDITOR = "glyph_matrix_editor"
    const val EXPORT = "export"
    const val SETTINGS = "settings"
    const val HELP = "help"
}

/**
 * Custom dark color scheme for the Glyph Matrix editor.
 */
private val GlyphDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFBBBBBB),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2D2D2D),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFF5722),
    onTertiary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFBBBBBB),
    error = Color(0xFFFF5252)
)

/**
 * Custom light color scheme for the Glyph Matrix editor.
 */
private val GlyphLightColorScheme = lightColorScheme(
    primary = Color(0xFF1C1C1C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF424242),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFFF5722),
    onTertiary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF424242),
    error = Color(0xFFD32F2F)
)

/**
 * Main activity for the Glyph Matrix Toy Editor.
 */
class MainActivity : ComponentActivity() {

    private lateinit var glyphManager: GlyphManager
    private val animationEngine = AnimationEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize GDK manager
        glyphManager = GlyphManager.getInstance(this)
        glyphManager.initialize()

        enableEdgeToEdge()

        setContent {
            GlyphMatrixApp(
                glyphManager = glyphManager,
                animationEngine = animationEngine
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        animationEngine.cleanup()
        glyphManager.cleanup()
    }
}

/**
 * Main composable for the Glyph Matrix Toy Editor app.
 */
@Composable
fun GlyphMatrixApp(
    glyphManager: GlyphManager,
    animationEngine: AnimationEngine
) {
    val navController = rememberNavController()

    // App state
    var hasSeenOnboarding by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(AppSettings()) }

    // Projects state (in a real app, this would be persisted)
    var projects by remember { mutableStateOf(listOf<AnimationProject>()) }
    var currentProjectId by remember { mutableStateOf<String?>(null) }

    // Editor state
    var editorState by remember {
        mutableStateOf(EditorState(project = AnimationProject.createNew()))
    }
    var playbackState by remember { mutableStateOf(PlaybackState.STOPPED) }

    // Nothing Phone 3 Glyph Matrix editor state
    var glyphEditorState by remember {
        mutableStateOf(GlyphEditorState())
    }

    // GDK status
    val isNothingPhone = glyphManager.isNothingPhone()
    val isSdkAvailable = glyphManager.isSdkAvailable()

    // Theme
    val colorScheme = if (isSystemInDarkTheme() || settings.darkMode) {
        GlyphDarkColorScheme
    } else {
        GlyphLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = if (hasSeenOnboarding) Routes.EDITOR else Routes.ONBOARDING
            ) {
                // Onboarding screen
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onComplete = {
                            hasSeenOnboarding = true
                            navController.navigate(Routes.EDITOR) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                // Projects screen
                composable(Routes.PROJECTS) {
                    ProjectsScreen(
                        projects = projects.map { project ->
                            ProjectInfo(
                                id = project.id,
                                name = project.name,
                                matrixWidth = project.matrixWidth,
                                matrixHeight = project.matrixHeight,
                                frameCount = project.frameCount,
                                modifiedAt = project.modifiedAt
                            )
                        },
                        onProjectSelect = { projectId ->
                            val project = projects.find { it.id == projectId }
                            if (project != null) {
                                currentProjectId = projectId
                                editorState = EditorState(project = project)
                                navController.navigate(Routes.EDITOR)
                            }
                        },
                        onProjectDelete = { projectId ->
                            projects = projects.filter { it.id != projectId }
                            if (currentProjectId == projectId) {
                                currentProjectId = null
                            }
                        },
                        onProjectRename = { projectId, newName ->
                            projects = projects.map {
                                if (it.id == projectId) it.rename(newName) else it
                            }
                            if (currentProjectId == projectId) {
                                editorState = editorState.copy(
                                    project = editorState.project.rename(newName)
                                )
                            }
                        },
                        onCreateProject = { name, width, height ->
                            val newProject = AnimationProject.createWithSize(name, width, height)
                            projects = projects + newProject
                            currentProjectId = newProject.id
                            editorState = EditorState(project = newProject)
                            navController.navigate(Routes.EDITOR)
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // Editor screen
                composable(Routes.EDITOR) {
                    EditorScreen(
                        editorState = editorState,
                        playbackState = playbackState,
                        onEditorStateChange = { newState ->
                            editorState = newState
                        },
                        onPlayPause = {
                            when (playbackState) {
                                PlaybackState.STOPPED, PlaybackState.PAUSED -> {
                                    animationEngine.loadProject(editorState.project)
                                    animationEngine.onFrameChanged = { frameIndex ->
                                        editorState = editorState.copy(
                                            project = editorState.project.goToFrame(frameIndex)
                                        )
                                    }
                                    animationEngine.play()
                                    playbackState = PlaybackState.PLAYING
                                }
                                PlaybackState.PLAYING -> {
                                    animationEngine.pause()
                                    playbackState = PlaybackState.PAUSED
                                }
                            }
                        },
                        onStop = {
                            animationEngine.stop()
                            playbackState = PlaybackState.STOPPED
                            editorState = editorState.copy(
                                project = editorState.project.goToFrame(0)
                            )
                        },
                        onNavigateToExport = {
                            navController.navigate(Routes.EXPORT)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Routes.SETTINGS)
                        },
                        onNavigateToProjects = {
                            navController.navigate(Routes.PROJECTS)
                        },
                        onNavigateToGlyphMatrix = {
                            navController.navigate(Routes.GLYPH_MATRIX_EDITOR)
                        },
                        onSaveProject = {
                            // Update the project in the projects list
                            val updatedProject = editorState.project
                            currentProjectId = updatedProject.id

                            projects = if (projects.any { it.id == updatedProject.id }) {
                                projects.map {
                                    if (it.id == updatedProject.id) updatedProject else it
                                }
                            } else {
                                projects + updatedProject
                            }
                        }
                    )
                }

                // Export screen
                composable(Routes.EXPORT) {
                    var isExporting by remember { mutableStateOf(false) }
                    var exportedFilePath by remember { mutableStateOf<String?>(null) }

                    ExportGuideScreen(
                        onExportClick = {
                            isExporting = true
                            // In a real implementation, this would call the exporter
                            // For now, simulate export completion
                            isExporting = false
                            exportedFilePath = "/storage/emulated/0/GlyphExports/${editorState.project.name}.glyph.json"
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        isNothingDevice = isNothingPhone,
                        isSdkAvailable = isSdkAvailable,
                        exportedFilePath = exportedFilePath,
                        isExporting = isExporting
                    )
                }

                // Nothing Phone 3 Glyph Matrix Editor
                composable(Routes.GLYPH_MATRIX_EDITOR) {
                    NothingGlyphMatrixEditor(
                        editorState = glyphEditorState,
                        onEditorStateChange = { newState ->
                            glyphEditorState = newState
                        },
                        onExport = { matrixState ->
                            // Export the matrix state
                            // In a real implementation, this would serialize the state
                            val exportData = matrixState.toExportMap()
                            // Handle export...
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // Settings screen
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        settings = settings,
                        onSettingsChange = { newSettings ->
                            settings = newSettings
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToHelp = {
                            navController.navigate(Routes.HELP)
                        },
                        isNothingPhone = isNothingPhone,
                        isSdkAvailable = isSdkAvailable
                    )
                }

                // Help screen
                composable(Routes.HELP) {
                    HelpScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
