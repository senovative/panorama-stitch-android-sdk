package io.senovative.panorama.sdk.util

import android.graphics.Bitmap
import io.senovative.panorama.sdk.OpenCvPanoramaEngine
import io.senovative.panorama.sdk.PanoramaFailure
import io.senovative.panorama.sdk.PanoramaRenderResult
import io.senovative.panorama.sdk.ui.model.PanoramaUiState
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

internal fun addScanFrame(
    bitmap: Bitmap,
    engine: OpenCvPanoramaEngine,
    executor: ExecutorService,
    mainExecutor: Executor,
    onState: ((PanoramaUiState) -> PanoramaUiState) -> Unit,
    onComplete: () -> Unit
) {
    executor.execute {
        val result = runCatching {
            engine.addFrame(bitmap)
        }.getOrElse {
            PanoramaRenderResult.Failure(PanoramaFailure.RenderFailed, it.message)
        }
        bitmap.recycle()

        mainExecutor.execute {
            onState { current ->
                when (result) {
                    is PanoramaRenderResult.Success -> {
                        current.guideBitmap?.recycle()
                        current.copy(
                            frameCount = result.frameCount,
                            guideBitmap = result.bitmap,
                            message = "Scanning… ${result.frameCount} frames captured."
                        )
                    }

                    is PanoramaRenderResult.Failure -> current.copy(
                        isRecording = false,
                        message = result.detail ?: result.reason.message
                    )
                }
            }
            onComplete()
        }
    }
}

internal fun finishPanorama(
    engine: OpenCvPanoramaEngine,
    executor: ExecutorService,
    mainExecutor: Executor,
    onState: ((PanoramaUiState) -> PanoramaUiState) -> Unit
) {
    mainExecutor.execute {
        onState { it.copy(isProcessing = true, message = "Rendering panorama…") }
    }

    executor.execute {
        val result = engine.render()
        mainExecutor.execute {
            onState { current ->
                when (result) {
                    is PanoramaRenderResult.Success -> current.copy(
                        isProcessing = false,
                        resultBitmap = result.bitmap,
                        message = "Panorama ready — ${result.frameCount} frames stitched."
                    )

                    is PanoramaRenderResult.Failure -> current.copy(
                        isProcessing = false,
                        message = result.detail ?: result.reason.message
                    )
                }
            }
        }
    }
}
