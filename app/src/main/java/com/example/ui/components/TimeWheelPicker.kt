package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishActiveContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishInactiveContainer
import com.example.ui.theme.PolishInactiveTrack
import com.example.ui.theme.PolishOnActiveContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyRow

@Composable
fun TimePickerComponent(
    hour24: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPm = hour24 >= 12
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(PolishInactiveContainer)
    ) {
        val isNarrow = maxWidth < 360.dp
        val horizontalPadding = if (isNarrow) 12.dp else 20.dp
        val verticalPadding = if (isNarrow) 16.dp else 20.dp
        val cardWidth = if (isNarrow) 74.dp else 88.dp
        val cardHeight = if (isNarrow) 68.dp else 76.dp
        val digitFontSize = if (isNarrow) 32.sp else 38.sp
        val colonPadding = if (isNarrow) 6.dp else 12.dp
        val colonSize = if (isNarrow) 36.sp else 44.sp
        val pillWidth = if (isNarrow) 48.dp else 56.dp
        val pillHeight = if (isNarrow) 36.dp else 40.dp
        val middleSpacer = if (isNarrow) 8.dp else 14.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hour Column
                TimeNumberColumn(
                    value = hour12,
                    label = "HOUR",
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    digitFontSize = digitFontSize,
                    onIncrement = {
                        val nextHour12 = if (hour12 == 12) 1 else hour12 + 1
                        val newHour24 = if (isPm) {
                            if (nextHour12 == 12) 12 else nextHour12 + 12
                        } else {
                            if (nextHour12 == 12) 0 else nextHour12
                        }
                        onTimeChanged(newHour24, minute)
                    },
                    onDecrement = {
                        val prevHour12 = if (hour12 == 1) 12 else hour12 - 1
                        val newHour24 = if (isPm) {
                            if (prevHour12 == 12) 12 else prevHour12 + 12
                        } else {
                            if (prevHour12 == 12) 0 else prevHour12
                        }
                        onTimeChanged(newHour24, minute)
                    },
                    testTagPrefix = "hour"
                )

                Text(
                    text = ":",
                    fontSize = colonSize,
                    fontWeight = FontWeight.Medium,
                    color = PolishPrimary,
                    modifier = Modifier.padding(horizontal = colonPadding)
                )

                // Minute Column
                TimeNumberColumn(
                    value = minute,
                    label = "MINUTE",
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    digitFontSize = digitFontSize,
                    onIncrement = {
                        val nextMin = (minute + 1) % 60
                        onTimeChanged(hour24, nextMin)
                    },
                    onDecrement = {
                        val prevMin = if (minute == 0) 59 else minute - 1
                        onTimeChanged(hour24, prevMin)
                    },
                    testTagPrefix = "minute"
                )

                Spacer(modifier = Modifier.width(middleSpacer))

                // AM / PM Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AmPmPill(
                        text = "AM",
                        isSelected = !isPm,
                        width = pillWidth,
                        height = pillHeight,
                        onClick = {
                            if (isPm) {
                                val newHour = if (hour24 == 12) 0 else hour24 - 12
                                onTimeChanged(newHour, minute)
                            }
                        },
                        testTag = "am_pill"
                    )

                    AmPmPill(
                        text = "PM",
                        isSelected = isPm,
                        width = pillWidth,
                        height = pillHeight,
                        onClick = {
                            if (!isPm) {
                                val newHour = if (hour24 == 0) 12 else hour24 + 12
                                onTimeChanged(newHour, minute)
                            }
                        },
                        testTag = "pm_pill"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick add buttons with smooth flow
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickTimeChip(label = "+15m", modifier = Modifier.weight(1f)) {
                    val newMin = (minute + 15) % 60
                    val addHour = (minute + 15) / 60
                    val newHour = (hour24 + addHour) % 24
                    onTimeChanged(newHour, newMin)
                }
                QuickTimeChip(label = "+30m", modifier = Modifier.weight(1f)) {
                    val newMin = (minute + 30) % 60
                    val addHour = (minute + 30) / 60
                    val newHour = (hour24 + addHour) % 24
                    onTimeChanged(newHour, newMin)
                }
                QuickTimeChip(label = "+1h", modifier = Modifier.weight(1f)) {
                    val newHour = (hour24 + 1) % 24
                    onTimeChanged(newHour, minute)
                }
                QuickTimeChip(label = "+8h", modifier = Modifier.weight(1f)) {
                    val newHour = (hour24 + 8) % 24
                    onTimeChanged(newHour, minute)
                }
            }
        }
    }
}

@Composable
private fun TimeNumberColumn(
    value: Int,
    label: String,
    cardWidth: androidx.compose.ui.unit.Dp = 88.dp,
    cardHeight: androidx.compose.ui.unit.Dp = 76.dp,
    digitFontSize: androidx.compose.ui.unit.TextUnit = 38.sp,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    testTagPrefix: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(40.dp)
                .testTag("${testTagPrefix}_increment")
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase $label",
                tint = PolishPrimary
            )
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, PolishBorder.copy(alpha = 0.4f)),
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = String.format("%02d", value),
                    fontSize = digitFontSize,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextPrimary
                )
            }
        }

        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(40.dp)
                .testTag("${testTagPrefix}_decrement")
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease $label",
                tint = PolishPrimary
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PolishTextSecondary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun AmPmPill(
    text: String,
    isSelected: Boolean,
    width: androidx.compose.ui.unit.Dp = 56.dp,
    height: androidx.compose.ui.unit.Dp = 40.dp,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PolishActiveContainer else Color.White,
        label = "pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PolishOnActiveContainer else PolishTextSecondary,
        label = "pill_text"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) PolishActiveContainer else PolishBorder.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@Composable
private fun QuickTimeChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(BorderStroke(1.dp, PolishBorder.copy(alpha = 0.4f)), shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PolishTextSecondary
        )
    }
}

