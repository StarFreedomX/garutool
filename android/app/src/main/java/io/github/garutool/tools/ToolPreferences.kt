package io.github.garutool.tools

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray

class ToolPreferences(context: Context) {
    private val values = context.getSharedPreferences("garutool", Context.MODE_PRIVATE)

    var overlayEnabled: Boolean
        get() = values.getBoolean("overlay_enabled", false)
        set(value) { values.edit().putBoolean("overlay_enabled", value).apply() }

    val enabledActions: List<String> get() {
        val saved = values.getStringSet("enabled_actions", null)
        return ToolCatalog.actions.map { it.id }.filter { saved == null || it in saved }
    }

    val pauseDurationMs: Long get() = values.getLong("pause_duration_ms", ToolCatalog.duration.getLong("default"))
        .coerceIn(ToolCatalog.duration.getLong("min"), ToolCatalog.duration.getLong("max"))

    val themeMode: String get() = values.getString("theme_mode", "system")
        ?.takeIf { it in ToolCatalog.themeModes } ?: "system"

    fun update(themeMode: String?, pauseDurationMs: Long?, enabledActions: List<String>?) {
        require(themeMode == null || themeMode in ToolCatalog.themeModes) { "Unknown theme" }
        require(pauseDurationMs == null || pauseDurationMs in
            ToolCatalog.duration.getLong("min")..ToolCatalog.duration.getLong("max")) { "Invalid pause duration" }
        require(enabledActions == null || enabledActions.all { id -> ToolCatalog.actions.any { it.id == id } }) {
            "Unknown action"
        }
        values.edit().apply {
            if (enabledActions != null) {
                putStringSet("enabled_actions", enabledActions.toSet())
                if (enabledActions.isEmpty()) putBoolean("overlay_enabled", false)
            }
            if (themeMode != null) putString("theme_mode", themeMode)
            if (pauseDurationMs != null) putLong("pause_duration_ms", pauseDurationMs)
        }.apply()
    }

    fun snapshot(): JSONObject = JSONObject()
        .put("overlayEnabled", overlayEnabled)
        .put("enabledActions", JSONArray(enabledActions))
        .put("pauseDurationMs", pauseDurationMs)
        .put("themeMode", themeMode)
}
