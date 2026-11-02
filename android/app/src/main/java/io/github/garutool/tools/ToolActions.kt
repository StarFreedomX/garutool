package io.github.garutool.tools

import android.content.Context
import io.github.garutool.BackAccessibilityService
import io.github.garutool.DisconnectActivity

enum class ActionResult { ACCEPTED, BUSY, ACCESSIBILITY_REQUIRED, FAILED, UNSUPPORTED }

object ToolActions {
    fun execute(context: Context, id: String): ActionResult = when (id) {
        "pausePulse" -> DisconnectActivity.launch(context)
        "globalBack" -> if (BackAccessibilityService.performBack()) ActionResult.ACCEPTED
            else ActionResult.ACCESSIBILITY_REQUIRED
        else -> ActionResult.UNSUPPORTED
    }
}
