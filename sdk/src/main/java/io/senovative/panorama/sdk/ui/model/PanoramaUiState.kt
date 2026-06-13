package io.senovative.panorama.sdk.ui.model

import android.graphics.Bitmap

internal data class PanoramaUiState(
    val frameCount: Int = 0,
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val resultBitmap: Bitmap? = null,
    val guideBitmap: Bitmap? = null,
    val message: String = "Tap record, then slowly sweep your camera.",
    val savedUri: String? = null
)
