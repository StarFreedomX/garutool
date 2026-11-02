package io.github.garutool.overlay

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import io.github.garutool.tools.ToolPreferences
import io.github.garutool.tools.ToolCatalog
import io.github.garutool.tools.dp

fun floatingToolbar(context: Context, drag: OverlayDrag, onAction: (String) -> Unit): View {
    val palette = ToolCatalog.palette(context)
    val style = ToolCatalog.toolbar
    fun size(key: String) = context.dp(style.getInt(key))
    val bar = LinearLayout(context).apply {
        gravity = Gravity.CENTER_VERTICAL
        isMotionEventSplittingEnabled = false
        setPadding(size("padding"), size("padding"), size("padding"), size("padding"))
        background = GradientDrawable().apply {
            setColor(palette.surface)
            cornerRadius = size("radius").toFloat()
            setStroke(context.dp(1), palette.line)
        }
        elevation = size("elevation").toFloat()
    }
    val enabled = ToolPreferences(context).enabledActions
    ToolCatalog.actions.filter { it.id in enabled }.forEachIndexed { index, action ->
        if (index > 0) bar.addView(View(context).apply { setBackgroundColor(palette.line) },
            LinearLayout.LayoutParams(size("separatorWidth"), size("separatorHeight")).apply {
                marginStart = size("separatorMargin")
                marginEnd = size("separatorMargin")
            })
        val button = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = action.label
            background = RippleDrawable(
                ColorStateList.valueOf((palette.accent and 0x00ffffff) or (36 shl 24)),
                null,
                GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = size("buttonRadius").toFloat()
                },
            )
            addView(ImageView(context).apply {
                setImageResource(action.icon)
                imageTintList = ColorStateList.valueOf(palette.text)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }, LinearLayout.LayoutParams(size("iconSize"), size("iconSize")))
            setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onAction(action.id)
            }
        }
        drag.attach(button)
        bar.addView(button, LinearLayout.LayoutParams(size("buttonSize"), size("buttonSize")))
    }
    return bar
}
