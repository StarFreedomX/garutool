package io.github.garutool.tools

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import io.github.garutool.BackAccessibilityService
import io.github.garutool.OverlayService
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArraySet

enum class OverlayStatus(val value: String) {
    STOPPED("stopped"), STARTING("starting"), RUNNING("running"),
    PERMISSION_REQUIRED("permissionRequired"), FAILED("failed"),
}

object ToolRuntime {
    private val handler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<() -> Unit>()
    private var revision = 0L
    private var overlayStatus = OverlayStatus.STOPPED
        private set
    private var overlayError: String? = null

    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
    fun publish() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { publish() }
            return
        }
        revision++
        listeners.forEach { it() }
    }

    private fun setOverlayStatus(status: OverlayStatus, error: String? = null) {
        overlayStatus = status
        overlayError = error
        publish()
    }

    fun shown() { setOverlayStatus(OverlayStatus.RUNNING) }

    fun setEnabled(context: Context, enabled: Boolean) {
        ToolPreferences(context).overlayEnabled = enabled
        if (enabled) start(context) else {
            context.stopService(Intent(context, OverlayService::class.java))
            setOverlayStatus(OverlayStatus.STOPPED)
        }
    }

    fun reconcile(context: Context) {
        if (!ToolPreferences(context).overlayEnabled) return
        if (!Settings.canDrawOverlays(context)) {
            context.stopService(Intent(context, OverlayService::class.java))
            setOverlayStatus(OverlayStatus.PERMISSION_REQUIRED)
        } else if (overlayStatus == OverlayStatus.STOPPED || overlayStatus == OverlayStatus.PERMISSION_REQUIRED) start(context)
    }

    private fun start(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            setOverlayStatus(OverlayStatus.PERMISSION_REQUIRED)
            return
        }
        if (overlayStatus == OverlayStatus.RUNNING || overlayStatus == OverlayStatus.STARTING) return
        setOverlayStatus(OverlayStatus.STARTING)
        try {
            context.startForegroundService(Intent(context, OverlayService::class.java))
        } catch (error: RuntimeException) {
            failed(error)
        }
    }

    fun failed(error: RuntimeException) {
        Log.e("GaruTool", "Overlay failed", error)
        setOverlayStatus(OverlayStatus.FAILED, "无法显示悬浮窗")
    }

    fun stopped(context: Context) {
        if (overlayStatus != OverlayStatus.FAILED) setOverlayStatus(
            if (ToolPreferences(context).overlayEnabled && !Settings.canDrawOverlays(context))
                OverlayStatus.PERMISSION_REQUIRED else OverlayStatus.STOPPED
        )
    }

    fun snapshot(context: Context): JSONObject = JSONObject()
        .put("revision", revision)
        .put("config", ToolPreferences(context).snapshot())
        .put("permissions", JSONObject()
            .put("overlay", Settings.canDrawOverlays(context))
            .put("accessibility", when {
                BackAccessibilityService.isConnected() -> "connected"
                BackAccessibilityService.isEnabled(context) -> "enabled"
                else -> "disabled"
            }))
        .put("overlay", JSONObject().put("status", overlayStatus.value).put("error", overlayError ?: JSONObject.NULL))
        .put("appearance", JSONObject().put("dark", ToolCatalog.isDark(context)))
}
