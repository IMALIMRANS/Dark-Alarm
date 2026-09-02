package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishActiveContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCardBg
import com.example.ui.theme.PolishInactiveContainer
import com.example.ui.theme.PolishLavenderBg
import com.example.ui.theme.PolishOnActiveContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = PolishLavenderBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & About",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PolishTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishLavenderBg
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // App Information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PolishPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Alarm,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Dark Alarm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Version 2.0 • Smart Wake-Up Engine",
                                fontSize = 12.sp,
                                color = PolishTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Website Link
                    SocialLinkRow(
                        title = "App Website",
                        subtitle = "https://darkalarm.pages.dev/",
                        icon = Icons.Default.Language,
                        onClick = { openUrl(context, "https://darkalarm.pages.dev/") }
                    )

                    // Privacy Policy
                    SocialLinkRow(
                        title = "Privacy Policy",
                        subtitle = "View privacy terms & conditions",
                        icon = Icons.Default.Policy,
                        onClick = { openUrl(context, "https://darkalarm.pages.dev/") }
                    )
                }
            }

            // Developer Profile Section
            Text(
                text = "Developer Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PolishActiveContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PolishPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "IM AL IMRAN S",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Developer & Creator • @IMALIMRANS",
                                fontSize = 12.sp,
                                color = PolishPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Developer Website
                    SocialLinkRow(
                        title = "Developer Website",
                        subtitle = "https://imalimrans.pages.dev/",
                        icon = Icons.Default.Language,
                        onClick = { openUrl(context, "https://imalimrans.pages.dev/") }
                    )

                    // Facebook
                    SocialLinkRow(
                        title = "Facebook",
                        subtitle = "@IMALIMRANS",
                        icon = Icons.Default.Code,
                        onClick = { openUrl(context, "https://facebook.com/IMALIMRANS") }
                    )

                    // YouTube
                    SocialLinkRow(
                        title = "YouTube",
                        subtitle = "@IMALIMRANS",
                        icon = Icons.Default.Code,
                        onClick = { openUrl(context, "https://youtube.com/@IMALIMRANS") }
                    )

                    // Instagram
                    SocialLinkRow(
                        title = "Instagram",
                        subtitle = "@IMALIMRANS",
                        icon = Icons.Default.Code,
                        onClick = { openUrl(context, "https://instagram.com/IMALIMRANS") }
                    )

                    // TikTok
                    SocialLinkRow(
                        title = "TikTok",
                        subtitle = "@IMALIMRANS",
                        icon = Icons.Default.Code,
                        onClick = { openUrl(context, "https://tiktok.com/@IMALIMRANS") }
                    )
                }
            }

            // Smart Alarm Core Features & Protections
            Text(
                text = "Alarm Protections",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Volume Enforcer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Volume is automatically restored to 100% while alarm rings to guarantee you wake up.",
                                fontSize = 12.sp,
                                color = PolishTextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Persistent Ringing Lock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "The alarm screen cannot be accidentally swiped away or closed until challenges are solved.",
                                fontSize = 12.sp,
                                color = PolishTextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SocialLinkRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = PolishInactiveContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PolishPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = PolishTextSecondary
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open link",
                tint = PolishPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open URL: $url", Toast.LENGTH_SHORT).show()
    }
}
