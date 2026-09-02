package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.Alarm
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) {
            cancel(alarm)
            return
        }

        val triggerMillis = calculateNextTriggerMillis(alarm)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    alarm.id.toInt(),
                    Intent(context, com.example.MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerMillis, showIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} at $triggerMillis (${alarm.getFormattedTime()})")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Permission denied scheduling exact alarm", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } catch (ex: Exception) {
                Log.e("AlarmScheduler", "Fallback scheduling failed", ex)
            }
        }
    }

    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Cancelled alarm ${alarm.id}")
    }

    companion object {
        fun calculateNextTriggerMillis(alarm: Alarm): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Case 1: One-time alarm (no repeating days)
            if (alarm.repeatDays.isEmpty()) {
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            // Case 2: Repeating alarm on specific days (1=Mon ... 7=Sun)
            // Note: Java Calendar day of week is: 1=Sunday, 2=Monday, 3=Tuesday, 4=Wednesday, 5=Thursday, 6=Friday, 7=Saturday
            // We map our 1=Mon...7=Sun to Calendar constants
            val calendarDays = alarm.repeatDays.map { dayIndex ->
                when (dayIndex) {
                    1 -> Calendar.MONDAY
                    2 -> Calendar.TUESDAY
                    3 -> Calendar.WEDNESDAY
                    4 -> Calendar.THURSDAY
                    5 -> Calendar.FRIDAY
                    6 -> Calendar.SATURDAY
                    7 -> Calendar.SUNDAY
                    else -> Calendar.MONDAY
                }
            }.toSet()

            var bestDiffMillis = Long.MAX_VALUE
            for (day in 0..7) {
                val candidate = (target.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, day)
                }
                val candidateDayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
                if (calendarDays.contains(candidateDayOfWeek)) {
                    val diff = candidate.timeInMillis - now.timeInMillis
                    if (diff > 0 && diff < bestDiffMillis) {
                        bestDiffMillis = diff
                    }
                }
            }

            return if (bestDiffMillis != Long.MAX_VALUE) {
                now.timeInMillis + bestDiffMillis
            } else {
                // Fallback
                target.add(Calendar.DAY_OF_YEAR, 1)
                target.timeInMillis
            }
        }

        fun getNextAlarmFormattedCountdown(alarm: Alarm): String {
            val triggerMillis = calculateNextTriggerMillis(alarm)
            val nowMillis = System.currentTimeMillis()
            val diffMs = triggerMillis - nowMillis
            if (diffMs <= 0) return "Ringing soon"

            val diffSeconds = diffMs / 1000
            val diffMinutes = (diffSeconds / 60) % 60
            val diffHours = (diffSeconds / 3600) % 24
            val diffDays = diffSeconds / (3600 * 24)

            return when {
                diffDays > 0 -> "In $diffDays d, $diffHours h, $diffMinutes min"
                diffHours > 0 -> "In $diffHours h $diffMinutes min"
                diffMinutes > 0 -> "In $diffMinutes minutes"
                else -> "In less than a minute"
            }
        }
    }
}
