# Glyph Matrix Toy Editor

An open-source Android Jetpack Compose application for creating pixel art animations designed for glyph matrix displays.

## Project Purpose

Glyph Matrix Toy Editor is a pixel art animation editor that allows users to create, edit, and export animations for glyph matrix displays. The app provides an intuitive interface for designing frame-by-frame animations with support for various widgets and export options.

## Project Structure

```
app/
└── src/
    └── main/
        ├── java/com/glyphmatrix/toyeditor/
        │   ├── MainActivity.kt           # Main entry point of the application
        │   ├── ui/
        │   │   ├── EditorScreen.kt        # Main editor screen composable
        │   │   ├── WidgetPanel.kt         # Panel for widget selection and configuration
        │   │   ├── MatrixCanvas.kt        # Canvas for pixel art drawing
        │   │   └── TimelineBar.kt         # Animation timeline control bar
        │   ├── data/models/
        │   │   ├── AnimationProject.kt    # Data model for animation projects
        │   │   ├── MatrixModel.kt         # Data model for matrix configurations
        │   │   ├── WidgetAsset.kt         # Data model for widget assets
        │   │   └── FrameData.kt           # Data model for individual animation frames
        │   ├── widgets/
        │   │   ├── PixelClock.kt          # Pixel clock widget implementation
        │   │   └── PixelBattery.kt        # Pixel battery widget implementation
        │   └── engine/logic/
        │       ├── AnimationEngine.kt     # Core animation rendering engine
        │       └── Exporter.kt            # Export functionality for animations
        ├── res/
        │   ├── layout/                    # XML layout resources
        │   ├── drawable/                  # Drawable resources
        │   └── values/                    # Value resources (strings, colors, etc.)
        └── AndroidManifest.xml            # Android application manifest
```

## Features (Planned)

- Pixel art drawing canvas with customizable matrix dimensions
- Frame-by-frame animation timeline
- Built-in widgets (clock, battery indicators)
- Animation preview and playback
- Export to various formats

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 21+
- Kotlin 1.8+
- Jetpack Compose

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Build and run on an Android device or emulator

## Copyright Policy

All content in this repository is original and generic. No proprietary or third-party content is included. All code, assets, and documentation are created specifically for this project and are available under the MIT License.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

## Contact

For questions or suggestions, please open an issue on this repository.