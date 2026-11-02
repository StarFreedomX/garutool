package io.github.garutool

import android.content.res.Configuration
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.getcapacitor.BridgeActivity
import io.github.garutool.bridge.ToolsPlugin
import io.github.garutool.tools.ToolCatalog
import io.github.garutool.tools.ToolRuntime

class MainActivity : BridgeActivity() {
    private val appearanceChanged: () -> Unit = { applyAppearance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(ToolsPlugin::class.java)
        super.onCreate(savedInstanceState)
        ToolRuntime.addListener(appearanceChanged)
        window.decorView.post { applyAppearance() }
    }

    override fun onResume() {
        super.onResume()
        ToolRuntime.reconcile(this)
        ToolRuntime.publish()
        applyAppearance()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ToolRuntime.publish()
    }

    override fun onDestroy() {
        ToolRuntime.removeListener(appearanceChanged)
        super.onDestroy()
    }

    private fun applyAppearance() {
        val palette = ToolCatalog.palette(this)
        window.decorView.setBackgroundColor(palette.surface)
        bridge?.webView?.setBackgroundColor(palette.surface)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !ToolCatalog.isDark(this@MainActivity)
            isAppearanceLightNavigationBars = !ToolCatalog.isDark(this@MainActivity)
        }
    }
}
