package com.example.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SmartAlarmApplication
import com.example.alarm.AlarmScheduler
import com.example.model.Alarm
import com.example.model.ChallengeType
import com.example.model.MathDifficulty
import com.example.service.AlarmRingingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SmartAlarmApplication
    private val repository = app.alarmRepository

    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _nextAlarmMessage = MutableStateFlow<String?>(null)
    val nextAlarmMessage: StateFlow<String?> = _nextAlarmMessage.asStateFlow()

    init {
        // Ticker to refresh countdown message and watch alarms
        viewModelScope.launch {
            repository.allAlarms.collect { list ->
                updateNextAlarmMessage(list)
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(60000)
                updateNextAlarmMessage(alarms.value)
            }
        }
    }

    private fun updateNextAlarmMessage(list: List<Alarm>) {
        val activeAlarms = list.filter { it.isEnabled }
        if (activeAlarms.isEmpty()) {
            _nextAlarmMessage.value = null
            return
        }

        // Find earliest upcoming alarm
        val earliest = activeAlarms.minByOrNull { AlarmScheduler.calculateNextTriggerMillis(it) }
        if (earliest != null) {
            _nextAlarmMessage.value = "${AlarmScheduler.getNextAlarmFormattedCountdown(earliest)} (${earliest.getFormattedTime()})"
        } else {
            _nextAlarmMessage.value = null
        }
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm, isEnabled)
        }
    }

    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch {
            if (alarm.id == 0L) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun testRingAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            // If alarm is not yet saved, save it temporarily
            val targetId = if (alarm.id == 0L) {
                repository.insertAlarm(alarm)
            } else {
                alarm.id
            }
            withContext(Dispatchers.Main) {
                AlarmRingingService.start(context, targetId)
            }
        }
    }
}
