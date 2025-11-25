# Glyph Matrix Toy Editor

An open-source Android Jetpack Compose application for creating pixel art animations designed for glyph matrix displays, with official Nothing Glyph Developer Kit (GDK) support for Nothing Phone 3.

## Project Purpose

Glyph Matrix Toy Editor is a pixel art animation editor that allows users to create, edit, and export animations for glyph matrix displays. The app provides an intuitive interface for designing frame-by-frame animations with support for various widgets and export options, including direct integration with Nothing Phone 3's Glyph Matrix display.

## Nothing Phone 3 Integration

This app supports the official **Nothing Glyph Developer Kit (GDK)** for exporting and activating custom glyph matrix animations on Nothing Phone 3 devices.

### Features

- **Export to GDK Format**: Convert your animations to GDK-compatible payloads (JSON or binary)
- **Direct Activation**: Register animations as system Glyph Toys on Nothing Phone 3
- **Preview Mode**: Test animations in the editor before exporting
- **Manual Export Guide**: Step-by-step instructions for manual integration

### GDK Setup

1. **Download the SDK**: Get the GlyphMatrixSDK.aar from the [official GDK repository](https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit)

2. **Add to Project**: Place the .aar file in `app/libs/` and uncomment the dependency in `app/build.gradle.kts`:
   ```kotlin
   implementation(files("libs/GlyphMatrixSDK.aar"))
   ```

3. **Get API Key**: For production builds, register at [Nothing Developer Kit](https://nothing.tech/pages/glyph-developer-kit) and replace the API key in `AndroidManifest.xml`

4. **Enable Debug Mode**: For development testing, enable Glyph debugging via ADB:
   ```bash
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
   Note: Debug mode automatically disables after 48 hours.

### Developer Resources

- [Glyph Matrix Developer Kit](https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit) - Official SDK and documentation
- [Glyph Developer Kit](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit) - Base GDK for all Nothing Phones
- [Nothing Developer Portal](https://nothing.tech/pages/glyph-developer-kit) - API key registration and resources
- [Nothing Community](https://nothing.community) - Developer community and support

### Manual Export (Non-Nothing Devices)

If you're developing on a non-Nothing device:

1. Export your animation using the in-app Export Guide
2. Transfer the exported `.glyph.json` file to your Nothing Phone
3. Use a GDK-compatible app to load and activate the animation
4. Your custom toy will appear in Settings > Glyph Interface > Glyph Toys

## Project Structure

```
app/
├── libs/
│   └── README.md                      # GDK SDK setup instructions
└── src/
    └── main/
        ├── java/com/glyphmatrix/toyeditor/
        │   ├── MainActivity.kt           # Main entry point of the application
        │   ├── gdk/
        │   │   ├── GdkExporter.kt         # GDK payload export functionality
        │   │   ├── GlyphManager.kt        # GDK SDK wrapper and manager
        │   │   └── GlyphToyService.kt     # Glyph Toy service registration
        │   ├── ui/
        │   │   ├── EditorScreen.kt        # Main editor screen composable
        │   │   ├── ExportGuideScreen.kt   # Export and integration guide UI
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
        │   └── values/                    # Value resources (strings, colors, themes)
        └── AndroidManifest.xml            # Android application manifest with GDK config
```

## Features (Planned)

- Pixel art drawing canvas with customizable matrix dimensions
- Frame-by-frame animation timeline
- Built-in widgets (clock, battery indicators)
- Animation preview and playback
- Export to various formats including GDK-compatible payloads
- Direct Nothing Phone 3 Glyph Matrix integration

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 29+ (Android 10+)
- Kotlin 1.9+
- Jetpack Compose
- Nothing Phone 3 (for Glyph Matrix features)
- Nothing GDK SDK (optional, for direct device integration)

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. (Optional) Add the GDK SDK to `app/libs/` for Nothing Phone integration
4. Build and run on an Android device or emulator

## Copyright Policy

All content in this repository is original and generic. No proprietary or third-party content is included. All code, assets, and documentation are created specifically for this project and are available under the MIT License.

This app uses the **official Nothing Glyph Developer Kit (GDK)** following Nothing's public API guidelines. The GDK SDK itself is not included in this repository - it must be downloaded separately from the official Nothing Developer Programme repositories.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

## Contact

For questions or suggestions, please open an issue on this repository.