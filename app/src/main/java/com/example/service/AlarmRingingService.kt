package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SmartAlarmApplication
import com.example.model.Alarm
import com.example.model.SoundType
import com.example.ui.ringing.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmRingingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var autoStopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentAlarm: Alarm? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SmartAlarm::RingingServiceWakeLock"
        ).apply {
            acquire(3 * 60 * 60 * 1000L) // 3 hours max safety limit
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        if (action == ACTION_STOP_ALARM) {
            stopAlarmAndService()
            return START_NOT_STICKY
        }

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        scope.launch {
            val app = application as SmartAlarmApplication
            val alarm = if (alarmId != -1L) {
                app.alarmRepository.getAlarmById(alarmId)
            } else {
                null
            }

            val effectiveAlarm = alarm ?: Alarm(
                id = -1L,
                label = "Smart Alarm",
                hour = 7,
                minute = 0,
                isSmartAlarm = false
            )
            currentAlarm = effectiveAlarm

            // Start Foreground Notification
            val notification = buildForegroundNotification(effectiveAlarm)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            // Start Audio and Vibration
            app.soundManager.startAlarmRinging(
                soundType = effectiveAlarm.soundType,
                soundUriOrName = effectiveAlarm.soundUriOrName,
                isVibrate = effectiveAlarm.isVibrate
            )

            // Setup Auto Stop Timer
            startAutoStopTimer(effectiveAlarm)

            // Launch Alarm Ringing UI
            val ringIntent = Intent(this@AlarmRingingService, AlarmRingingActivity::class.java).apply {
                setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra(AlarmRingingActivity.EXTRA_ALARM_ID, effectiveAlarm.id)
            }
            startActivity(ringIntent)
        }

        return START_STICKY
    }

    private fun startAutoStopTimer(alarm: Alarm) {
        autoStopJob?.cancel()
        val minutes = if (alarm.isSmartAlarm && alarm.autoStopMinutes > 0) {
            alarm.autoStopMinutes
        } else {
            30 // Default 30 minutes auto-stop
        }

        autoStopJob = scope.launch {
            Log.d("AlarmRingingService", "Auto-stop set for $minutes minutes")
            delay(minutes * 60 * 1000L)
            Log.d("AlarmRingingService", "Auto-stop triggered! Stopping alarm.")
            stopAlarmAndService()
        }
    }

    private fun buildForegroundNotification(alarm: Alarm): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmRingingActivity.EXTRA_ALARM_ID, alarm.id)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarm.id.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SmartAlarmApplication.CHANNEL_ALARM_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.label.ifBlank { "Smart Alarm" })
            .setContentText("Alarm is ringing! Time: ${alarm.getFormattedTime()}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun stopAlarmAndService() {
        try {
            val app = application as SmartAlarmApplication
            app.soundManager.stopAll()
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error stopping sound manager", e)
        }

        autoStopJob?.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        // Broadcast stop event so UI knows
        sendBroadcast(Intent(ACTION_ALARM_DISMISSED))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmAndService()
    }

    companion object {
        const val NOTIFICATION_ID = 9001
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val ACTION_START_ALARM = "com.aistudio.smartalarm.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.aistudio.smartalarm.ACTION_STOP_ALARM"
        const val ACTION_ALARM_DISMISSED = "com.aistudio.smartalarm.ACTION_ALARM_DISMISSED"

        @Volatile
        var isRinging: Boolean = false
            private set

        @Volatile
        var currentAlarmId: Long = -1L
            private set

        fun start(context: Context, alarmId: Long) {
            isRinging = true
            currentAlarmId = alarmId
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            isRinging = false
            currentAlarmId = -1L
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
    }
}
