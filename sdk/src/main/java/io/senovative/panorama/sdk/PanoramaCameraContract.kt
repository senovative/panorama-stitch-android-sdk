package io.senovative.panorama.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * [ActivityResultContract] for launching the panorama camera.
 *
 * Usage:
 * ```kotlin
 * val launcher = rememberLauncherForActivityResult(PanoramaCameraContract()) { uri ->
 *     if (uri != null) {
 *         // Panorama saved at uri
 *     }
 * }
 * launcher.launch(Unit)
 * ```
 */
class PanoramaCameraContract : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, PanoramaCameraActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.data
    }
}
