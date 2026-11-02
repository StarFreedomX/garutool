package io.github.garutool.tools

import android.content.Context
import org.json.JSONObject

class ToolPreferences(context: Context) {
    private val values = context.getSharedPreferences("garutool", Context.MODE_PRIVATE)

    var overlayEnabled: Boolean
        get() = values.getBoolean("overlay_enabled", false)
        set(value) { values.edit().putBoolean("overlay_enabled", value).apply() }

    val pauseDurationMs: Long get() = values.getLong("pause_duration_ms", ToolCatalog.duration.getLong("default"))
        .coerceIn(ToolCatalog.duration.getLong("min"), ToolCatalog.duration.getLong("max"))

    val themeMode: String get() = values.getString("theme_mode", "system")
        ?.takeIf { it in ToolCatalog.themeModes } ?: "system"

    fun update(themeMode: String?, pauseDurationMs: Long?) {
        require(themeMode == null || themeMode in ToolCatalog.themeModes) { "Unknown theme" }
        require(pauseDurationMs == null || pauseDurationMs in
            ToolCatalog.duration.getLong("min")..ToolCatalog.duration.getLong("max")) { "Invalid pause duration" }
        values.edit().apply {
            if (themeMode != null) putString("theme_mode", themeMode)
            if (pauseDurationMs != null) putLong("pause_duration_ms", pauseDurationMs)
        }.apply()
    }

    fun snapshot(): JSONObject = JSONObject()
        .put("overlayEnabled", overlayEnabled)
        .put("pauseDurationMs", pauseDurationMs)
        .put("themeMode", themeMode)
}
