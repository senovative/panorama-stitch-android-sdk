package io.senovative.panorama.sdk.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.senovative.panorama.sdk.OpenCvPanoramaEngine
import io.senovative.panorama.sdk.PanoramaDirection
import io.senovative.panorama.sdk.ui.components.BottomPanel
import io.senovative.panorama.sdk.ui.components.CameraPreview
import io.senovative.panorama.sdk.ui.components.PanoramaGuideOverlay
import io.senovative.panorama.sdk.ui.components.PermissionScreen
import io.senovative.panorama.sdk.ui.components.ResultPreview
import io.senovative.panorama.sdk.ui.components.TopBar
import io.senovative.panorama.sdk.ui.model.PanoramaUiState
import io.senovative.panorama.sdk.util.addScanFrame
import io.senovative.panorama.sdk.util.finishPanorama
import io.senovative.panorama.sdk.util.saveBitmap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Full panorama camera screen.
 *
 * @param onFinished Called when the user closes the result preview.
 *   Receives the saved panorama [Uri] if the user saved, or `null` if cancelled.
 */
@Composable
internal fun PanoramaCameraScreen(
    onFinished: (Uri?) -> Unit = {}
) {
    val context = LocalContext.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val processingExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var direction by remember { mutableStateOf(PanoramaDirection.Horizontal) }
    val engine = remember(direction) { OpenCvPanoramaEngine(direction) }
    var state by remember { mutableStateOf(PanoramaUiState()) }
    var liveGuideBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scanFrameInFlight by remember { mutableStateOf(false) }
    val scanEnabled = remember { AtomicBoolean(false) }
    val latestState by rememberUpdatedState(state)
    val latestLiveGuideBitmap by rememberUpdatedState(liveGuideBitmap)

    // ── Side effects ─────────────────────────────────────────────────────────────

    LaunchedEffect(state.isRecording) { scanEnabled.set(state.isRecording) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            hasCameraPermission = it
            state = state.copy(
                message = if (it) "Camera ready." else "Camera permission required."
            )
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(engine) { onDispose { engine.close() } }
    DisposableEffect(processingExecutor) { onDispose { processingExecutor.shutdown() } }
    DisposableEffect(Unit) {
        onDispose {
            latestLiveGuideBitmap?.recycle()
            latestState.guideBitmap?.recycle()
            latestState.resultBitmap?.recycle()
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────────

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                shouldCollectScanFrame = { scanEnabled.get() },
                onLiveFrame = { bitmap ->
                    liveGuideBitmap?.recycle()
                    liveGuideBitmap = bitmap
                },
                onScanFrame = { bitmap ->
                    if (!state.isRecording || scanFrameInFlight
                        || state.isProcessing || state.resultBitmap != null
                    ) {
                        bitmap.recycle()
                        return@CameraPreview
                    }
                    scanFrameInFlight = true
                    addScanFrame(
                        bitmap = bitmap, engine = engine,
                        executor = processingExecutor, mainExecutor = mainExecutor,
                        onState = { state = it(state) },
                        onComplete = { scanFrameInFlight = false }
                    )
                }
            )

            PanoramaGuideOverlay(
                direction, state.guideBitmap, liveGuideBitmap,
                state.frameCount, state.isRecording, Modifier.fillMaxSize()
            )

            TopBar(
                    direction, state.frameCount,
                    enabled = !state.isProcessing && !state.isRecording,
                    onDirectionChange = {
                        direction = it
                        state.resultBitmap?.recycle()
                        state.guideBitmap?.recycle()
                        state = PanoramaUiState(
                            message = "${
                                if (it == PanoramaDirection.Horizontal) "Horizontal" else "Vertical"
                            } mode ready."
                        )
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                BottomPanel(
                    state, canRecord = true,
                    onStartRecording = {
                        engine.reset()
                        state.resultBitmap?.recycle()
                        state.guideBitmap?.recycle()
                        state = PanoramaUiState(
                            isRecording = true,
                            message = "Scanning… slowly sweep your camera."
                        )
                    },
                    onStopRecording = {
                        val captured = state.frameCount
                        state = state.copy(
                            isRecording = false,
                            message = if (captured >= 2) "Scan complete. Rendering…"
                            else "Too few frames. Try again, sweep slower."
                        )
                        if (captured >= 2) {
                            finishPanorama(engine, processingExecutor, mainExecutor) {
                                state = it(state)
                            }
                        }
                    },
                    onFinish = {
                        finishPanorama(engine, processingExecutor, mainExecutor) {
                            state = it(state)
                        }
                    },
                    onReset = {
                        engine.reset()
                        state.resultBitmap?.recycle()
                        state.guideBitmap?.recycle()
                        state = PanoramaUiState(message = "Session reset.")
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
        } else {
            PermissionScreen(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }

        // ── Auto-save when render completes ──────────────────────────────────
        LaunchedEffect(state.resultBitmap) {
            val target = state.resultBitmap ?: return@LaunchedEffect
            state = state.copy(isProcessing = true, message = "Saving panorama…")
            processingExecutor.execute {
                val saved = runCatching { saveBitmap(context, target) }
                mainExecutor.execute {
                    val uriStr = saved.getOrNull()?.toString()
                    target.recycle()
                    if (uriStr != null) {
                        onFinished(Uri.parse(uriStr))
                    } else {
                        state = state.copy(
                            isProcessing = false,
                            resultBitmap = null,
                            message = "Save failed: ${saved.exceptionOrNull()?.message}"
                        )
                    }
                }
            }
        }
    }
}
