package com.example.ui.ringing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MathChallengeEngine
import com.example.model.MathDifficulty
import com.example.model.MathProblem
import com.example.ui.components.CustomMathKeypad
import com.example.ui.theme.PolishActiveContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishInactiveContainer
import com.example.ui.theme.PolishInactiveTrack
import com.example.ui.theme.PolishOnActiveContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.SoftRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MathChallengeView(
    difficulty: MathDifficulty,
    problemCount: Int,
    onChallengeCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val problems = remember { MathChallengeEngine.generateProblems(difficulty, problemCount) }
    var currentProblemIndex by remember { mutableIntStateOf(0) }
    var currentInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }

    val currentProblem = problems.getOrNull(currentProblemIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Progress
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PolishActiveContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Math Challenge",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Problem ${currentProblemIndex + 1} of ${problems.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (currentProblemIndex + 1).toFloat() / problems.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PolishPrimary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }

        // Problem Box & Answer Input Card
        if (currentProblem != null) {
            Column(
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 4.dp,
                    border = BorderStroke(
                        2.dp,
                        if (isError) SoftRed else PolishBorder.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentProblem.question,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = PolishTextPrimary,
                            lineHeight = 36.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Answer Box
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PolishInactiveContainer)
                                .border(
                                    1.5.dp,
                                    if (isError) SoftRed else PolishPrimary.copy(alpha = 0.6f),
                                    RoundedCornerShape(18.dp)
                                )
                        ) {
                            Text(
                                text = if (currentInput.isEmpty()) "?" else currentInput,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentInput.isEmpty()) PolishTextSecondary.copy(alpha = 0.6f) else PolishTextPrimary
                            )
                        }

                        if (isError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage.ifBlank { "Incorrect! Try again." },
                                color = SoftRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Custom On-Screen Keypad
        CustomMathKeypad(
            onDigitClick = { digit ->
                isError = false
                if (currentInput.length < 7) {
                    currentInput += digit
                }
            },
            onBackspaceClick = {
                isError = false
                if (currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                }
            },
            onNegativeClick = {
                isError = false
                currentInput = if (currentInput.startsWith("-")) {
                    currentInput.substring(1)
                } else {
                    "-$currentInput"
                }
            },
            onSubmitClick = {
                val inputVal = currentInput.toIntOrNull()
                if (inputVal == null) {
                    isError = true
                    errorMessage = "Enter an answer first"
                    return@CustomMathKeypad
                }

                if (currentProblem != null && inputVal == currentProblem.correctAnswer) {
                    isError = false
                    currentInput = ""
                    if (currentProblemIndex + 1 >= problems.size) {
                        onChallengeCompleted()
                    } else {
                        currentProblemIndex++
                    }
                } else {
                    isError = true
                    errorMessage = "Wrong answer! Alarm keeps ringing."
                    scope.launch {
                        // Shake animation
                        for (i in 0..2) {
                            shakeOffset.animateTo(25f, tween(50))
                            shakeOffset.animateTo(-25f, tween(50))
                        }
                        shakeOffset.animateTo(0f, tween(50))
                    }
                }
            }
        )
    }
}
