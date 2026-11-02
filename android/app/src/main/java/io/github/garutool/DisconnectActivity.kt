package io.github.garutool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.util.Log
import io.github.garutool.tools.ActionResult
import io.github.garutool.tools.ToolPreferences

class DisconnectActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var closeScheduled = false
    private val closeActivity = Runnable {
        if (!isFinishing) finishAndRemoveTask()
        suppressTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inFlight = true
        activityCreated = true
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        window.setDimAmount(0f)
        window.attributes = window.attributes.apply { windowAnimations = 0 }
        setContentView(View(this).apply { setBackgroundColor(Color.TRANSPARENT) })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        suppressTransition()
    }

    override fun onResume() {
        super.onResume()
        if (!closeScheduled) {
            closeScheduled = true
            handler.postDelayed(closeActivity, ToolPreferences(this).pauseDurationMs)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing) closeActivity.run()
    }

    override fun onDestroy() {
        handler.removeCallbacks(closeActivity)
        inFlight = false
        activityCreated = false
        super.onDestroy()
    }

    private fun suppressTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    companion object {
        private val launchHandler = Handler(Looper.getMainLooper())
        private var inFlight = false
        private var activityCreated = false
        private var lastLaunch = -1_000L

        fun launch(context: Context): ActionResult {
            val now = SystemClock.elapsedRealtime()
            if (inFlight || now - lastLaunch < 800L) return ActionResult.BUSY
            lastLaunch = now
            inFlight = true
            try {
                context.startActivity(
                    Intent(context, DisconnectActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )
                )
                // A background launch may be denied without throwing an exception.
                launchHandler.postDelayed({
                    if (lastLaunch == now && !activityCreated) inFlight = false
                }, 1_500L)
                return ActionResult.ACCEPTED
            } catch (error: SecurityException) {
                inFlight = false
                Log.e("GaruTool", "Pause activity launch failed", error)
                return ActionResult.FAILED
            }
        }
    }
}
