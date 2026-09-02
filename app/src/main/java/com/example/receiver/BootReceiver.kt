package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.SmartAlarmApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d("BootReceiver", "Device rebooted, rescheduling active alarms...")
            val app = context.applicationContext as? SmartAlarmApplication
            app?.let { application ->
                CoroutineScope(Dispatchers.IO).launch {
                    val activeAlarms = application.alarmRepository.getActiveAlarms()
                    application.alarmRepository.rescheduleAllActive(activeAlarms)
                    Log.d("BootReceiver", "Rescheduled ${activeAlarms.size} active alarms.")
                }
            }
        }
    }
}
