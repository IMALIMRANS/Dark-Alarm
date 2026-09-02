package com.example.engine

import com.example.model.MathDifficulty
import com.example.model.MathProblem
import kotlin.random.Random

object MathChallengeEngine {

    fun generateProblems(difficulty: MathDifficulty, count: Int): List<MathProblem> {
        val problems = mutableListOf<MathProblem>()
        for (i in 1..count) {
            problems.add(generateSingleProblem(difficulty))
        }
        return problems
    }

    private fun generateSingleProblem(difficulty: MathDifficulty): MathProblem {
        return when (difficulty) {
            MathDifficulty.EASY -> generateEasyProblem()
            MathDifficulty.MEDIUM -> generateMediumProblem()
            MathDifficulty.HARD -> generateHardAlgebraProblem()
        }
    }

    // Easy: Addition & Subtraction only (2-digit arithmetic)
    private fun generateEasyProblem(): MathProblem {
        val isAddition = Random.nextBoolean()
        return if (isAddition) {
            val a = Random.nextInt(12, 89)
            val b = Random.nextInt(11, 79)
            MathProblem(
                question = "$a + $b = ?",
                correctAnswer = a + b
            )
        } else {
            val a = Random.nextInt(35, 99)
            val b = Random.nextInt(12, a - 5)
            MathProblem(
                question = "$a - $b = ?",
                correctAnswer = a - b
            )
        }
    }

    // Medium: Addition, Subtraction & Multiplication (no division)
    private fun generateMediumProblem(): MathProblem {
        val type = Random.nextInt(3)
        return when (type) {
            0 -> { // Multiplication + Addition: a * b + c
                val a = Random.nextInt(3, 13)
                val b = Random.nextInt(4, 12)
                val c = Random.nextInt(10, 50)
                MathProblem(
                    question = "($a × $b) + $c = ?",
                    correctAnswer = (a * b) + c
                )
            }
            1 -> { // Multiplication - Subtraction: a * b - c
                val a = Random.nextInt(5, 14)
                val b = Random.nextInt(4, 12)
                val mult = a * b
                val c = Random.nextInt(5, mult - 5)
                MathProblem(
                    question = "($a × $b) - $c = ?",
                    correctAnswer = mult - c
                )
            }
            else -> { // Addition then subtraction or 3-term
                val a = Random.nextInt(20, 60)
                val b = Random.nextInt(15, 50)
                val c = Random.nextInt(10, 35)
                MathProblem(
                    question = "$a + $b - $c = ?",
                    correctAnswer = a + b - c
                )
            }
        }
    }

    // Hard: Algebra questions solving for x and y
    private fun generateHardAlgebraProblem(): MathProblem {
        val type = Random.nextInt(4)
        return when (type) {
            0 -> {
                // Find x: ax + b = c
                val a = Random.nextInt(2, 9)
                val x = Random.nextInt(2, 16)
                val b = Random.nextInt(5, 30)
                val c = a * x + b
                MathProblem(
                    question = "Solve for x:\n${a}x + $b = $c",
                    explanation = "${a}x = ${c - b} ➔ x = $x",
                    correctAnswer = x
                )
            }
            1 -> {
                // Find x: ax - b = c
                val a = Random.nextInt(2, 8)
                val x = Random.nextInt(3, 18)
                val b = Random.nextInt(6, 35)
                val c = a * x - b
                MathProblem(
                    question = "Solve for x:\n${a}x - $b = $c",
                    explanation = "${a}x = ${c + b} ➔ x = $x",
                    correctAnswer = x
                )
            }
            2 -> {
                // Find y: ay - b = c
                val a = Random.nextInt(2, 7)
                val y = Random.nextInt(4, 20)
                val b = Random.nextInt(8, 40)
                val c = a * y - b
                MathProblem(
                    question = "Solve for y:\n${a}y - $b = $c",
                    explanation = "${a}y = ${c + b} ➔ y = $y",
                    correctAnswer = y
                )
            }
            else -> {
                // System equation: ax + by = target, given y = val, solve for x
                val a = Random.nextInt(2, 6)
                val b = Random.nextInt(2, 6)
                val yVal = Random.nextInt(2, 9)
                val xVal = Random.nextInt(2, 12)
                val target = a * xVal + b * yVal
                MathProblem(
                    question = "Solve for x:\n${a}x + ${b}y = $target\n(Given y = $yVal)",
                    explanation = "${a}x + ${b * yVal} = $target ➔ ${a}x = ${target - b * yVal} ➔ x = $xVal",
                    correctAnswer = xVal
                )
            }
        }
    }
}
