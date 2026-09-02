package com.example.ui.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.SmartAlarmApplication
import com.example.audio.AlarmSoundManager
import com.example.model.Alarm
import com.example.model.ChallengeType
import com.example.model.MathDifficulty
import com.example.model.SoundType
import com.example.ui.components.TimePickerComponent
import com.example.ui.home.PolishCustomSwitch
import com.example.ui.theme.PolishActiveContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCardBg
import com.example.ui.theme.PolishInactiveContainer
import com.example.ui.theme.PolishInactiveTrack
import com.example.ui.theme.PolishLavenderBg
import com.example.ui.theme.PolishOnActiveContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryBadge
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.SoftRed
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditAlarmScreen(
    initialAlarm: Alarm?,
    onSaveAlarm: (Alarm) -> Unit,
    onNavigateToScanSetup: (Alarm) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { (context.applicationContext as SmartAlarmApplication).soundManager }
    val builtInSounds = remember { AlarmSoundManager.loadSounds(context) }
    val scope = rememberCoroutineScope()

    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Morning Alarm") }
    var repeatDays by remember { mutableStateOf(initialAlarm?.repeatDays ?: listOf(1, 2, 3, 4, 5, 6, 7)) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Intercept hardware/gesture back press to display exit confirmation dialog
    BackHandler(enabled = true) {
        showExitConfirmDialog = true
    }

    var soundType by remember { mutableStateOf(initialAlarm?.soundType ?: SoundType.BUILT_IN) }
    var soundUriOrName by remember { mutableStateOf(initialAlarm?.soundUriOrName ?: "extreme_siren") }
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var currentPlayingId by remember { mutableStateOf<String?>(null) }
    var showSoundSelectionSheet by remember { mutableStateOf(false) }

    var isVibrate by remember { mutableStateOf(initialAlarm?.isVibrate ?: true) }
    var isSmartAlarm by remember { mutableStateOf(initialAlarm?.isSmartAlarm ?: false) }
    var challengeType by remember { mutableStateOf(initialAlarm?.challengeType ?: ChallengeType.MATH) }
    var mathDifficulty by remember { mutableStateOf(initialAlarm?.mathDifficulty ?: MathDifficulty.EASY) }
    var mathProblemCount by remember { mutableIntStateOf(initialAlarm?.mathProblemCount ?: 3) }
    var autoStopMinutes by remember { mutableIntStateOf(initialAlarm?.autoStopMinutes ?: 30) }
    var customAutoStopInput by remember {
        mutableStateOf(
            if (initialAlarm != null && initialAlarm.autoStopMinutes !in listOf(30, 60, 120, 180)) {
                initialAlarm.autoStopMinutes.toString()
            } else ""
        )
    }
    var isCustomAutoStopSelected by remember { mutableStateOf(autoStopMinutes !in listOf(30, 60, 120, 180)) }

    // Dynamic reactive list for scan reference photos
    val referencePhotos = remember {
        mutableStateListOf<String>().apply {
            addAll(initialAlarm?.referencePhotoPaths ?: emptyList())
        }
    }

    // Sync any newly captured reference photos or updated initial alarm without losing user state
    LaunchedEffect(initialAlarm) {
        initialAlarm?.let { updated ->
            if (updated.referencePhotoPaths != referencePhotos.toList()) {
                referencePhotos.clear()
                referencePhotos.addAll(updated.referencePhotoPaths)
            }
            if (updated.isSmartAlarm && !isSmartAlarm) {
                isSmartAlarm = true
            }
        }
    }

    // Helper to build the current draft state including all user selections
    fun buildCurrentDraft(): Alarm {
        val finalPhotos = referencePhotos.toList()
        val finalAutoStop = if (isCustomAutoStopSelected) {
            customAutoStopInput.toIntOrNull() ?: 30
        } else {
            autoStopMinutes
        }
        return (initialAlarm ?: Alarm()).copy(
            hour = hour,
            minute = minute,
            label = label.ifBlank { "Smart Alarm" },
            isEnabled = true,
            repeatDays = repeatDays,
            soundType = soundType,
            soundUriOrName = soundUriOrName,
            isVibrate = isVibrate,
            isSmartAlarm = isSmartAlarm,
            challengeType = challengeType,
            mathDifficulty = mathDifficulty,
            mathProblemCount = mathProblemCount,
            autoStopMinutes = finalAutoStop,
            referencePhotoPaths = finalPhotos
        )
    }

    // Custom Ringtone picker launcher
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                soundType = SoundType.CUSTOM
                soundUriOrName = uri.toString()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.stopPreview()
        }
    }

    Scaffold(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialAlarm == null || initialAlarm.id == 0L) "Set Alarm" else "Edit Alarm",
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                // If Scan or Math+Scan is enabled and photo list is empty, prompt user to capture
                                if (isSmartAlarm && (challengeType == ChallengeType.SCAN || challengeType == ChallengeType.MATH_AND_SCAN) && referencePhotos.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Please capture at least 1 object with the camera for Scan Challenge",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onNavigateToScanSetup(buildCurrentDraft())
                                    return@launch
                                }

                                val newAlarm = buildCurrentDraft()

                                Toast.makeText(
                                    context,
                                    "Alarm saved for ${String.format("%02d:%02d", if (hour == 0) 12 else if (hour > 12) hour - 12 else hour, minute)} ${if (hour >= 12) "PM" else "AM"}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onSaveAlarm(newAlarm)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("save_alarm_button")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishLavenderBg
                )
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PolishLavenderBg)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. TIME PICKER COMPONENT
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePickerComponent(
                        hour24 = hour,
                        minute = minute,
                        onTimeChanged = { h, m ->
                            hour = h
                            minute = m
                        }
                    )
                }
            }

            // ==========================================
            // 2. LABEL INPUT
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Alarm Label",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PolishTextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        placeholder = { Text("e.g. Work, Workout, Study", color = PolishTextSecondary.copy(alpha = 0.6f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishBorder,
                            focusedContainerColor = PolishInactiveContainer,
                            unfocusedContainerColor = PolishInactiveContainer,
                            focusedTextColor = PolishTextPrimary,
                            unfocusedTextColor = PolishTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alarm_label_input")
                    )
                }
            }

            // ==========================================
            // 3. REPEAT SCHEDULE (DAYS OF WEEK)
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Repeat Schedule",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PolishTextSecondary
                        )
                        Text(
                            text = if (repeatDays.size == 7) "Every day" else if (repeatDays.isEmpty()) "Once (Ring once)" else if (repeatDays.toSet() == setOf(1, 2, 3, 4, 5)) "Weekdays" else if (repeatDays.toSet() == setOf(6, 7)) "Weekends" else "${repeatDays.size} days / week",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PolishPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isEveryDay = repeatDays.size == 7
                        val isWeekdays = repeatDays.toSet() == setOf(1, 2, 3, 4, 5)
                        val isWeekends = repeatDays.toSet() == setOf(6, 7)
                        val isOnce = repeatDays.isEmpty()

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isEveryDay) PolishPrimary else PolishInactiveContainer,
                            border = BorderStroke(1.dp, if (isEveryDay) PolishPrimary else PolishBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { repeatDays = listOf(1, 2, 3, 4, 5, 6, 7) }
                        ) {
                            Text(
                                text = "Every day",
                                fontSize = 11.sp,
                                fontWeight = if (isEveryDay) FontWeight.Bold else FontWeight.Medium,
                                color = if (isEveryDay) Color.White else PolishTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isWeekdays) PolishPrimary else PolishInactiveContainer,
                            border = BorderStroke(1.dp, if (isWeekdays) PolishPrimary else PolishBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { repeatDays = listOf(1, 2, 3, 4, 5) }
                        ) {
                            Text(
                                text = "Weekdays",
                                fontSize = 11.sp,
                                fontWeight = if (isWeekdays) FontWeight.Bold else FontWeight.Medium,
                                color = if (isWeekdays) Color.White else PolishTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isWeekends) PolishPrimary else PolishInactiveContainer,
                            border = BorderStroke(1.dp, if (isWeekends) PolishPrimary else PolishBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { repeatDays = listOf(6, 7) }
                        ) {
                            Text(
                                text = "Weekends",
                                fontSize = 11.sp,
                                fontWeight = if (isWeekends) FontWeight.Bold else FontWeight.Medium,
                                color = if (isWeekends) Color.White else PolishTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isOnce) PolishPrimary else PolishInactiveContainer,
                            border = BorderStroke(1.dp, if (isOnce) PolishPrimary else PolishBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { repeatDays = emptyList() }
                        ) {
                            Text(
                                text = "Once",
                                fontSize = 11.sp,
                                fontWeight = if (isOnce) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOnce) Color.White else PolishTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val dayItems = listOf(
                        1 to "Mon",
                        2 to "Tue",
                        3 to "Wed",
                        4 to "Thu",
                        5 to "Fri",
                        6 to "Sat",
                        7 to "Sun"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for ((dayIndex, dayLabel) in dayItems) {
                            val isSelected = repeatDays.contains(dayIndex)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PolishPrimary else PolishInactiveContainer)
                                    .border(
                                        1.dp,
                                        if (isSelected) PolishPrimary else PolishBorder,
                                        CircleShape
                                    )
                                    .clickable {
                                        repeatDays = if (isSelected) {
                                            (repeatDays - dayIndex).sorted()
                                        } else {
                                            (repeatDays + dayIndex).sorted()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else PolishTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. SOUND & VIBRATION SETTINGS
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ringtone & Vibration",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PolishTextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Sound Selection Row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PolishInactiveContainer,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSoundSelectionSheet = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PolishActiveContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = PolishPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = "Alarm Sound",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PolishTextPrimary
                                    )
                                    Text(
                                        text = if (soundType == SoundType.CUSTOM) "Custom Device Sound" else builtInSounds.find { it.id == soundUriOrName }?.displayName ?: soundUriOrName,
                                        fontSize = 12.sp,
                                        color = PolishPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Change sound",
                                tint = PolishTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vibration Row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PolishInactiveContainer,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PolishActiveContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = PolishPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Vibrate on Ringing",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PolishTextPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            PolishCustomSwitch(
                                checked = isVibrate,
                                onCheckedChange = { isVibrate = it }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 5. SMART WAKE-UP CHALLENGES & PROTECTIONS
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isSmartAlarm) PolishPrimary else PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSmartAlarm) PolishPrimary else PolishInactiveContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isSmartAlarm) Color.White else PolishTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Smart Wake-Up Lock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PolishTextPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Requires challenge to dismiss alarm",
                                    fontSize = 12.sp,
                                    color = PolishTextSecondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        PolishCustomSwitch(
                            checked = isSmartAlarm,
                            onCheckedChange = { isSmartAlarm = it }
                        )
                    }

                    AnimatedVisibility(
                        visible = isSmartAlarm,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "CHALLENGE TYPE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary,
                                letterSpacing = 1.sp
                            )

                            // Challenge Type Selector Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChallengeChip(
                                    title = "Math",
                                    description = "Equations",
                                    icon = Icons.Default.Calculate,
                                    isSelected = challengeType == ChallengeType.MATH,
                                    onClick = { challengeType = ChallengeType.MATH },
                                    modifier = Modifier.weight(1f)
                                )
                                ChallengeChip(
                                    title = "Scan Object",
                                    description = "Real item",
                                    icon = Icons.Default.QrCodeScanner,
                                    isSelected = challengeType == ChallengeType.SCAN,
                                    onClick = { challengeType = ChallengeType.SCAN },
                                    modifier = Modifier.weight(1f)
                                )
                                ChallengeChip(
                                    title = "Extreme",
                                    description = "Math + Scan",
                                    icon = Icons.Default.Security,
                                    isSelected = challengeType == ChallengeType.MATH_AND_SCAN,
                                    onClick = { challengeType = ChallengeType.MATH_AND_SCAN },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Math Settings (If Math or Math+Scan selected)
                            if (challengeType == ChallengeType.MATH || challengeType == ChallengeType.MATH_AND_SCAN) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(PolishInactiveContainer)
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Calculate,
                                            contentDescription = null,
                                            tint = PolishPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Math Challenge Settings",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = PolishTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Difficulty",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PolishTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MathDifficulty.entries.forEach { diff ->
                                            val isSelected = mathDifficulty == diff
                                            MathDifficultyChip(
                                                label = when (diff) {
                                                    MathDifficulty.EASY -> "Easy"
                                                    MathDifficulty.MEDIUM -> "Medium"
                                                    MathDifficulty.HARD -> "Hard"
                                                },
                                                isSelected = isSelected,
                                                onClick = { mathDifficulty = diff },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Problems to solve",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PolishTextSecondary
                                        )
                                        Text(
                                            text = "$mathProblemCount questions",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PolishPrimary
                                        )
                                    }

                                    Slider(
                                        value = mathProblemCount.toFloat(),
                                        onValueChange = { mathProblemCount = it.toInt() },
                                        valueRange = 1f..5f,
                                        steps = 3,
                                        colors = SliderDefaults.colors(
                                            thumbColor = PolishPrimary,
                                            activeTrackColor = PolishPrimary,
                                            inactiveTrackColor = PolishInactiveTrack
                                        )
                                    )
                                }
                            }

                            // Scan Photo Manager (If Scan or Math+Scan selected)
                            if (challengeType == ChallengeType.SCAN || challengeType == ChallengeType.MATH_AND_SCAN) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(PolishInactiveContainer)
                                        .border(1.dp, if (referencePhotos.isEmpty()) SoftRed.copy(alpha = 0.5f) else PolishBorder, RoundedCornerShape(18.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = PolishPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Target Object Photo",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PolishTextPrimary
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = if (referencePhotos.isEmpty()) SoftRed.copy(alpha = 0.15f) else PolishActiveContainer
                                        ) {
                                            Text(
                                                text = if (referencePhotos.isEmpty()) "Required" else "${referencePhotos.size} Saved",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (referencePhotos.isEmpty()) SoftRed else PolishPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (referencePhotos.isNotEmpty()) {
                                        // Horizontal photo list + Add more button
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            itemsIndexed(referencePhotos) { index, path ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(68.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .border(1.5.dp, PolishPrimary, RoundedCornerShape(14.dp))
                                                ) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(File(path)),
                                                        contentDescription = "Photo ${index + 1}",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )

                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(3.dp)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(SoftRed)
                                                            .clickable { referencePhotos.removeAt(index) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            item {
                                                Surface(
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = PolishCardBg,
                                                    border = BorderStroke(1.dp, PolishPrimary),
                                                    modifier = Modifier
                                                        .size(68.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .clickable { onNavigateToScanSetup(buildCurrentDraft()) }
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        Icon(
                                                            Icons.Default.CameraAlt,
                                                            contentDescription = "Add photo",
                                                            tint = PolishPrimary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "+ Retake",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PolishPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Point camera at this object when the alarm rings (60% match to dismiss).",
                                            fontSize = 11.sp,
                                            color = PolishTextSecondary
                                        )
                                    } else {
                                        Surface(
                                            color = PolishCardBg,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, PolishBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Snap a photo of your morning item (e.g. bathroom sink or coffee mug). You will need to scan it to turn off the alarm.",
                                                    fontSize = 12.sp,
                                                    color = PolishTextSecondary,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 17.sp
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Button(
                                                    onClick = { onNavigateToScanSetup(buildCurrentDraft()) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                                    shape = RoundedCornerShape(100.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.CameraAlt,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Take Target Photo",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Auto Stop Settings
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PolishInactiveContainer)
                                    .padding(14.dp)
                                ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = PolishPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Auto Stop Duration",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PolishTextPrimary
                                    )
                                }
                                Text(
                                    text = "Automatically stops alarm if not dismissed within duration",
                                    fontSize = 12.sp,
                                    color = PolishTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val presetMinutes = listOf(15, 30, 60, 120, 180)
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(presetMinutes) { mins ->
                                        val isSelected = !isCustomAutoStopSelected && autoStopMinutes == mins
                                        AutoStopDurationPresetChip(
                                            label = "$mins min",
                                            isSelected = isSelected,
                                            onClick = {
                                                isCustomAutoStopSelected = false
                                                autoStopMinutes = mins
                                            }
                                        )
                                    }
                                    item {
                                        AutoStopDurationPresetChip(
                                            label = "Custom",
                                            isSelected = isCustomAutoStopSelected,
                                            onClick = { isCustomAutoStopSelected = true }
                                        )
                                    }
                                }

                                if (isCustomAutoStopSelected) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = customAutoStopInput,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() } && input.length <= 4) {
                                                customAutoStopInput = input
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                "Enter minutes (e.g. 45)",
                                                fontSize = 13.sp,
                                                color = PolishTextSecondary.copy(alpha = 0.6f)
                                            )
                                        },
                                        trailingIcon = {
                                            Text(
                                                "min",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = PolishPrimary,
                                                modifier = Modifier.padding(end = 12.dp)
                                            )
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PolishTextPrimary
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PolishPrimary,
                                            unfocusedBorderColor = PolishBorder,
                                            focusedContainerColor = PolishCardBg,
                                            unfocusedContainerColor = PolishCardBg,
                                            focusedTextColor = PolishTextPrimary,
                                            unfocusedTextColor = PolishTextPrimary,
                                            cursorColor = PolishPrimary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("custom_autostop_input")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // ALARM SOUND SELECTION MODAL BOTTOM SHEET
    // ==========================================
    if (showSoundSelectionSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                soundManager.stopPreview()
                isPreviewPlaying = false
                currentPlayingId = null
                showSoundSelectionSheet = false
            },
            sheetState = sheetState,
            containerColor = PolishCardBg,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Alarm Sound",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "Tap to preview loud wake-up melodies",
                            fontSize = 13.sp,
                            color = PolishTextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            soundManager.stopPreview()
                            isPreviewPlaying = false
                            currentPlayingId = null
                            showSoundSelectionSheet = false
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PolishTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "HIGH-INTENSITY & BUILT-IN SOUNDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(builtInSounds, key = { it.id }) { sound ->
                        val isSelected = soundType == SoundType.BUILT_IN && soundUriOrName == sound.id
                        val isThisPlaying = isPreviewPlaying && currentPlayingId == sound.id

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) PolishActiveContainer else PolishInactiveContainer,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) PolishPrimary else PolishBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    soundType = SoundType.BUILT_IN
                                    soundUriOrName = sound.id
                                    currentPlayingId = sound.id
                                    isPreviewPlaying = true
                                    soundManager.previewSound(SoundType.BUILT_IN, sound.id) {
                                        isPreviewPlaying = false
                                        currentPlayingId = null
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Play / Stop Icon Button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isThisPlaying) PolishPrimary else PolishCardBg)
                                        .clickable {
                                            if (isThisPlaying) {
                                                soundManager.stopPreview()
                                                isPreviewPlaying = false
                                                currentPlayingId = null
                                            } else {
                                                soundType = SoundType.BUILT_IN
                                                soundUriOrName = sound.id
                                                currentPlayingId = sound.id
                                                isPreviewPlaying = true
                                                soundManager.previewSound(SoundType.BUILT_IN, sound.id) {
                                                    isPreviewPlaying = false
                                                    currentPlayingId = null
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isThisPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isThisPlaying) "Stop" else "Play",
                                        tint = if (isThisPlaying) Color.White else PolishPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = sound.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = PolishTextPrimary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = if (isSelected) PolishPrimary else PolishBorder.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = sound.tag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else PolishTextSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sound.description,
                                        fontSize = 12.sp,
                                        color = PolishTextSecondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Radio Selection Checkmark
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) PolishPrimary else Color.Transparent)
                                        .border(
                                            2.dp,
                                            if (isSelected) PolishPrimary else PolishBorder,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "CUSTOM DEVICE RINGTONE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Custom Ringtone Row
                    item {
                        val isCustomSelected = soundType == SoundType.CUSTOM
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isCustomSelected) PolishActiveContainer else PolishInactiveContainer,
                            border = BorderStroke(
                                if (isCustomSelected) 2.dp else 1.dp,
                                if (isCustomSelected) PolishPrimary else PolishBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    }
                                    ringtonePickerLauncher.launch(intent)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(PolishCardBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = PolishPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Choose from Device Storage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PolishTextPrimary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isCustomSelected) "Custom sound selected" else "Browse device ringtones & music files",
                                        fontSize = 12.sp,
                                        color = PolishTextSecondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isCustomSelected) PolishPrimary else Color.Transparent)
                                        .border(
                                            2.dp,
                                            if (isCustomSelected) PolishPrimary else PolishBorder,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCustomSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Confirm Button
                Button(
                    onClick = {
                        soundManager.stopPreview()
                        isPreviewPlaying = false
                        currentPlayingId = null
                        showSoundSelectionSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Confirm Selection", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // Unsaved Changes / Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = PolishCardBg,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PolishActiveContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = PolishPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Save Alarm Changes?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PolishTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You are leaving without saving. Would you like to save this alarm or close without saving?",
                        fontSize = 14.sp,
                        color = PolishTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = PolishInactiveContainer,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                val hour12 = when {
                                    hour == 0 -> 12
                                    hour > 12 -> hour - 12
                                    else -> hour
                                }
                                val amPm = if (hour >= 12) "PM" else "AM"
                                Text(
                                    text = String.format("%02d:%02d %s", hour12, minute, amPm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PolishPrimary
                                )
                                Text(
                                    text = label.ifBlank { "Smart Alarm" },
                                    fontSize = 12.sp,
                                    color = PolishTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = PolishActiveContainer,
                                border = BorderStroke(1.dp, PolishPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (repeatDays.size == 7) "Every day" else if (repeatDays.isEmpty()) "Once" else if (repeatDays.toSet() == setOf(1, 2, 3, 4, 5)) "Weekdays" else if (repeatDays.toSet() == setOf(6, 7)) "Weekends" else "${repeatDays.size} days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        if (isSmartAlarm && (challengeType == ChallengeType.SCAN || challengeType == ChallengeType.MATH_AND_SCAN) && referencePhotos.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Please capture at least 1 object with the camera for Scan Challenge",
                                Toast.LENGTH_LONG
                            ).show()
                            onNavigateToScanSetup(buildCurrentDraft())
                            return@Button
                        }
                        val newAlarm = buildCurrentDraft()
                        Toast.makeText(
                            context,
                            "Alarm saved for ${String.format("%02d:%02d", if (hour == 0) 12 else if (hour > 12) hour - 12 else hour, minute)} ${if (hour >= 12) "PM" else "AM"}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onSaveAlarm(newAlarm)
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_save_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Exit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Close / Discard Button
                    Button(
                        onClick = {
                            showExitConfirmDialog = false
                            onBack()
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishInactiveContainer,
                            contentColor = SoftRed
                        ),
                        border = BorderStroke(1.dp, SoftRed.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dialog_close_discard_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Close", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Keep Editing Button
                    Button(
                        onClick = {
                            showExitConfirmDialog = false
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishInactiveContainer,
                            contentColor = PolishTextPrimary
                        ),
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dialog_keep_editing_button")
                    ) {
                        Text("Keep Editing", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        )
    }
}

@Composable
private fun AutoStopDurationPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PolishPrimary else PolishCardBg,
        border = BorderStroke(
            1.dp,
            if (isSelected) PolishPrimary else PolishBorder.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else PolishTextPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun MathDifficultyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PolishPrimary else PolishCardBg,
        border = BorderStroke(
            1.dp,
            if (isSelected) PolishPrimary else PolishBorder.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else PolishTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun ChallengeChip(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) PolishPrimary else PolishInactiveContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorder),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else PolishPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else PolishTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else PolishTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
