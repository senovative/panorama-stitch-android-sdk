package io.senovative.panorama.sdk

import android.graphics.Bitmap

sealed interface PanoramaRenderResult {
    data class Success(
        val bitmap: Bitmap,
        val frameCount: Int
    ) : PanoramaRenderResult

    data class Failure(
        val reason: PanoramaFailure,
        val detail: String? = null
    ) : PanoramaRenderResult
}
