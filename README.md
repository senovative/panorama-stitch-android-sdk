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
    implementation("com.github.USERNAME:panorama-stitch-android-sdk:Tag")
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

## Requirements
- Minimum SDK: 24 (or as defined in your setup)
- Jetpack Compose
- OpenCV (Included in the SDK dependencies)

## Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
