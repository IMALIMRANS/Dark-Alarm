package com.example

import com.example.engine.MathChallengeEngine
import com.example.model.MathDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testEasyMathGeneration() {
        val problems = MathChallengeEngine.generateProblems(MathDifficulty.EASY, 3)
        assertEquals(3, problems.size)
        problems.forEach { problem ->
            assertNotNull(problem.question)
            assertTrue(problem.question.contains("+") || problem.question.contains("-"))
        }
    }

    @Test
    fun testMediumMathGeneration() {
        val problems = MathChallengeEngine.generateProblems(MathDifficulty.MEDIUM, 4)
        assertEquals(4, problems.size)
        problems.forEach { problem ->
            assertNotNull(problem.question)
            // Verify no division
            assertTrue(!problem.question.contains("/") && !problem.question.contains("÷"))
        }
    }

    @Test
    fun testHardAlgebraMathGeneration() {
        val problems = MathChallengeEngine.generateProblems(MathDifficulty.HARD, 3)
        assertEquals(3, problems.size)
        problems.forEach { problem ->
            assertTrue(problem.question.contains("Solve for") || problem.question.contains("x"))
        }
    }
}
