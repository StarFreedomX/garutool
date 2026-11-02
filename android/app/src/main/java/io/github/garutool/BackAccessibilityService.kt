package io.github.garutool

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import java.lang.ref.WeakReference
import io.github.garutool.tools.ToolRuntime

class BackAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = 0
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = 0
        }
        connection = WeakReference(this)
        notifyConnectionChanged()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        clearConnection()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        clearConnection()
        super.onDestroy()
    }

    private fun clearConnection() {
        if (connection.get() === this) {
            connection.clear()
            notifyConnectionChanged()
        }
    }

    private fun notifyConnectionChanged() {
        ToolRuntime.publish()
    }

    companion object {
        private var connection = WeakReference<BackAccessibilityService>(null)

        fun isConnected(): Boolean = connection.get() != null

        fun isEnabled(context: Context): Boolean {
            if (isConnected()) return true
            val manager = context.getSystemService(AccessibilityManager::class.java)
            return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any {
                    val service = it.resolveInfo.serviceInfo
                    service.packageName == context.packageName &&
                        service.name == BackAccessibilityService::class.java.name
                }
        }

        fun performBack(): Boolean = connection.get()?.performGlobalAction(GLOBAL_ACTION_BACK) == true

        fun openSettings(context: Context) {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
