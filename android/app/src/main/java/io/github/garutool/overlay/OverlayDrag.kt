package io.github.garutool.overlay

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.roundToInt

class OverlayDrag(
    context: Context,
    private val origin: () -> Point?,
    private val move: (Int, Int) -> Unit,
    private val save: () -> Unit,
) {
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    fun attach(view: View) {
        var pointer = MotionEvent.INVALID_POINTER_ID
        var startX = 0f
        var startY = 0f
        var windowX = 0
        var windowY = 0
        var dragging = false

        fun finish(touched: View): Boolean {
            touched.isPressed = false
            val clicked = !dragging && pointer != MotionEvent.INVALID_POINTER_ID
            if (dragging) save()
            pointer = MotionEvent.INVALID_POINTER_ID
            dragging = false
            return clicked
        }

        view.setOnTouchListener { touched, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val position = origin() ?: return@setOnTouchListener false
                    pointer = event.getPointerId(0)
                    startX = event.rawX
                    startY = event.rawY
                    windowX = position.x
                    windowY = position.y
                    dragging = false
                    touched.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val index = event.findPointerIndex(pointer)
                    if (index < 0) return@setOnTouchListener true
                    val rawX = if (Build.VERSION.SDK_INT >= 29) event.getRawX(index)
                        else event.getX(index) + event.rawX - event.x
                    val rawY = if (Build.VERSION.SDK_INT >= 29) event.getRawY(index)
                        else event.getY(index) + event.rawY - event.y
                    val dx = rawX - startX
                    val dy = rawY - startY
                    if (!dragging && (abs(dx) > slop || abs(dy) > slop)) {
                        dragging = true
                        touched.isPressed = false
                    }
                    if (dragging) move(windowX + dx.roundToInt(), windowY + dy.roundToInt())
                }
                MotionEvent.ACTION_UP -> if (finish(touched)) touched.performClick()
                MotionEvent.ACTION_POINTER_UP -> {
                    if (event.getPointerId(event.actionIndex) == pointer) finish(touched)
                }
                MotionEvent.ACTION_CANCEL -> finish(touched)
            }
            true
        }
    }
}
