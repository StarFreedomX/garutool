package io.github.garutool.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import io.github.garutool.tools.ToolCatalog
import kotlin.math.roundToInt

class OverlayWindow(context: Context, private val onAction: (String) -> Unit) {
    private val windowContext = if (Build.VERSION.SDK_INT >= 30) {
        val display = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
        context.createDisplayContext(display).createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)
    } else context
    private val manager = windowContext.getSystemService(WindowManager::class.java)
    private val positions = context.getSharedPreferences("overlay_position", Context.MODE_PRIVATE)
    private var view: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var positionBounds: Rect? = null
    private var dark: Boolean? = null
    private val drag = OverlayDrag(windowContext,
        origin = { params?.let { Point(it.x, it.y) } },
        move = { x, y ->
            params?.let {
                it.x = x
                it.y = y
            }
            clamp()
        },
        save = { savePosition() },
    )

    fun refreshAppearance() {
        if (view != null && dark != ToolCatalog.isDark(windowContext)) show()
    }

    fun show() {
        close()
        val bar = floatingToolbar(windowContext, drag, onAction)
        bar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bounds = usableBounds()
        val layout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= 30) setFitInsetsTypes(0)
            if (Build.VERSION.SDK_INT >= 28)
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            x = bounds.left + ((bounds.width() - bar.measuredWidth).coerceAtLeast(0) *
                positions.getFloat("x", 0.025f)).roundToInt()
            y = bounds.top + ((bounds.height() - bar.measuredHeight).coerceAtLeast(0) *
                positions.getFloat("y", 0.32f)).roundToInt()
        }
        manager.addView(bar, layout)
        view = bar
        params = layout
        positionBounds = bounds
        dark = ToolCatalog.isDark(windowContext)
        bar.post { clamp() }
    }

    fun close() {
        view?.let {
            savePosition()
            if (it.isAttachedToWindow) manager.removeView(it)
        }
        view = null
        params = null
    }

    private fun clamp() {
        val bar = view ?: return
        val layout = params ?: return
        val bounds = usableBounds()
        layout.x = layout.x.coerceIn(bounds.left, (bounds.right - bar.measuredWidth).coerceAtLeast(bounds.left))
        layout.y = layout.y.coerceIn(bounds.top, (bounds.bottom - bar.measuredHeight).coerceAtLeast(bounds.top))
        positionBounds = bounds
        if (bar.isAttachedToWindow) manager.updateViewLayout(bar, layout)
    }

    private fun savePosition() {
        val bar = view ?: return
        val layout = params ?: return
        val bounds = positionBounds ?: usableBounds()
        positions.edit()
            .putFloat("x", ((layout.x - bounds.left).toFloat() /
                (bounds.width() - bar.measuredWidth).coerceAtLeast(1)).coerceIn(0f, 1f))
            .putFloat("y", ((layout.y - bounds.top).toFloat() /
                (bounds.height() - bar.measuredHeight).coerceAtLeast(1)).coerceIn(0f, 1f))
            .apply()
    }

    private fun usableBounds(): Rect {
        if (Build.VERSION.SDK_INT >= 30) {
            val metrics = manager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            return Rect(metrics.bounds).apply {
                left += insets.left
                top += insets.top
                right -= insets.right
                bottom -= insets.bottom
            }
        }
        val size = Point()
        @Suppress("DEPRECATION")
        manager.defaultDisplay.getSize(size)
        return Rect(0, 0, size.x, size.y)
    }
}
