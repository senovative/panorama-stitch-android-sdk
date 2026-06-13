package io.senovative.panorama.sdk

import android.graphics.Bitmap
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.DMatch
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.features2d.BFMatcher
import org.opencv.features2d.ORB
import org.opencv.imgproc.Imgproc
import java.io.Closeable
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class OpenCvPanoramaEngine(
    private val direction: PanoramaDirection,
    private val maxInputEdge: Int = 1600,
    private val minimumMatches: Int = 16
) : Closeable {

    private val frames = mutableListOf<Mat>()

    val frameCount: Int
        get() = frames.size

    fun addFrame(bitmap: Bitmap): PanoramaRenderResult {
        if (!ensureOpenCv()) {
            return PanoramaRenderResult.Failure(PanoramaFailure.OpenCvUnavailable)
        }

        return runCatching {
            frames += bitmap.toRgbaMat().scaledTo(maxInputEdge)
            PanoramaRenderResult.Success(frames.last().toBitmap(), frames.size)
        }.getOrElse {
            PanoramaRenderResult.Failure(PanoramaFailure.RenderFailed, it.message)
        }
    }

    fun render(): PanoramaRenderResult {
        if (!ensureOpenCv()) {
            return PanoramaRenderResult.Failure(PanoramaFailure.OpenCvUnavailable)
        }
        val n = frames.size
        if (n < 2) {
            return PanoramaRenderResult.Failure(PanoramaFailure.NeedMoreFrames)
        }

        return runCatching {
            val pairHomographies = mutableListOf<Mat>()

            // 1. Calculate Pairwise Homographies (H_i+1_to_i)
            for (i in 0 until n - 1) {
                val h = computePairTranslation(frames[i], frames[i + 1])
                if (h == null) {
                    pairHomographies.forEach { it.release() }
                    return PanoramaRenderResult.Failure(PanoramaFailure.RenderFailed, "Invalid sweep motion")
                }
                pairHomographies.add(h)
            }

            // 2. Chain Homographies to Center Reference
            val refIdx = n / 2
            val absHomographies = Array(n) { Mat() }
            absHomographies[refIdx] = Mat.eye(3, 3, CvType.CV_64F)

            // For i > refIdx, map i to refIdx
            for (i in refIdx + 1 until n) {
                val chained = Mat()
                // abs[i] = abs[i-1] * pair[i-1]
                Core.gemm(absHomographies[i - 1], pairHomographies[i - 1], 1.0, Mat(), 0.0, chained)
                absHomographies[i] = chained
            }

            // For i < refIdx, map i to refIdx
            for (i in refIdx - 1 downTo 0) {
                val chained = Mat()
                // abs[i] = abs[i+1] * (pair[i]^-1)
                Core.gemm(absHomographies[i + 1], pairHomographies[i].inv(), 1.0, Mat(), 0.0, chained)
                absHomographies[i] = chained
            }

            // 3. Find Global Bounding Box
            var minX = Double.MAX_VALUE
            var minY = Double.MAX_VALUE
            var maxX = -Double.MAX_VALUE
            var maxY = -Double.MAX_VALUE

            for (i in 0 until n) {
                val frame = frames[i]
                val w = frame.cols().toDouble()
                val h = frame.rows().toDouble()
                val corners = MatOfPoint2f(
                    Point(0.0, 0.0),
                    Point(w, 0.0),
                    Point(w, h),
                    Point(0.0, h)
                )
                val warpedCorners = MatOfPoint2f()
                Core.perspectiveTransform(corners, warpedCorners, absHomographies[i])

                warpedCorners.toArray().forEach { p ->
                    if (p.x < minX) minX = p.x
                    if (p.y < minY) minY = p.y
                    if (p.x > maxX) maxX = p.x
                    if (p.y > maxY) maxY = p.y
                }
                releaseAll(corners, warpedCorners)
            }

            val canvasWidth = max(1, ceil(maxX - minX).roundToInt())
            val canvasHeight = max(1, ceil(maxY - minY).roundToInt())

            val translation = Mat.eye(3, 3, CvType.CV_64F)
            translation.put(0, 2, -minX)
            translation.put(1, 2, -minY)

            // 4. Voronoi Seam Blending (ArgMax)
            val finalCanvas8U = Mat(canvasHeight, canvasWidth, CvType.CV_8UC4, Scalar(0.0, 0.0, 0.0, 0.0))
            val currentMaxAlpha = Mat(canvasHeight, canvasWidth, CvType.CV_32FC1, Scalar(-1.0))

            for (i in 0 until n) {
                val frame = frames[i]
                
                // Shift homography by translation
                val finalH = Mat()
                Core.gemm(translation, absHomographies[i], 1.0, Mat(), 0.0, finalH)

                // Create distance transform alpha mask for feathering
                val mask8U = Mat(frame.rows(), frame.cols(), CvType.CV_8UC1, Scalar(255.0))
                Imgproc.rectangle(
                    mask8U,
                    Point(0.0, 0.0),
                    Point(frame.cols() - 1.0, frame.rows() - 1.0),
                    Scalar(0.0),
                    1 // 1-pixel black border
                )
                val dist = Mat()
                Imgproc.distanceTransform(mask8U, dist, Imgproc.DIST_L2, Imgproc.DIST_MASK_PRECISE)
                Core.normalize(dist, dist, 0.0, 1.0, Core.NORM_MINMAX)

                // Warp frame and alpha mask to canvas space
                val warpedFrame = Mat()
                Imgproc.warpPerspective(
                    frame, warpedFrame, finalH,
                    Size(canvasWidth.toDouble(), canvasHeight.toDouble())
                )

                val warpedAlpha = Mat()
                Imgproc.warpPerspective(
                    dist, warpedAlpha, finalH,
                    Size(canvasWidth.toDouble(), canvasHeight.toDouble())
                )

                // Voronoi mask: where warpedAlpha > currentMaxAlpha
                val greaterMask = Mat()
                Core.compare(warpedAlpha, currentMaxAlpha, greaterMask, Core.CMP_GT)

                // Update canvas and max alpha using the mask
                warpedFrame.copyTo(finalCanvas8U, greaterMask)
                warpedAlpha.copyTo(currentMaxAlpha, greaterMask)

                releaseAll(finalH, mask8U, dist, warpedFrame, warpedAlpha, greaterMask)
            }

            // Crop black borders
            val cropped = finalCanvas8U.cropContent()
            val bitmap = cropped.toBitmap()

            // Cleanup
            releaseAll(finalCanvas8U, currentMaxAlpha, cropped, translation)
            pairHomographies.forEach { it.release() }
            absHomographies.forEach { it.release() }

            PanoramaRenderResult.Success(bitmap, frames.size)
        }.getOrElse {
            PanoramaRenderResult.Failure(PanoramaFailure.RenderFailed, it.message)
        }
    }

    fun reset() {
        frames.forEach(Mat::release)
        frames.clear()
    }

    override fun close() {
        reset()
    }

    private fun computePairTranslation(base: Mat, next: Mat): Mat? {
        val baseGray = base.toGray()
        val nextGray = next.toGray()
        
        val baseFloat = Mat()
        val nextFloat = Mat()
        baseGray.convertTo(baseFloat, CvType.CV_32FC1)
        nextGray.convertTo(nextFloat, CvType.CV_32FC1)
        
        val window = Mat()
        Imgproc.createHanningWindow(window, Size(base.cols().toDouble(), base.rows().toDouble()), CvType.CV_32FC1)
        
        val shift = Imgproc.phaseCorrelate(nextFloat, baseFloat, window)
        
        releaseAll(baseGray, nextGray, baseFloat, nextFloat, window)
        
        val horizontal = abs(shift.x) >= abs(shift.y) * 0.5
        val vertical = abs(shift.y) >= abs(shift.x) * 0.5
        val valid = when (direction) {
            PanoramaDirection.Horizontal -> horizontal
            PanoramaDirection.Vertical -> vertical
        }
        
        if (!valid) return null

        val h = Mat.eye(3, 3, CvType.CV_64F)
        h.put(0, 2, shift.x)
        h.put(1, 2, shift.y)
        
        return h
    }

    private fun Bitmap.toRgbaMat(): Mat {
        val source = if (config == Bitmap.Config.ARGB_8888) this else copy(Bitmap.Config.ARGB_8888, false)
        val rgba = Mat()
        Utils.bitmapToMat(source, rgba)
        if (source !== this) {
            source.recycle()
        }
        return rgba
    }

    private fun Mat.scaledTo(maxEdge: Int): Mat {
        val edge = max(cols(), rows())
        if (edge <= maxEdge) return this

        val scale = maxEdge.toDouble() / edge.toDouble()
        val resized = Mat()
        Imgproc.resize(this, resized, Size(cols() * scale, rows() * scale), 0.0, 0.0, Imgproc.INTER_AREA)
        release()
        return resized
    }

    private fun Mat.toGray(): Mat {
        val gray = Mat()
        Imgproc.cvtColor(this, gray, Imgproc.COLOR_RGBA2GRAY)
        return gray
    }

    private fun Mat.contentMask(): Mat {
        val gray = toGray()
        val mask = Mat()
        Imgproc.threshold(gray, mask, 1.0, 255.0, Imgproc.THRESH_BINARY)
        gray.release()
        return mask
    }

    private fun Mat.cropContent(): Mat {
        val mask = contentMask()
        val nonZero = Mat()
        Core.findNonZero(mask, nonZero)
        if (nonZero.empty()) {
            releaseAll(mask, nonZero)
            return clone()
        }

        val rect = Imgproc.boundingRect(nonZero)
        val cropped = Mat(this, rect).clone()
        releaseAll(mask, nonZero)
        return cropped
    }

    private fun Mat.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(this, bitmap)
        return bitmap
    }

    private fun ensureOpenCv(): Boolean {
        return openCvLoaded || OpenCVLoader.initLocal().also { openCvLoaded = it }
    }

    private fun releaseAll(vararg mats: Mat) {
        mats.forEach(Mat::release)
    }

    private companion object {
        var openCvLoaded = false
    }
}
