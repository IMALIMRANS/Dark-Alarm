package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.alarm.AlarmScheduler
import com.example.audio.AlarmSoundManager
import com.example.data.AlarmRepository
import com.example.data.AppDatabase

class SmartAlarmApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var alarmScheduler: AlarmScheduler
        private set

    lateinit var alarmRepository: AlarmRepository
        private set

    lateinit var soundManager: AlarmSoundManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        alarmScheduler = AlarmScheduler(this)
        alarmRepository = AlarmRepository(database.alarmDao(), alarmScheduler)
        soundManager = AlarmSoundManager(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "Smart Alarm Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for ringing alarms and wake-up challenges"
                setSound(null, null) // Custom sound handled by AlarmSoundManager
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ALARM_ID = "smart_alarm_channel"
        lateinit var instance: SmartAlarmApplication
            private set
    }
}
