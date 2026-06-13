package io.senovative.panorama.sdk.ui.components

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.senovative.panorama.sdk.util.rotateBy
import io.senovative.panorama.sdk.util.scaledCopyTo
import java.util.concurrent.Executors

@Composable
internal fun CameraPreview(
    modifier: Modifier = Modifier,
    shouldCollectScanFrame: () -> Boolean,
    onLiveFrame: (Bitmap) -> Unit,
    onScanFrame: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        var cameraProvider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        future.addListener(
            {
                cameraProvider = future.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { imageAnalysis ->
                        var lastLiveFrameAt = 0L
                        var lastScanFrameAt = 0L
                        imageAnalysis.setAnalyzer(analysisExecutor) { image ->
                            val now = SystemClock.elapsedRealtime()
                            val shouldSendLiveFrame = now - lastLiveFrameAt >= 125L
                            val shouldSendScanFrame =
                                shouldCollectScanFrame() && now - lastScanFrameAt >= 650L
                            if (!shouldSendLiveFrame && !shouldSendScanFrame) {
                                image.close()
                                return@setAnalyzer
                            }

                            val decoded = runCatching {
                                image.toBitmap().rotateBy(image.imageInfo.rotationDegrees)
                            }.getOrNull()
                            image.close()
                            decoded ?: return@setAnalyzer

                            val liveBitmap =
                                if (shouldSendLiveFrame) decoded.scaledCopyTo(360) else null
                            val scanBitmap =
                                if (shouldSendScanFrame) decoded.scaledCopyTo(1280) else null
                            decoded.recycle()

                            if (shouldSendLiveFrame) lastLiveFrameAt = now
                            if (shouldSendScanFrame) lastScanFrameAt = now

                            mainExecutor.execute {
                                liveBitmap?.let(onLiveFrame)
                                scanBitmap?.let(onScanFrame)
                            }
                        }
                    }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            },
            mainExecutor
        )

        onDispose {
            cameraProvider?.unbindAll()
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
