package com.example.model

enum class ChallengeType {
    MATH,
    SCAN,
    MATH_AND_SCAN
}

enum class MathDifficulty {
    EASY,     // Addition & Subtraction only
    MEDIUM,   // Addition, Subtraction & Multiplication (no division)
    HARD      // Linear and Algebraic expressions solving for x / y
}

enum class SoundType {
    BUILT_IN,
    CUSTOM
}
