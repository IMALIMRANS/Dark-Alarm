package com.example.data

import com.example.alarm.AlarmScheduler
import com.example.model.Alarm
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)

    suspend fun getActiveAlarms(): List<Alarm> = alarmDao.getActiveAlarms()

    suspend fun insertAlarm(alarm: Alarm): Long {
        val id = alarmDao.insertAlarm(alarm)
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.isEnabled) {
            alarmScheduler.schedule(savedAlarm)
        }
        return id
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
        if (alarm.isEnabled) {
            alarmScheduler.schedule(alarm)
        } else {
            alarmScheduler.cancel(alarm)
        }
    }

    suspend fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        val updated = alarm.copy(isEnabled = isEnabled)
        alarmDao.updateAlarm(updated)
        if (isEnabled) {
            alarmScheduler.schedule(updated)
        } else {
            alarmScheduler.cancel(updated)
        }
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmScheduler.cancel(alarm)
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun deleteAlarmById(id: Long) {
        val alarm = alarmDao.getAlarmById(id)
        if (alarm != null) {
            alarmScheduler.cancel(alarm)
            alarmDao.deleteAlarm(alarm)
        }
    }

    fun rescheduleAllActive(alarms: List<Alarm>) {
        alarms.filter { it.isEnabled }.forEach {
            alarmScheduler.schedule(it)
        }
    }
}
