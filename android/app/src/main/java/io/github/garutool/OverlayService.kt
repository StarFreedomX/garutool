package io.github.garutool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import io.github.garutool.overlay.OverlayWindow
import io.github.garutool.tools.ActionResult
import io.github.garutool.tools.ToolActions
import io.github.garutool.tools.ToolPreferences
import io.github.garutool.tools.ToolRuntime

class OverlayService : Service() {
    private var overlay: OverlayWindow? = null
    private val settingsChanged: () -> Unit = {
        try { overlay?.refreshAppearance() } catch (error: RuntimeException) { fail(error) }
    }

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.overlay_notification_channel),
                NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
        )
        ToolRuntime.addListener(settingsChanged)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) ToolPreferences(this).overlayEnabled = false
        if (!ToolPreferences(this).overlayEnabled || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            if (Build.VERSION.SDK_INT >= 34)
                startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else startForeground(NOTIFICATION_ID, notification())
            if (overlay == null) {
                overlay = OverlayWindow(this, ::execute)
                overlay!!.show()
            }
            ToolRuntime.shown()
        } catch (error: RuntimeException) { fail(error) }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try { overlay?.show() } catch (error: RuntimeException) { fail(error) }
        ToolRuntime.publish()
    }

    override fun onDestroy() {
        ToolRuntime.removeListener(settingsChanged)
        overlay?.close()
        overlay = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        ToolRuntime.stopped(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun fail(error: RuntimeException) {
        ToolRuntime.failed(error)
        stopSelf()
    }

    private fun execute(id: String) {
        when (ToolActions.execute(this, id)) {
            ActionResult.ACCESSIBILITY_REQUIRED -> {
                Toast.makeText(this, getString(R.string.back_enable_required, getString(R.string.app_name)),
                    Toast.LENGTH_SHORT).show()
                BackAccessibilityService.openSettings(this)
            }
            ActionResult.FAILED, ActionResult.UNSUPPORTED ->
                Toast.makeText(this, R.string.action_unavailable, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    private fun notification(): Notification {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val open = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), flags)
        val close = PendingIntent.getService(this, 1,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP), flags)
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_overlay)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.overlay_notification_status))
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, getString(R.string.overlay_notification_stop), close).build())
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "floating_tools"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "io.github.garutool.STOP_OVERLAY"
    }
}
