# Nothing GDK SDK Placeholder

This directory is for the Nothing Glyph Matrix SDK.

## Setup Instructions

1. Download the GlyphMatrixSDK.aar from the official Nothing Developer Kit:
   https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit

2. Place the downloaded .aar file in this directory (app/libs/)

3. Uncomment the SDK dependency in app/build.gradle.kts:
   ```kotlin
   implementation(files("libs/GlyphMatrixSDK.aar"))
   ```

4. Sync the Gradle project

## Note

The SDK is not included in this repository to comply with Nothing's distribution terms.
You must download it directly from the official source.
