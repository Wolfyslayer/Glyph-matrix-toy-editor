# Glyph Matrix Toy Editor

An open-source Android Jetpack Compose application for creating pixel art animations designed for glyph matrix displays, with official Nothing Glyph Developer Kit (GDK) support for Nothing Phone 3.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Android](https://img.shields.io/badge/Android-10%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-orange)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.11-blueviolet)

## Features

### ✏️ Interactive Pixel Matrix Editor
- **Paint Pixels**: Draw pixel-by-pixel with touch controls
- **Drawing Tools**: Brush, eraser, fill bucket, and color picker
- **Brightness Control**: Adjustable pixel brightness (0-255)
- **Grid Display**: Optional grid overlay for precise editing

### 🎬 Animation Timeline
- **Multi-Frame Animations**: Create frame-by-frame animations
- **Timeline Controls**: Play, pause, stop, step forward/backward
- **Frame Management**: Add, duplicate, delete, and reorder frames
- **Onion Skinning**: See previous/next frames as reference while drawing
- **Variable Frame Duration**: Set custom timing per frame

### ↩️ Undo/Redo
- Full undo/redo support with 50-level history
- Preserves all editing operations

### 🔧 Pixel-Perfect Widgets
- **Pixel Clock**: Display time in 12h/24h format with optional seconds
- **Pixel Battery**: Battery indicator with percentage display
- **Charging Animation**: Animated pulse effect when charging
- **Low Battery Warning**: Flashing animation for low battery

### 💾 Project Management
- Create, save, and load animation projects
- Rename and delete projects
- Project metadata with timestamps
- Custom matrix dimensions (4×4 to 64×64)

### 📤 Export Options
- **GDK JSON Format**: For manual transfer to Nothing Phone
- **GDK Binary Format**: Optimized format for device storage
- **Project JSON**: Full project export for backup/sharing
- **PNG Sprite Sheet**: Visual export for sharing

### 📱 Nothing Phone 3 Integration
- **GDK SDK Support**: Direct integration with Nothing's Glyph Matrix SDK
- **Glyph Toy Registration**: Register animations as system toys
- **Preview Mode**: Test animations before exporting
- **Export Guide**: Step-by-step instructions for manual integration

### 🎓 Onboarding & Help
- Interactive tutorial for new users
- In-app help accessible from settings

### ⚙️ Settings
- Default matrix dimensions
- Default frame duration
- Grid visibility toggle
- Auto-save preference
- Haptic feedback toggle

## Screenshots

*The app features a dark-themed interface optimized for pixel art creation with a canvas area, toolbar, and timeline.*

## Requirements

- Android 10+ (API level 29+)
- Android Studio Ladybug or later
- Kotlin 2.1.0+
- Jetpack Compose 2024.11+
- Nothing Phone 3 (for Glyph Matrix features, optional)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Wolfyslayer/Glyph-matrix-toy-editor.git
cd Glyph-matrix-toy-editor
```

### 2. Open in Android Studio

Open the project in Android Studio and sync Gradle.

### 3. Build and Run

Build and run on an Android device or emulator.

### 4. (Optional) Add GDK SDK for Nothing Phone Integration

1. Download `GlyphMatrixSDK.aar` from the [official GDK repository](https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit)
2. Place it in `app/libs/`
3. Uncomment the SDK dependency in `app/build.gradle.kts`:
   ```kotlin
   implementation(files("libs/GlyphMatrixSDK.aar"))
   ```
4. Sync Gradle

## Nothing Phone 3 Integration

### GDK Setup

1. **Download the SDK**: Get the GlyphMatrixSDK.aar from the [official GDK repository](https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit)

2. **Add to Project**: Place the .aar file in `app/libs/` and uncomment the dependency

3. **Get API Key**: For production builds, register at [Nothing Developer Kit](https://nothing.tech/pages/glyph-developer-kit) and replace the API key in `AndroidManifest.xml`

4. **Enable Debug Mode**: For development testing, enable Glyph debugging via ADB:
   ```bash
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
   Note: Debug mode automatically disables after 48 hours.

### Manual Export (Non-Nothing Devices)

If you're developing on a non-Nothing device:

1. Export your animation using the in-app Export Guide
2. Transfer the exported `.glyph.json` file to your Nothing Phone
3. Use a GDK-compatible app to load and activate the animation
4. Your custom toy will appear in Settings > Glyph Interface > Glyph Toys

### Developer Resources

- [Glyph Matrix Developer Kit](https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit) - Official SDK and documentation
- [Glyph Developer Kit](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit) - Base GDK for all Nothing Phones
- [Nothing Developer Portal](https://nothing.tech/pages/glyph-developer-kit) - API key registration and resources
- [Nothing Community](https://nothing.community) - Developer community and support

## Project Structure

```
app/
├── libs/
│   └── README.md                      # GDK SDK setup instructions
├── proguard-rules.pro                 # ProGuard configuration
└── src/
    └── main/
        ├── java/com/glyphmatrix/toyeditor/
        │   ├── MainActivity.kt           # Main entry with navigation
        │   ├── gdk/
        │   │   ├── GdkExporter.kt         # GDK payload export
        │   │   ├── GlyphManager.kt        # GDK SDK wrapper
        │   │   └── GlyphToyService.kt     # Glyph Toy service
        │   ├── ui/
        │   │   ├── EditorScreen.kt        # Main editor UI
        │   │   ├── ExportGuideScreen.kt   # Export guide UI
        │   │   ├── MatrixCanvas.kt        # Pixel art canvas
        │   │   ├── OnboardingScreen.kt    # Tutorial/help
        │   │   ├── ProjectsScreen.kt      # Project management
        │   │   ├── SettingsScreen.kt      # App settings
        │   │   ├── TimelineBar.kt         # Animation timeline
        │   │   └── WidgetPanel.kt         # Widget selection
        │   ├── data/models/
        │   │   ├── AnimationProject.kt    # Project data model
        │   │   ├── FrameData.kt           # Frame data model
        │   │   ├── MatrixModel.kt         # Matrix data model
        │   │   └── WidgetAsset.kt         # Widget data model
        │   ├── widgets/
        │   │   ├── PixelBattery.kt        # Battery widget
        │   │   └── PixelClock.kt          # Clock widget
        │   └── engine/logic/
        │       ├── AnimationEngine.kt     # Playback engine
        │       └── Exporter.kt            # Export functionality
        ├── res/
        │   ├── drawable/                  # Vector drawables
        │   ├── mipmap-anydpi-v26/         # Adaptive icons
        │   └── values/                    # Resources
        └── AndroidManifest.xml            # App manifest with GDK config
```

## Architecture

The app follows a modular architecture with clear separation of concerns:

- **UI Layer**: Jetpack Compose composables for all screens
- **Data Layer**: Immutable data models with copy-on-write semantics
- **Engine Layer**: Animation playback and export functionality
- **GDK Layer**: Nothing SDK integration and service registration

### Key Design Decisions

- **Immutable State**: All data models are immutable for predictable state management
- **Undo/Redo Stack**: 50-level history with efficient state snapshots
- **Coroutine-based Playback**: Non-blocking animation engine
- **Modular Widgets**: Self-contained widget implementations

## Usage

### Creating an Animation

1. **New Project**: Tap the menu icon and select "Projects", then tap + to create a new project
2. **Draw**: Use the brush tool to paint pixels on the canvas
3. **Add Frames**: Use the + button in the timeline to add frames
4. **Preview**: Press the play button to preview your animation
5. **Export**: Tap the share icon to export your animation

### Using Widgets

1. Tap the widgets icon in the toolbar
2. Select a widget (Clock or Battery)
3. Tap on the canvas to place the widget
4. Widget pixels are rendered at maximum brightness

### Drawing Tools

| Tool | Description |
|------|-------------|
| 🖌️ Brush | Draw pixels at current brightness |
| ❌ Eraser | Remove pixels (set to 0) |
| 🪣 Fill | Flood fill connected area |
| 🎨 Picker | Pick brightness from existing pixel |

### Keyboard Shortcuts (When connected)

| Shortcut | Action |
|----------|--------|
| Ctrl+Z | Undo |
| Ctrl+Y | Redo |
| Space | Play/Pause |

## Building

### Debug Build

```bash
./gradlew assembleDebug
```

### Release Build

```bash
./gradlew assembleRelease
```

### Run Tests

```bash
./gradlew test
```

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

### Guidelines

1. Follow the existing code style
2. Add comments for complex logic
3. Update documentation as needed
4. Test on multiple screen sizes

## Copyright Policy

All content in this repository is original. No proprietary or third-party content is included. All code, assets, and documentation are created specifically for this project and are available under the MIT License.

This app uses the **official Nothing Glyph Developer Kit (GDK)** following Nothing's public API guidelines. The GDK SDK itself is not included in this repository - it must be downloaded separately from the official Nothing Developer Programme repositories.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Glyph Matrix Toy Editor Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contact

For questions or suggestions, please open an issue on this repository.