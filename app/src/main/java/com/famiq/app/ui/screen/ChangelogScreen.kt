package com.famiq.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val latestVersion by viewModel.latestVersion.collectAsStateWithLifecycle()
    val changelog by viewModel.updateChangelog.collectAsStateWithLifecycle()
    val downloadUrl by viewModel.downloadUrl.collectAsStateWithLifecycle()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsStateWithLifecycle()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.back),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isUpdateAvailable) stringResource(R.string.update_available) else stringResource(R.string.changelog),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isUpdateAvailable) stringResource(R.string.new_version_info) else "Review our latest improvements",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // --- VERSION INFO SECTION (Card Style, Rounded, Padded) ---
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.radialGradient(listOf(GreenSoft, GreenLight.copy(alpha = 0.3f))),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUpdateAvailable) Icons.Outlined.RocketLaunch else Icons.Outlined.CheckCircle, 
                            contentDescription = null, 
                            tint = GreenMain,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isUpdateAvailable) stringResource(R.string.update_ready_msg) else stringResource(R.string.up_to_date_msg), 
                            fontSize = 12.sp, 
                            color = if (isUpdateAvailable) GreenAccent else onBg.copy(alpha = 0.6f), 
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = stringResource(R.string.version_caption_prefix, latestVersion.ifEmpty { "v1.0.2" }), 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Black, 
                            color = onBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 40.dp),
                thickness = 1.dp,
                color = onBg.copy(alpha = 0.05f)
            )

            // --- CHANGELOG SECTION ---
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.HistoryEdu, 
                        contentDescription = null, 
                        tint = onBg.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.changelog),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = onBg
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, onBg.copy(alpha = 0.05f))
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = formatChangelog(changelog.ifEmpty { "No details available." }),
                            fontSize = 14.sp,
                            color = onBg.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // --- ACTION BUTTONS ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (downloadUrl.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(stringResource(R.string.update_now), fontWeight = FontWeight.ExtraBold)
                    }

                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.later), color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun formatChangelog(text: String) = buildAnnotatedString {
    val lines = text.split("\n")
    lines.forEach { line ->
        val trimmed = line.trim()
        when {
            // Judul/Header (diawali # atau diakhiri :)
            trimmed.startsWith("#") || trimmed.endsWith(":") -> {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = GreenDark)) {
                    append(line.replace("#", "").trim())
                }
            }
            // Sub-judul/Poin Penting (diawali * atau -)
            trimmed.startsWith("*") || trimmed.startsWith("-") -> {
                val parts = line.split("**")
                if (parts.size >= 3) { // Mendukung bold markdown **teks**
                    parts.forEachIndexed { index, part ->
                        if (index % 2 == 1) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
                        } else {
                            append(part)
                        }
                    }
                } else {
                    append(line)
                }
            }
            else -> append(line)
        }
        append("\n")
    }
}
