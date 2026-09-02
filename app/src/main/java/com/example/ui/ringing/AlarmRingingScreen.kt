package com.example.ui.ringing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Alarm
import com.example.model.ChallengeType
import com.example.ui.theme.PolishPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class RingingUiState {
    RINGING_PROMPT,
    MATH_CHALLENGE,
    SCAN_CHALLENGE,
    COMPLETED
}

@Composable
fun AlarmRingingScreen(
    alarm: Alarm,
    onAlarmDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(RingingUiState.RINGING_PROMPT) }
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    // Live clock ticker
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C194D),
                        Color(0xFF1E1035),
                        Color(0xFF130924)
                    )
                )
            )
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "ringing_state_transition"
        ) { targetState ->
            when (targetState) {
                RingingUiState.RINGING_PROMPT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Header Status
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFFE8DEF8).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f)),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ALARM RINGING",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE8DEF8),
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }

                        // Center Clock & Alarm Label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentTimeString.ifBlank { alarm.getFormattedTime() },
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentDateString,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = alarm.label.ifBlank { "Smart Alarm" },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    if (alarm.isSmartAlarm) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                when (alarm.challengeType) {
                                                    ChallengeType.MATH -> Icons.Default.Calculate
                                                    ChallengeType.SCAN -> Icons.Default.QrCodeScanner
                                                    ChallengeType.MATH_AND_SCAN -> Icons.Default.Alarm
                                                },
                                                contentDescription = null,
                                                tint = Color(0xFFD0BCFF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (alarm.challengeType) {
                                                    ChallengeType.MATH -> "Math Challenge Required"
                                                    ChallengeType.SCAN -> "Scan Photo Match Required"
                                                    ChallengeType.MATH_AND_SCAN -> "Math + Scan Challenge Required"
                                                },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFE8DEF8)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Big Glowing Pulsing STOP Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(175.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                PolishPrimary,
                                                Color(0xFF533B88),
                                                Color(0xFF3F256E)
                                            )
                                        )
                                    )
                                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    .clickable {
                                        if (!alarm.isSmartAlarm) {
                                            uiState = RingingUiState.COMPLETED
                                        } else {
                                            when (alarm.challengeType) {
                                                ChallengeType.MATH -> uiState = RingingUiState.MATH_CHALLENGE
                                                ChallengeType.SCAN -> uiState = RingingUiState.SCAN_CHALLENGE
                                                ChallengeType.MATH_AND_SCAN -> uiState = RingingUiState.MATH_CHALLENGE
                                            }
                                        }
                                    }
                                    .testTag("alarm_stop_button")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Stop,
                                        contentDescription = "Stop Alarm",
                                        tint = Color.White,
                                        modifier = Modifier.size(46.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "STOP",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (alarm.isSmartAlarm)
                                    "Tap STOP to begin wake-up challenge"
                                else
                                    "Tap STOP to turn off alarm",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                RingingUiState.MATH_CHALLENGE -> {
                    MathChallengeView(
                        difficulty = alarm.mathDifficulty,
                        problemCount = alarm.mathProblemCount,
                        onChallengeCompleted = {
                            if (alarm.challengeType == ChallengeType.MATH_AND_SCAN) {
                                uiState = RingingUiState.SCAN_CHALLENGE
                            } else {
                                uiState = RingingUiState.COMPLETED
                            }
                        },
                        modifier = Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    )
                }

                RingingUiState.SCAN_CHALLENGE -> {
                    ScanChallengeView(
                        referencePhotoPaths = alarm.referencePhotoPaths,
                        onChallengeCompleted = {
                            uiState = RingingUiState.COMPLETED
                        },
                        modifier = Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    )
                }

                RingingUiState.COMPLETED -> {
                    DismissalCelebrationView(
                        onDismissed = onAlarmDismissed
                    )
                }
            }
        }
    }
}

/**
 * Animated Celebration Screen featuring Confetti burst, trophy pop animation,
 * and dynamic time-based greeting (Good Morning, Good Afternoon, Good Evening, Good Night).
 */
@Composable
private fun DismissalCelebrationView(
    onDismissed: () -> Unit
) {
    // Determine dynamic greeting based on current hour
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 4..11 -> Pair("Good Morning! 🌅", "Rise and shine! You've conquered the morning alarm.")
        in 12..16 -> Pair("Good Afternoon! ☀️", "Great job! Stay energized and have a productive day.")
        in 17..20 -> Pair("Good Evening! 🌇", "Well done! Wishing you a relaxing and wonderful evening.")
        else -> Pair("Good Night! 🌙", "Alarm completed! Have a restful and peaceful night.")
    }

    val trophyScale = remember { Animatable(0f) }
    val confettiProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        trophyScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
        )
        confettiProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        delay(2500)
        onDismissed()
    }

    // Generate random confetti particle coordinates
    val particles = remember {
        val rand = Random(42)
        List(45) {
            ConfettiParticle(
                x = rand.nextFloat(),
                speedY = rand.nextFloat() * 0.8f + 0.3f,
                speedX = (rand.nextFloat() - 0.5f) * 0.4f,
                size = rand.nextFloat() * 8f + 6f,
                color = when (rand.nextInt(5)) {
                    0 -> Color(0xFFFFD54F)
                    1 -> Color(0xFF81C784)
                    2 -> Color(0xFF64B5F6)
                    3 -> Color(0xFFE57373)
                    else -> Color(0xFFBA68C8)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val p = confettiProgress.value
            for (particle in particles) {
                val currentY = (particle.speedY * p * size.height) % size.height
                val currentX = (particle.x * size.width + particle.speedX * p * size.width) % size.width
                drawCircle(
                    color = particle.color.copy(alpha = (1f - p * 0.4f).coerceIn(0f, 1f)),
                    radius = particle.size,
                    center = Offset(currentX, currentY)
                )
            }
        }

        // Center Celebration Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy Glow Container
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(trophyScale.value)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFF9800),
                                Color(0xFF673AB7)
                            )
                        )
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
            ) {
                Text(
                    text = "🎉 CONGRATULATIONS! 🎉",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFFFFE082),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = greeting.first,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = greeting.second,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onDismissed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .testTag("dismiss_celebration_button")
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val color: Color
)
