package com.example.ui.ringing

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.engine.ScanMatcherEngine
import com.example.ui.theme.PolishActiveContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishInactiveContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.SoftRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

@Composable
fun ScanChallengeView(
    referencePhotoPaths: List<String>,
    onChallengeCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var useFrontCamera by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var activeCamera: Camera? by remember { mutableStateOf(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (_: Exception) { }
        }
    }

    var isChecking by remember { mutableStateOf(false) }
    var currentSimilarity by remember { mutableDoubleStateOf(0.0) }
    var statusMessage by remember { mutableStateOf("Point camera at your registered object") }
    var isSuccess by remember { mutableStateOf(false) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var emergencyTapsRemaining by remember { mutableIntStateOf(10) }
    var showEmergencyDismiss by remember { mutableStateOf(referencePhotoPaths.isEmpty()) }

    // Infinite laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Function to trigger a single capture & match check
    fun performScanCheck() {
        if (isChecking || isSuccess) return
        val capture = imageCapture ?: return
        isChecking = true
        statusMessage = "Analyzing object..."

        capture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val rotation = image.imageInfo.rotationDegrees
                    val bitmap = image.toBitmap()
                    image.close()

                    scope.launch {
                        val result = if (referencePhotoPaths.isNotEmpty()) {
                            ScanMatcherEngine.compareWithReferences(bitmap, referencePhotoPaths, rotation)
                        } else {
                            ScanMatcherEngine.MatchResult(0.0, false, -1, "No reference photo registered")
                        }

                        currentSimilarity = result.similarityPercentage
                        isChecking = false

                        if (result.isMatched) {
                            isSuccess = true
                            statusMessage = "Object Match Confirmed (${result.similarityPercentage.toInt()}%)!"
                            delay(700)
                            onChallengeCompleted()
                        } else {
                            failedAttempts++
                            statusMessage = if (referencePhotoPaths.isEmpty()) {
                                "No registered photo found. Use Emergency Wake-up below."
                            } else {
                                "Match: ${result.similarityPercentage.toInt()}% (60% needed to unlock)"
                            }

                            if (failedAttempts >= 4 || referencePhotoPaths.isEmpty()) {
                                showEmergencyDismiss = true
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    isChecking = false
                }
            }
        )
    }

    // Continuous automatic scan loop every 1.8 seconds
    LaunchedEffect(hasCameraPermission, imageCapture, isSuccess) {
        if (!hasCameraPermission) return@LaunchedEffect
        delay(1200) // wait for camera to warm up
        while (isActive && !isSuccess) {
            if (!isChecking && imageCapture != null && referencePhotoPaths.isNotEmpty()) {
                performScanCheck()
            }
            delay(1800)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Reference Photos Bar & Match Gauge
        Column(modifier = Modifier.fillMaxWidth()) {
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
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Scan Target Object",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Match requirement: 60%",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Live Similarity Badge
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (currentSimilarity >= 60.0) PolishPrimary else Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (currentSimilarity >= 60.0) Color.White else Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "${currentSimilarity.toInt()}% Match",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Similarity Progress Bar
            LinearProgressIndicator(
                progress = { (currentSimilarity / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (currentSimilarity >= 60.0) PolishPrimary else Color(0xFF64B5F6),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            if (referencePhotoPaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(referencePhotoPaths) { index, path ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, PolishPrimary, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(File(path)),
                                contentDescription = "Reference $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Live Viewfinder Camera Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            val selector = if (useFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    capture
                                )
                                activeCamera = cam
                            } catch (_: Exception) { }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            val selector = if (useFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    capture
                                )
                                activeCamera = cam
                            } catch (_: Exception) { }
                        }, ContextCompat.getMainExecutor(context))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Camera access needed to verify object",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // Target Reticle Box
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(
                        2.5.dp,
                        if (currentSimilarity >= 60.0) Color(0xFF81C784) else PolishPrimary.copy(alpha = 0.85f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Animated laser line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(3.dp)
                        .offset(y = laserOffset.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, PolishPrimary, Color.White, PolishPrimary, Color.Transparent)
                            )
                        )
                )
            }

            // Top Camera Controls overlay: Torch & Flip
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Torch Button
                IconButton(
                    onClick = {
                        val newTorchState = !isTorchOn
                        isTorchOn = newTorchState
                        activeCamera?.cameraControl?.enableTorch(newTorchState)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isTorchOn) PolishPrimary else Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Flip Camera Button
                IconButton(
                    onClick = { useFrontCamera = !useFrontCamera },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishPrimary.copy(alpha = 0.90f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(68.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "OBJECT VERIFIED (${currentSimilarity.toInt()}%)",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Status Box & Scan Action
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, PolishBorder.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSuccess) PolishPrimary else if (currentSimilarity > 0) Color(0xFF1E88E5) else Color(0xFF2C194D),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Scan Trigger Button
                Button(
                    onClick = { performScanCheck() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("scan_verify_button")
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (isChecking) "ANALYZING..." else "SCAN NOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                // Emergency tap wake-up fallback
                if (showEmergencyDismiss) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            emergencyTapsRemaining--
                            if (emergencyTapsRemaining <= 0) {
                                isSuccess = true
                                statusMessage = "Emergency Wake-up Verified!"
                                scope.launch {
                                    delay(600)
                                    onChallengeCompleted()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishInactiveContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = SoftRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (emergencyTapsRemaining > 0) "Emergency Wake-up: Tap $emergencyTapsRemaining more times" else "Alarm Dismissed",
                            color = SoftRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
