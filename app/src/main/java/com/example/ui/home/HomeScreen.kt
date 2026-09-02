package com.example.ui.home

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Alarm
import com.example.model.ChallengeType
import com.example.ui.theme.PolishActiveContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCardBg
import com.example.ui.theme.PolishInactiveContainer
import com.example.ui.theme.PolishInactiveTrack
import com.example.ui.theme.PolishLavenderBg
import com.example.ui.theme.PolishMuted
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    nextAlarmMessage: String?,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onEditAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onAddAlarm: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTestRingAlarm: (Context, Alarm) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = PolishLavenderBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PolishLavenderBg)
        ) {
            // Header Row: App Title, Next Alarm Subtitle, 3-Dot Settings & Add Alarm Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Alarm",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = nextAlarmMessage?.let { "Next in $it" } ?: "No active alarms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishTextSecondary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3-Dot Settings Button (Prominent)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PolishCardBg)
                            .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = onNavigateToSettings)
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                            .testTag("settings_menu_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Settings & About",
                            tint = PolishTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Add Alarm Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PolishPrimary)
                            .clickable(onClick = onAddAlarm)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                            .testTag("add_alarm_header_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Alarm",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Alarms List / Empty State
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 40.dp)
                    ) {
                        Surface(
                            color = PolishCardBg,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, PolishBorder),
                            modifier = Modifier.size(88.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = PolishPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No Alarms Created",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PolishTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You haven't set any alarms yet.\nTap below to configure a new smart alarm.",
                            fontSize = 14.sp,
                            color = PolishTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onAddAlarm,
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("create_first_alarm_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Alarm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        PolishAlarmCard(
                            alarm = alarm,
                            onToggle = { isChecked -> onToggleAlarm(alarm, isChecked) },
                            onClick = { onEditAlarm(alarm) },
                            onDelete = { onDeleteAlarm(alarm) },
                            onTestRing = { onTestRingAlarm(context, alarm) }
                        )
                    }

                    // Smart Mode Active Banner
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        PolishSmartModeBanner()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PolishAlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTestRing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = alarm.isEnabled
    val containerBg = PolishCardBg
    val borderStrokeColor = if (isEnabled) PolishPrimary.copy(alpha = 0.5f) else PolishBorder

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(if (isEnabled) 1.5.dp else 1.dp, borderStrokeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 2.dp else 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("alarm_card_${alarm.id}")
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            // Row 1: Time, AM/PM, and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val hour12 = when {
                        alarm.hour == 0 -> 12
                        alarm.hour > 12 -> alarm.hour - 12
                        else -> alarm.hour
                    }
                    val formattedHourMinute = String.format("%02d:%02d", hour12, alarm.minute)
                    val amPm = if (alarm.hour >= 12) "PM" else "AM"

                    Text(
                        text = formattedHourMinute,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) PolishTextPrimary else PolishTextSecondary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = amPm,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) PolishPrimary else PolishTextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Switch
                PolishCustomSwitch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("toggle_alarm_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Challenge Chip Badge, Repeating Days, Label, and Vibration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Challenge Pill Badge
                if (alarm.isSmartAlarm) {
                    val challengeBadgeText = when (alarm.challengeType) {
                        ChallengeType.MATH -> "MATH (${alarm.mathDifficulty.name.uppercase()})"
                        ChallengeType.SCAN -> "PHOTO SCAN"
                        ChallengeType.MATH_AND_SCAN -> "MATH + SCAN"
                    }
                    Surface(
                        color = if (isEnabled) PolishActiveContainer else PolishInactiveContainer,
                        border = BorderStroke(1.dp, if (isEnabled) PolishPrimary.copy(alpha = 0.3f) else PolishBorder),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = challengeBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) PolishPrimary else PolishTextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = PolishInactiveContainer,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "STANDARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Repeat Days / Label
                Text(
                    text = "${alarm.label} • ${alarm.getRepeatDaysText()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Vibration Indicator
                if (alarm.isVibrate) {
                    Icon(
                        Icons.Default.Vibration,
                        contentDescription = "Vibration enabled",
                        tint = PolishTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 3: Action Controls (Test Ring & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Ring Trigger Pill Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolishActiveContainer)
                        .clickable(onClick = onTestRing)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PolishPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Alarm Ring",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete alarm",
                        tint = PolishTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Custom switch matching the Professional Polish aesthetic:
 * 56dp width x 32dp height rounded pill track with smooth animated thumb and check icon.
 */
@Composable
fun PolishCustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackBgColor by animateColorAsState(
        targetValue = if (checked) PolishPrimary else PolishInactiveTrack,
        label = "switch_track_color"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 26.dp else 4.dp,
        label = "switch_thumb_offset"
    )
    val thumbBgColor by animateColorAsState(
        targetValue = if (checked) Color.White else PolishMuted,
        label = "switch_thumb_color"
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(trackBgColor)
            .border(
                BorderStroke(
                    width = if (checked) 0.dp else 1.dp,
                    color = if (checked) Color.Transparent else PolishBorder
                ),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(thumbBgColor)
                .shadow(elevation = 1.dp, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = PolishPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun PolishSmartModeBanner(modifier: Modifier = Modifier) {
    Surface(
        color = PolishCardBg,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PolishBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PolishActiveContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = PolishPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Smart Wake-Up Active",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Text(
                    text = "Alarms enforce wake-up challenges to prevent oversleeping",
                    fontSize = 12.sp,
                    color = PolishTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
