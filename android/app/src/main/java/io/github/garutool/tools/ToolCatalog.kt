package io.github.garutool.tools

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import io.github.garutool.ToolAssets
import org.json.JSONObject

object ToolCatalog {
    private val source = JSONObject(ToolAssets.CATALOG_JSON)
    val duration: JSONObject = source.getJSONObject("pauseDuration")
    val toolbar: JSONObject = source.getJSONObject("toolbar")
    val themeModes = source.getJSONArray("themeModes").let { values ->
        (0 until values.length()).map { values.getJSONObject(it).getString("value") }.toSet()
    }
    data class Action(val id: String, val label: String, val icon: Int)
    val actions = source.getJSONArray("actions").let { values ->
        (0 until values.length()).map {
            val item = values.getJSONObject(it)
            Action(item.getString("id"), item.getString("label"), ToolAssets.icon(item.getString("icon")))
        }
    }

    class Palette(private val colors: JSONObject) {
        private fun color(name: String) = Color.parseColor(colors.getString(name))
        val surface get() = color("surface")
        val text get() = color("text")
        val line get() = color("line")
        val accent get() = color("accent")
    }

    fun isDark(context: Context): Boolean = when (ToolPreferences(context).themeMode) {
        "light" -> false
        "dark" -> true
        else -> context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
    }

    fun palette(context: Context) = Palette(
        source.getJSONObject("themes").getJSONObject(if (isDark(context)) "dark" else "light")
    )
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
