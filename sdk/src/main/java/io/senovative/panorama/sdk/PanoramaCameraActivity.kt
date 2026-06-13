package io.senovative.panorama.sdk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.senovative.panorama.sdk.ui.screen.PanoramaCameraScreen
import io.senovative.panorama.sdk.ui.theme.PanoramaTheme

/**
 * Self-contained Activity that provides the full panorama camera experience.
 *
 * Launch via [PanoramaCameraContract] to receive the saved panorama [android.net.Uri]
 * as the activity result.
 */
class PanoramaCameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PanoramaTheme {
                PanoramaCameraScreen(
                    onFinished = { uri ->
                        if (uri != null) {
                            setResult(RESULT_OK, Intent().setData(uri))
                        } else {
                            setResult(RESULT_CANCELED)
                        }
                        finish()
                    }
                )
            }
        }
    }
}
