package com.example.mediaportal

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val imageMatrixState = Matrix()
    private var mode = NONE

    private var lastTouchPoint = PointF()
    private var minScale = 1.0f
    private var maxScale = 5.0f
    private var currentScale = 1.0f

    private var scaleDetector: ScaleGestureDetector
    private var gestureDetector: GestureDetector

    private var origWidth = 0f
    private var origHeight = 0f
    private var viewWidth = 0
    private var viewHeight = 0

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())

        setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            val currentPoint = PointF(event.x, event.y)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchPoint.set(currentPoint)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG && currentScale > minScale) {
                        val deltaX = currentPoint.x - lastTouchPoint.x
                        val deltaY = currentPoint.y - lastTouchPoint.y
                        imageMatrixState.postTranslate(deltaX, deltaY)
                        fixTranslation()
                        lastTouchPoint.set(currentPoint.x, currentPoint.y)
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    mode = ZOOM
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                    performClick()
                }
            }
            imageMatrix = imageMatrixState
            true
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null && viewWidth > 0 && viewHeight > 0) {
            fitToScreen()
        }
    }

    fun resetZoom() {
        imageMatrixState.reset()
        currentScale = 1.0f
        fitToScreen()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val prevScale = currentScale
            currentScale *= scaleFactor

            if (currentScale > maxScale) {
                currentScale = maxScale
                scaleFactor = maxScale / prevScale
            } else if (currentScale < minScale) {
                currentScale = minScale
                scaleFactor = minScale / prevScale
            }

            if (origWidth * currentScale <= viewWidth || origHeight * currentScale <= viewHeight) {
                imageMatrixState.postScale(scaleFactor, scaleFactor, viewWidth / 2f, viewHeight / 2f)
            } else {
                imageMatrixState.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            }

            fixTranslation()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (currentScale > minScale) {
                resetZoom()
            } else {
                val targetScale = 2.5f
                val scaleFactor = targetScale / currentScale
                currentScale = targetScale
                imageMatrixState.postScale(scaleFactor, scaleFactor, e.x, e.y)
                fixTranslation()
                imageMatrix = imageMatrixState
            }
            return true
        }
    }

    private fun fixTranslation() {
        val m = FloatArray(9)
        imageMatrixState.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * currentScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * currentScale)

        if (fixTransX != 0f || fixTransY != 0f) {
            imageMatrixState.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans) return -trans + minTrans
        if (trans > maxTrans) return -trans + maxTrans
        return 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (drawable != null && drawable.intrinsicWidth != 0 && drawable.intrinsicHeight != 0) {
            fitToScreen()
        }
    }

    private fun fitToScreen() {
        val drawable = drawable ?: return
        val bmWidth = drawable.intrinsicWidth.toFloat()
        val bmHeight = drawable.intrinsicHeight.toFloat()

        if (bmWidth == 0f || bmHeight == 0f || viewWidth == 0 || viewHeight == 0) return

        val scaleX = viewWidth.toFloat() / bmWidth
        val scaleY = viewHeight.toFloat() / bmHeight
        val scale = Math.min(scaleX, scaleY)

        imageMatrixState.reset()
        imageMatrixState.postScale(scale, scale)

        val redundantYSpace = viewHeight.toFloat() - (scale * bmHeight)
        val redundantXSpace = viewWidth.toFloat() - (scale * bmWidth)

        imageMatrixState.postTranslate(redundantXSpace / 2f, redundantYSpace / 2f)

        origWidth = viewWidth - redundantXSpace
        origHeight = viewHeight - redundantYSpace
        currentScale = 1.0f

        imageMatrix = imageMatrixState
    }
}
