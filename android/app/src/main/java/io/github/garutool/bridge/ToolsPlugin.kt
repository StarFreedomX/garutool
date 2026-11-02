package io.github.garutool.bridge

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import io.github.garutool.BackAccessibilityService
import io.github.garutool.tools.ToolRuntime

@CapacitorPlugin(name = "GaruTools", permissions = [
    Permission(alias = "notifications", strings = [Manifest.permission.POST_NOTIFICATIONS]),
])
class ToolsPlugin : Plugin() {
    private val stateChanged: () -> Unit = {
        notifyListeners("stateChanged", JSObject(ToolRuntime.snapshot(context).toString()))
    }

    override fun load() { ToolRuntime.addListener(stateChanged) }
    override fun handleOnDestroy() { ToolRuntime.removeListener(stateChanged) }

    @PluginMethod
    fun getState(call: PluginCall) = onMain(call) { resolveState(call) }

    @PluginMethod
    fun setOverlayEnabled(call: PluginCall) = onMain(call) {
        val enabled = call.getBoolean("enabled") ?: throw IllegalArgumentException("Missing enabled")
        ToolRuntime.setEnabled(context, enabled)
        resolveState(call)
    }

    @PluginMethod
    fun updateConfig(call: PluginCall) = onMain(call) {
        val duration = if (call.data.has("pauseDurationMs")) {
            val value = call.data.opt("pauseDurationMs")
            require(value is Number) { "Invalid pause duration" }
            val raw = value.toDouble()
            require(raw.isFinite() && raw == raw.toLong().toDouble()) { "Invalid pause duration" }
            raw.toLong()
        } else null
        val theme = if (call.data.has("themeMode")) {
            val value = call.data.opt("themeMode")
            require(value is String) { "Unknown theme" }
            value
        } else null
        val actions = if (call.data.has("enabledActions")) {
            val items = call.data.opt("enabledActions")
            require(items is org.json.JSONArray) { "Invalid actions" }
            (0 until items.length()).map {
                val id = items.opt(it)
                require(id is String) { "Invalid action" }
                id
            }
        } else null
        ToolRuntime.updateConfig(context, theme, duration, actions)
        resolveState(call)
    }

    @PluginMethod
    fun openSettings(call: PluginCall) = onMain(call) {
        when (call.getString("kind")) {
            "overlay" -> {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"))
                try { activity.startActivity(intent) }
                catch (_: android.content.ActivityNotFoundException) {
                    activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")))
                }
            }
            "accessibility" -> BackAccessibilityService.openSettings(activity)
            else -> throw IllegalArgumentException("Unknown settings page")
        }
        call.resolve()
    }

    @PluginMethod
    fun requestNotifications(call: PluginCall) = onMain(call) {
        val preferences = context.getSharedPreferences("garutool_ui", android.content.Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT < 33 || getPermissionState("notifications") == PermissionState.GRANTED ||
            preferences.getBoolean("notification_requested", false)) {
            call.resolve()
        } else {
            preferences.edit().putBoolean("notification_requested", true).apply()
            requestPermissionForAlias("notifications", call, "notificationsResult")
        }
    }

    @PermissionCallback
    private fun notificationsResult(call: PluginCall) { call.resolve() }

    private fun resolveState(call: PluginCall) {
        call.resolve(JSObject(ToolRuntime.snapshot(context).toString()))
    }

    private fun onMain(call: PluginCall, action: () -> Unit) {
        activity.runOnUiThread {
            try { action() }
            catch (error: IllegalArgumentException) { call.reject(error.message, "INVALID_ARGUMENT", error) }
            catch (error: RuntimeException) {
                Log.e("GaruTool", "Native command failed: ${call.methodName}", error)
                call.reject("暂时无法执行", "NATIVE_ERROR", error)
            }
        }
    }
}
