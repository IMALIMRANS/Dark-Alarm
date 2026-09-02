package com.example.ui.ringing

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.SmartAlarmApplication
import com.example.model.Alarm
import com.example.service.AlarmRingingService
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class AlarmRingingActivity : ComponentActivity() {

    private var alarmDismissReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLockScreenFlags()
        enableEdgeToEdge()

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)

        // Register dismiss receiver in case auto-stop triggers in background
        alarmDismissReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                finish()
            }
        }
        val filter = IntentFilter(AlarmRingingService.ACTION_ALARM_DISMISSED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmDismissReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(alarmDismissReceiver, filter)
        }

        // Prevent accidental back gesture during ringing challenge
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Must complete wake-up challenge or emergency tap to dismiss
            }
        })

        setContent {
            MyApplicationTheme(darkTheme = true) {
                var alarmState by remember {
                    mutableStateOf(
                        Alarm(
                            id = alarmId,
                            label = "Smart Alarm",
                            hour = 7,
                            minute = 0,
                            isSmartAlarm = false
                        )
                    )
                }

                LaunchedEffect(alarmId) {
                    if (alarmId != -1L) {
                        val app = application as SmartAlarmApplication
                        val dbAlarm = app.alarmRepository.getAlarmById(alarmId)
                        if (dbAlarm != null) {
                            alarmState = dbAlarm
                        }
                    }
                }

                AlarmRingingScreen(
                    alarm = alarmState,
                    onAlarmDismissed = {
                        dismissAndFinish()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Volume override: Prevents muting the alarm via volume keys
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
            return true // Consume key event
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun dismissAndFinish() {
        AlarmRingingService.stop(this)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmDismissReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
