package com.luckyzyx.colorpicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min

class ColorGradientView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentColor: Int = Color.RED
    private var alpha: Int = 255
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var hue: Float = 0f

    private val ringPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val fillPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var svBitmap: Bitmap? = null
    private var hueBitmap: Bitmap? = null

    private val showHueBar: Boolean = true

    private var listener: ((Int) -> Unit)? = null

    init {
        post { invalidate() }
    }

    fun getCurrentColor(): Int = (currentColor and 0xFFFFFF) or (alpha shl 24)

    fun setColor(color: Int) {
        currentColor = color and 0xFFFFFF
        alpha = Color.alpha(color)
        updatePosition(currentColor)
        invalidate()
        notifyChanged()
    }

    fun setAlpha(a: Int) {
        alpha = min(max(a, 0), 255)
        invalidate()
        notifyChanged()
    }

    fun setOnColorChangedListener(l: (Int) -> Unit) {
        listener = l
    }

    private fun updatePosition(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue = hsv[0]
        val w = width.toFloat()
        val h = height.toFloat()
        val usableH = if (showHueBar) h * 0.85f else h
        touchX = hsv[1] * w
        touchY = (1f - hsv[2]) * usableH
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val usableH = if (showHueBar) h * 0.85f else h

        var bmp = svBitmap
        if (bmp == null || bmp.width != w.toInt() || bmp.height != usableH.toInt()) {
            bmp = makeSvBitmap(w.toInt(), usableH.toInt())
            svBitmap = bmp
        }
        bmp?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        if (showHueBar) {
            val hueBarTop = usableH + 50f
            val hueBarH = h - usableH
            var hb = hueBitmap
            if (hb == null || hb.width != w.toInt()) {
                val bw = w.toInt()
                val bh = hueBarH.toInt()
                hb = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
                val lg = LinearGradient(
                    0f, 0f, bw.toFloat(), 0f,
                    intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
                        Color.BLUE, Color.MAGENTA, Color.RED),
                    floatArrayOf(0f, 0.16666667f, 0.33333334f, 0.5f, 0.6666667f, 0.8333333f, 1f),
                    Shader.TileMode.CLAMP
                )
                val p = Paint().apply { shader = lg }
                Canvas(hb).drawRect(0f, 0f, bw.toFloat(), bh.toFloat(), p)
                hueBitmap = hb
            }
            hb?.let { canvas.drawBitmap(it, 0f, hueBarTop, null) }
            val hueX = w * (hue / 360f)
            ringPaint.color = Color.BLACK
            ringPaint.strokeWidth = 5f
            canvas.drawLine(hueX, hueBarTop, hueX, h, ringPaint)
        }

        ringPaint.color = Color.WHITE
        ringPaint.strokeWidth = 4f
        canvas.drawCircle(touchX, touchY, 16f, ringPaint)
        fillPaint.color = getCurrentColor()
        canvas.drawCircle(touchX, touchY, 12f, fillPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val h = height.toFloat()
        val usableH = if (showHueBar) h * 0.85f else h
        val hueBarTop = usableH + 50f
        val action = event.action
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) return true

        if (showHueBar && y > hueBarTop) {
            hue = (min(x / width, 1f)) * 360f
            svBitmap = makeSvBitmap(width, (height * 0.85f).toInt())
            svBitmap?.let {
                val px = min(touchX.toInt(), it.width - 1).coerceAtLeast(0)
                val py = min(touchY.toInt(), it.height - 1).coerceAtLeast(0)
                currentColor = it.getPixel(px, py)
            }
        } else if (y <= usableH) {
            touchX = min(x, width.toFloat())
            touchY = min(y, usableH)
            svBitmap?.let {
                val px = min(touchX.toInt(), it.width - 1).coerceAtLeast(0)
                val py = min(touchY.toInt(), it.height - 1).coerceAtLeast(0)
                currentColor = it.getPixel(px, py)
            }
        }
        invalidate()
        notifyChanged()
        return true
    }

    private fun makeSvBitmap(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        val hsv = FloatArray(3)
        hsv[0] = hue
        for (j in 0 until h) {
            hsv[2] = 1f - j.toFloat() / h
            for (i in 0 until w) {
                hsv[1] = i.toFloat() / w
                pixels[j * w + i] = Color.HSVToColor(hsv)
            }
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }

    private fun notifyChanged() {
        listener?.invoke(getCurrentColor())
    }

    fun getcurrentColor(): Int = getCurrentColor()
}
