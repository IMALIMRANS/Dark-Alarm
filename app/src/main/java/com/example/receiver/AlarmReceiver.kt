package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.SmartAlarmApplication
import com.example.service.AlarmRingingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        Log.d("AlarmReceiver", "Received alarm trigger for alarmId: $alarmId")

        // Start ringing foreground service
        AlarmRingingService.start(context, alarmId)

        // Reschedule next occurrence if repeating or toggle off if one-time
        if (alarmId != -1L) {
            val app = context.applicationContext as? SmartAlarmApplication
            app?.let { application ->
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = application.alarmRepository.getAlarmById(alarmId)
                    if (alarm != null) {
                        if (alarm.repeatDays.isEmpty()) {
                            // One-time alarm: turn off
                            application.alarmRepository.toggleAlarm(alarm, false)
                        } else {
                            // Repeating alarm: schedule next day
                            application.alarmScheduler.schedule(alarm)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.aistudio.smartalarm.ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
