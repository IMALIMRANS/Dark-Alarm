package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int = 7,                 // 0-23
    val minute: Int = 0,               // 0-59
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun. Default Every Day (1..7)
    val soundType: SoundType = SoundType.BUILT_IN,
    val soundUriOrName: String = "gentle_melody",
    val isVibrate: Boolean = true,
    val isSmartAlarm: Boolean = false,
    val challengeType: ChallengeType = ChallengeType.MATH,
    val mathDifficulty: MathDifficulty = MathDifficulty.EASY,
    val mathProblemCount: Int = 3,
    val autoStopMinutes: Int = 30,     // 30, 60, 120, 180, etc.
    val referencePhotoPaths: List<String> = emptyList(), // local file paths
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isDaily(): Boolean = repeatDays.size == 7

    fun isWeekdays(): Boolean = repeatDays.toSet() == setOf(1, 2, 3, 4, 5)

    fun isWeekends(): Boolean = repeatDays.toSet() == setOf(6, 7)

    fun getFormattedTime(): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%02d:%02d %s", displayHour, minute, amPm)
    }

    fun getRepeatDaysText(): String {
        if (repeatDays.isEmpty()) return "Once"
        if (isDaily()) return "Everyday"
        if (isWeekdays()) return "Weekdays"
        if (isWeekends()) return "Weekends"

        val dayNames = mapOf(
            1 to "Mon",
            2 to "Tue",
            3 to "Wed",
            4 to "Thu",
            5 to "Fri",
            6 to "Sat",
            7 to "Sun"
        )
        return repeatDays.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}
