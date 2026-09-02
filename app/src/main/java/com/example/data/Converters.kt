package com.example.data

import androidx.room.TypeConverter
import com.example.model.ChallengeType
import com.example.model.MathDifficulty
import com.example.model.SoundType

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        if (data.isBlank()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(";;;")
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        if (data.isBlank()) return emptyList()
        return data.split(";;;").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromChallengeType(type: ChallengeType): String = type.name

    @TypeConverter
    fun toChallengeType(name: String): ChallengeType {
        return try {
            ChallengeType.valueOf(name)
        } catch (e: Exception) {
            ChallengeType.MATH
        }
    }

    @TypeConverter
    fun fromMathDifficulty(difficulty: MathDifficulty): String = difficulty.name

    @TypeConverter
    fun toMathDifficulty(name: String): MathDifficulty {
        return try {
            MathDifficulty.valueOf(name)
        } catch (e: Exception) {
            MathDifficulty.EASY
        }
    }

    @TypeConverter
    fun fromSoundType(type: SoundType): String = type.name

    @TypeConverter
    fun toSoundType(name: String): SoundType {
        return try {
            SoundType.valueOf(name)
        } catch (e: Exception) {
            SoundType.BUILT_IN
        }
    }
}
