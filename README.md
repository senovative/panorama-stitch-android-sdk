# Panorama Stitch Android SDK

A powerful Android SDK that provides an interactive camera interface for stitching panorama photos in real-time. Built with Jetpack Compose, CameraX, and OpenCV.

## Features
- Real-time panorama stitching.
- Built-in CameraX preview and image capture.
- Intuitive, customizable Jetpack Compose UI.
- Simple drop-in integration.

## Installation

You can install this SDK via [JitPack](https://jitpack.io).

### 1. Add JitPack repository
In your root `settings.gradle.kts` (or `build.gradle.kts` if using older Gradle), add the JitPack repository:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // <-- Add this line
    }
}
```

### 2. Add the dependency
In your app-level `build.gradle.kts`, add the following dependency. Replace `Tag` with the latest release version or commit hash:

```kotlin
dependencies {
    implementation("com.github.senovative:panorama-stitch-android-sdk:<version>")
}
```
*(Note: Replace `USERNAME` with your GitHub username once the repository is pushed).*

## Usage

Using the SDK is simple. It provides a pre-built Compose screen that handles permissions, camera preview, and the panorama capture process out-of-the-box.

### Launching the Panorama Screen
You can launch the panorama screen in your Compose activity:

```kotlin
import io.senovative.panorama.sdk.ui.screen.PanoramaCameraScreen
import io.senovative.panorama.sdk.PanoramaRenderResult

// Inside your Compose hierarchy:
PanoramaCameraScreen(
    onResult = { result ->
        when (result) {
            is PanoramaRenderResult.Success -> {
                val stitchedBitmap = result.bitmap
                // Handle the successful panorama bitmap (e.g., show it to the user)
            }
            is PanoramaRenderResult.Error -> {
                val exception = result.exception
                // Handle the error
            }
            PanoramaRenderResult.Cancelled -> {
                // The user closed the camera without finishing
            }
        }
    }
)
```

For a complete working example, please check the `:sample` module in this repository.

## Tech Stack & Requirements
- **Language**: Kotlin 2.4.0
- **Build System**: Gradle with AGP 9.2.0
- **JDK Version**: Java 17
- **Android SDK**: 
  - Minimum SDK: 24
  - Target / Compile SDK: 37
- **Core Libraries**:
  - Jetpack Compose (BOM 2026.05.01)
  - CameraX (1.6.1)
  - OpenCV (4.13.0 - Included via dependencies)

## Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
