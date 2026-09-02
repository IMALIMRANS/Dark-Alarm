package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.Alarm
import com.example.service.AlarmRingingService
import com.example.ui.create.CreateEditAlarmScreen
import com.example.ui.create.ReferencePhotoSetupScreen
import com.example.ui.home.AlarmViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.ringing.AlarmRingingActivity
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val alarmViewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndResumeRingingActivity()

        setContent {
            MyApplicationTheme {
                // Request Notification Permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                SmartAlarmApp(viewModel = alarmViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndResumeRingingActivity()
    }

    private fun checkAndResumeRingingActivity() {
        if (AlarmRingingService.isRinging) {
            val ringIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmRingingActivity.EXTRA_ALARM_ID, AlarmRingingService.currentAlarmId)
            }
            startActivity(ringIntent)
        }
    }
}

@Composable
fun SmartAlarmApp(viewModel: AlarmViewModel) {
    val navController = rememberNavController()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val nextAlarmMessage by viewModel.nextAlarmMessage.collectAsStateWithLifecycle()

    var currentDraftAlarm by remember { mutableStateOf<Alarm?>(null) }

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                alarms = alarms,
                nextAlarmMessage = nextAlarmMessage,
                onToggleAlarm = { alarm, isEnabled ->
                    viewModel.toggleAlarm(alarm, isEnabled)
                },
                onEditAlarm = { alarm ->
                    currentDraftAlarm = alarm
                    navController.navigate("create_alarm")
                },
                onDeleteAlarm = { alarm ->
                    viewModel.deleteAlarm(alarm)
                },
                onAddAlarm = {
                    val now = java.util.Calendar.getInstance()
                    currentDraftAlarm = Alarm(
                        hour = now.get(java.util.Calendar.HOUR_OF_DAY),
                        minute = (now.get(java.util.Calendar.MINUTE) + 1) % 60
                    )
                    navController.navigate("create_alarm")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onTestRingAlarm = { context, alarm ->
                    viewModel.testRingAlarm(context, alarm)
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("create_alarm") {
            CreateEditAlarmScreen(
                initialAlarm = currentDraftAlarm,
                onSaveAlarm = { alarm ->
                    viewModel.saveAlarm(alarm)
                    currentDraftAlarm = null
                    navController.popBackStack("home", inclusive = false)
                },
                onNavigateToScanSetup = { draftAlarm ->
                    currentDraftAlarm = draftAlarm
                    navController.navigate("reference_photo_setup")
                },
                onBack = {
                    currentDraftAlarm = null
                    navController.popBackStack()
                }
            )
        }

        composable("reference_photo_setup") {
            ReferencePhotoSetupScreen(
                initialPhotoPaths = currentDraftAlarm?.referencePhotoPaths ?: emptyList(),
                onSaveReferencePhotos = { updatedPhotos ->
                    currentDraftAlarm = (currentDraftAlarm ?: Alarm()).copy(
                        referencePhotoPaths = updatedPhotos,
                        isSmartAlarm = true // Ensure Smart Lock stays on when photos are captured
                    )
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

